package main;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import main.geradores.GenOptions;
import main.geradores.IGerador;
import main.geradores.Utils;
import main.geradores.impl.ParseScript;
import main.geradores.impl.GerarBackEnd;
import main.geradores.impl.GerarFrontEnd;
import main.geradores.model.ModelGenerator;

/**
 * @author Anderson Dourado Cunha
 */
public class GeradorDeEntidades {
	// private static String mainPath = ".\\src\\main\\java\\com\\innovaro\\acs\\";

	public static void gerarEntidadeFrom(String... args) throws IOException {
		final GenOptions options = new GenOptions(args[1]);
		boolean parsingScript = false; // Verifica se deve fazer primeiro o parsing do script para depois gerar o Back e o Front !!
		boolean hasFrontBaseDefined = false;
		boolean hasRouteDefined = false;

		// Caso de USO DO TESTE deve-se criar o script de TESTE
		if( args[0].equalsIgnoreCase("-test") ) {
			gerarArquivoTeste();
			args[0] = "-mf";
		}

		// Caso tenha usado um Script que modela a partir de um arquivo.
		if( args[0].equalsIgnoreCase("-modelFile") || args[0].equalsIgnoreCase("-mf") ) {
			String pathFile = args[1];
			if( Files.isRegularFile(Paths.get(pathFile), LinkOption.NOFOLLOW_LINKS) ) {
				options.modelFile = pathFile;
				options.generateModelScript();
			} else {
				throw new IOException("Arquivo passado não encontrado !");
			}
		}

		// for (String param : args) {
		for(int i = 2; i < args.length; i++) {
			String param = args[i];

			////////////////////////////////////////////////////
			// OPTIONS DE GERAÇÃO
			if(param.toLowerCase().startsWith("-r") && param.length() > 2) {
				options.defaultRoute    = param.substring(2);
				if(hasFrontBaseDefined == false) {
					options.frontBaseFolder = param.substring(2);
					options.frontBaseName   = options.getFrontNameFrom( param.substring(2) );
				}
				hasFrontBaseDefined = true;
				hasRouteDefined = true;

			} else if(param.toLowerCase().startsWith("-ea") && param.length() > 3) {
				options.accessAlias = param.substring(3);
			} else if(param.toLowerCase().startsWith("-et") && param.length() > 3) {
				options.entityTableName = param.substring(3);
			} else if(param.toLowerCase().startsWith("-module") && param.length() > 7) {
				options.frontModuleName = param.substring(7);
			} else if(param.toLowerCase().startsWith("-front-base") && param.length() > 11) {
				options.frontBaseFolder = param.substring(11);
				options.frontBaseName   = options.getFrontNameFrom( param.substring(11) );
				hasFrontBaseDefined = true;

			////////////////////////////////////////////////////
			// FLAGS DE GERAÇÃO
			} else if(param.equalsIgnoreCase("-nogeneratemodel") || param.equalsIgnoreCase("-nogm") )
				options.generateModel = false;
			else if(param.equalsIgnoreCase("-generateempresaentity") || param.equalsIgnoreCase("-genemp") )
				options.generateEmpresaEntity = true;
			else if(param.equalsIgnoreCase("-generateresourceservice") || param.equalsIgnoreCase("-genserv") )
				options.generateResourceService = true;
			else if(param.equalsIgnoreCase("-onlyBackEnd") || param.equalsIgnoreCase("-back") )
				options.onlyBackEnd = true;
			else if(param.equalsIgnoreCase("-onlyFrontEnd") || param.equalsIgnoreCase("-front") )
				options.onlyFrontEnd = true;
			else if(param.equalsIgnoreCase("-onlyModel") || param.equalsIgnoreCase("-model") )
				options.onlyModel = true;
			else if(param.equalsIgnoreCase("-fullFrontEnd") || param.equalsIgnoreCase("-ffe") )
				options.fullFrontEnd = true;
			else if(param.equalsIgnoreCase("-parseScript") || param.equalsIgnoreCase("-ps") )
				parsingScript = true;
			else if(param.equalsIgnoreCase("-auditionMode") || param.equalsIgnoreCase("-audit") )
				Utils.aditionModeOn();
		}

		if(options.getModelGenerator() != null) {
			ModelGenerator mg = options.getModelGenerator();

			options.entityName      = mg.getClassName();
			options.entityTableName = mg.getTableName();
			if(hasRouteDefined == false)
				options.defaultRoute    = mg.getDefaultRoute();

			if(hasFrontBaseDefined == false) {
				options.frontBaseFolder = mg.getDefaultRoute();
				options.frontBaseName   = options.getFrontNameFrom( options.defaultRoute );
			}

			if(options.accessAlias == null) {
				options.accessAlias = options.entityTableName.toUpperCase();
			}
		}

		if(options.accessAlias == null) {
			options.accessAlias = "ALIAS_PADRAO";
		}

		//--------------------------------- PARSING DE SCRIPT ---------------------------------//
		if(parsingScript == true) {
			// Gera um novo MODEL SCRIPT para as opções passadas !!
			new ParseScript().gerarArquivos(options);
		}

		System.out.println( options.toString() );
		List<IGerador> geradores = Arrays.asList(
				new GerarBackEnd(),
				new GerarFrontEnd()
		);

		for( IGerador ger : geradores ) {
			ger.gerarArquivos(options);
			System.out.println();
		}
	}

	private static void gerarArquivoTeste() {
		try {
			InputStream resource = GeradorDeEntidades.class.getClassLoader().getResourceAsStream("test-script.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(resource) );

			PrintStream arquivo = new PrintStream("test-script.txt");

			String line = "";
			while( (line = br.readLine()) != null ) {
				arquivo.println(line);
			}

			arquivo.close();
			br.close();

		} catch (Exception e) {
			throw new RuntimeException("Erro na geração do ARQUIVO DE TESTE", e);
		}
	}
}
