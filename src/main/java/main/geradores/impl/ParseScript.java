package main.geradores.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import main.geradores.GenOptions;
import main.geradores.IGerador;
import main.geradores.Utils;
import main.geradores.model.ModelGenerator;
import main.geradores.model.utils.ConstraintType;
import main.geradores.model.utils.Property;
import main.geradores.model.utils.PropertyType;

public class ParseScript implements IGerador {
	private static String mainPath = "./";

	@Override
	public void gerarArquivos(GenOptions options) throws IOException {
		System.out.println("===============================================");
		System.out.println("============== PARSING SCRIPT =================");

		// 1 - Gera o novo SCRIPT 
		gerarScript(options);

		// 2 - Muda o arquivo de MODELO 
		String path = ParseScript.mainPath + "scripts\\";
		options.modelFile = path + options.entityName + "_script.txt";

		// 3 - Regera o MODELO 
		options.generateModelScript();

		System.out.println("");
		System.out.println("===============================================");
		System.out.println("===============================================");
	}

	private void gerarScript(GenOptions options) throws IOException {
		String path = ParseScript.mainPath + "scripts\\";

		String classBody = getModelClassBodyFromModelGenerator( options );

		Utils.writeContentTo(path + options.entityName + "_script.txt", classBody);
		System.out.println("Generated PARSING SCRIPT '" + options.entityName + "' into '" + path + "'");
	}

	private String getModelClassBodyFromModelGenerator(GenOptions options) {
		ModelGenerator modelGen = options.getModelGenerator();

		String joins = "";
		String properties = "";

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// 1 Criar o SEQUENCE !!
		String out = "-----------------------------------------------------------------------------" + "\r\n" +
				"-- TABELA " + modelGen.getTableName().toUpperCase() + "\r\n"
				;

		out += "CREATE SEQUENCE public."  +  modelGen.getTableName().toLowerCase() + "_seq" + "\r\n" +
				"    " + "START WITH 1"   + "\r\n" +
				"    " + "INCREMENT BY 1" + "\r\n" +
				"    " + "NO MINVALUE"    + "\r\n" +
				"    " + "NO MAXVALUE"    + "\r\n" +
				"    " + "CACHE 1;"       + "\r\n\r\n"
		;

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// 2 - CRIAR TABELA
		out += "CREATE TABLE " + modelGen.getTableName().toUpperCase() + " (" + "\r\n"
		;
		out += String.format("   %-25sINTEGER NOT NULL,\r\n", "ID" ); // TODAS AS TABELAS PARSEADAS TERÃO UM ID !!

		/////////////////////////////////////////////////////////////////////////////////
		// 2.1 - Propriedades
		for( Property prop : modelGen.getProperties() ) {
			if(prop.getType() == PropertyType.JOIN || prop.getType() == PropertyType.JOIN_COMPOSTO) { // JOINS
				joins += String.format("   %-25sINTEGER NOT NULL,\r\n", prop.getName().toUpperCase() );

			} else if (prop.getType() == PropertyType.JOIN_COMPOSTO_CHAVE) { // joins compostos de chave
				joins += "   " + prop.toString() + "\r\n";

			} else if ( prop.isPrimaryKey() ) { // PRIMARY KEY DA TABELA !!
				continue; // IGNORA A PK DA TABELA !!

			} else {
				String type = "";
				switch (prop.getType()) {
					default:
					case STRING:
						type += "VARCHAR(" + prop.getInteiro() + ")"; break;
					case CHAR:
						type += "CHAR(" + prop.getInteiro() + ")"; break;
					case DATE:
						type += "DATE"; break;
					case NUMERO:
						type += "INTEGER"; break;
					case DECIMAL:
						type += "NUMERIC(" + prop.getInteiro() + "," + prop.getDecimal() + ")"; break;
					case SHORT:
						type += "SMALLINT"; break;
					case LONG:
						type += "BIGINT"; break;
				}

				properties += String.format("   %-25s %s%s,\r\n",
						prop.getName().toUpperCase(),
						type,
						prop.isNullable()? "" : " NOT NULL");
			}
		}

		out += joins + "\r\n" + properties;

		/////////////////////////////////////////////////////////////////////////////////
		out += "\r\n";
		out += "   " + "-- CONSTRAINTS"+ "\r\n";

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// 3 - UNIQUES (UNIQUES PADRAO)
		String uniques = modelGen.getConstraints().stream()
				.filter( con -> {
					return con.getType() == ConstraintType.UNIQUE;
				})
				.map( con -> {
					String keys = "";
					for(String column : con.getKeys()) {
						if(keys.equals("") == false) keys += ", ";
						keys += column.toUpperCase();
					}

					return "   " + "CONSTRAINT " + con.getName() + " UNIQUE (" + keys + ")";
					// return con.toString();
				})
				.reduce( (lineA, lineB) -> {
					return lineA + ",\r\n" + lineB;
				})
				.orElse("");
		if(uniques.equals("") == false) {
			out += uniques + ",\r\n";
		}

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// 3.1 - UNIQUES GERADAS PELA PK (UNIQUES DE PK)
		List<String> uniquePK =  modelGen.getProperties().stream()
				.filter( prop -> {
					// 1 - verifica se é primaria
					if( prop.isPrimaryKey() == false) return false;

					// 2 - A chave deve estar presente em alguma das constraints de JOIN!
					if( prop.getType() == PropertyType.JOIN  ||
							prop.getType() == PropertyType.JOIN_COMPOSTO ||
							prop.getType() == PropertyType.JOIN_COMPOSTO_CHAVE) { // Para pegar somente PK vinda de JOINS !!
						return true;
					}

					return false;
				})
				.map( prop -> {
					return prop.getName().toUpperCase();
				})
				.collect( Collectors.toList() );


		if(uniquePK.size() > 1) {
			String keys = String.join(", ", uniquePK);
			out += "   " + "CONSTRAINT " + modelGen.getTableName().toUpperCase() + "_PKEY_UN UNIQUE (" + keys + "),\r\n";
		}

		out += "   " + "CONSTRAINT " + modelGen.getTableName().toUpperCase() + "_PKEY PRIMARY KEY (ID)"+ "\r\n"; // TODAS AS TABELAS TEM SOMENTE UM PK !!
		out += ");" + "\r\n";

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// 4 - INDEXERS
		String indexersConstraint = modelGen.getConstraints().stream()
				.filter( con -> {
					return con.getType() == ConstraintType.INDEX;
				})
				.map( con -> {

					String keys = "";
					for(String column : con.getKeys()) {
						if(keys.equals("") == false) keys += ", ";
						keys += column.toUpperCase();
					}

					return "CREATE INDEX " + con.getName() + " ON " + modelGen.getTableName().toUpperCase() + " (" + keys + ");";
				})
				.reduce( (lineA, lineB) -> {
					return lineA + "\r\n" + lineB;
				})
				.orElse("");
		if(indexersConstraint.equals("") == false) {
			out += "\r\n";
			out += "-- INDEXERS " + "\r\n";
			out += indexersConstraint + "\r\n";
		}

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// 5 - Foreign Keys (JOINS)
		String foreignConstraints = modelGen.getConstraints().stream()
				.filter( con -> {
					return con.getType() == ConstraintType.FOREIGN_KEY;
				})
				.map( con -> {
					String fk = "";

					String keys[] = con.getKeys();
					if(keys.length > 1) { // Se tiver mais de uma chave na constrainyt, deve determinar qual deve ser utilizada !!
						// 1 - a chave a ser utilizada é aquela q não pertence a nenhuma outra constraint, somente a ela mesmo !
						for (int i = 0; i < keys.length; i++) {
							if( modelGen.getConstraintByKey(keys[i]).equals(con) == true) {
								if(fk.equals("") == false) fk += ", ";
								fk += keys[i].toUpperCase();
							}
						}

					} else { // Se tiver somente uma chave, então usa-se ela mesmo !!
						fk = keys[0].toUpperCase();
					}

					// CREATE INDEX FK_EMPRESA_CFOP_DEPTO ON CFOP_DEPARTAMENTO USING btree (ID_EMPRESA);
					String con_out = "CREATE INDEX " + con.getName() + " ON " + modelGen.getTableName().toUpperCase() + " USING btree (" + fk + ")";
					return con_out;
				})
				.reduce( (lineA, lineB) -> {
					return lineA + ";\r\n" + lineB;
				})
				.orElse("");
		if(foreignConstraints.equals("") == false) {
			out += "\r\n";
			out += "-- SCRIPT FOREIGN KEYS " + "\r\n";
			out += foreignConstraints + ";\r\n";
		}
		out += "-----------------------------------------------------------------------------" + "\r\n";

		///////////////////////////////////////////////////////////////////////////////////////////////////
		// 6 - Foreign Key Final (DEVE SER POSTO NO FINAL DO ARQUIVO)
		String foreignKeysFinal = modelGen.getConstraints().stream()
				.filter( con -> {
					return con.getType() == ConstraintType.FOREIGN_KEY;
				})
				.map( con -> {
					String fk = "";
					String fkReference = "";

					String keys[] = con.getKeys();
					String keysReference[] = con.getReference().getKeys();
					if(keys.length > 1) { // Se tiver mais de uma chave na constrainyt, deve determinar qual deve ser utilizada !!
						// 1 - a chave a ser utilizada é aquela q não pertence a nenhuma outra constraint, somente a ela mesmo !
						for (int i = 0; i < keys.length; i++) {
							if( modelGen.getConstraintByKey(keys[i]).equals(con) == true) {
								if(fk.equals("") == false) fk += ", ";
								fk += keys[i].toUpperCase();

								if(fkReference.equals("") == false) fkReference += ", ";
								fkReference += keysReference[i].toUpperCase();
							}
						}

					} else { // Se tiver somente uma chave, então usa-se ela mesmo !!
						fk = keys[0].toUpperCase();
						fkReference += keysReference[0].toUpperCase();
					}

					String con_out = "ALTER TABLE ONLY " + modelGen.getTableName().toUpperCase() + "\r\n" +
							"    " + "ADD CONSTRAINT " + con.getName() + " FOREIGN KEY (" + fk + ") REFERENCES " + con.getReference().getReferenceName() + " (" + fkReference + ");";
					return con_out;
				})
				.reduce( (lineA, lineB) -> {
					return lineA + "\r\n" + lineB;
				})
				.orElse("");
		if(foreignKeysFinal.equals("") == false) {
			out += "\r\n";
			out += "--  (POR NO FINAL DO ARQUIVO)" + "\r\n";
			out += "-- SCRIPT FOREIGN KEYS DA TABELA " + modelGen.getTableName().toUpperCase() + "\r\n";
			out += foreignKeysFinal + "\r\n";
		}

		return out;
	}
}
