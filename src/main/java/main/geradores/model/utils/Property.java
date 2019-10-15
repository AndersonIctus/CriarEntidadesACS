package main.geradores.model.utils;

public class Property implements Comparable<Property> {
	private String name;
	private String variableName;
	private PropertyType type = PropertyType.NUMERO;
	private int inteiro = 0;
	private int decimal = 0;
	private boolean isPrimaryKey = false;
	private boolean isNullable = true;
	private String comentario = "";

	public Property(MyMatching mat) {
		// Name 
		this.name = mat.get(0).toLowerCase();
		this.variableName = normalizaPropertieName(name);

		// Type
		setType( mat.get(1) );

		// Outros possiveis tokens !!
		for(int i = 2; i < mat.size(); i++) {
			String token = mat.get(i);

			if(token.matches("\\([0-9, ]+\\)")) { // Um tamanho !
				defineTamanho(token);
			} else if(token.matches("NOT NULL")) { // Verifica se pode ser NOT NULL
				this.isNullable = false;
			} else if(token.matches("--[()\\w\\s\\u00C0-\\u00FF,-]+")) { // Verifica se houve algum comentário
				this.comentario = token;
			} else {
				throw new RuntimeException("Houve um problema na geração de um TOKEN ("+token+") na propriedade da linha : '" + mat.getLine() + "'");
			}
		}
	}

	public Property(String name, PropertyType propertyType, int inteiro, int decimal, boolean nullable, boolean primaryKey, String comentario) {
		this.name = name;
		this.variableName = normalizaPropertieName(name);

		setType(propertyType);
		this.inteiro = inteiro;
		this.decimal = decimal;
		this.isNullable = nullable;
		this.isPrimaryKey = primaryKey;
		setComentario(comentario);
	}

	private String normalizaPropertieName(String name) {
		String names[] = name.split("_");
		String nameNormalizado = "";

		for (int i = 0; i < names.length; i++) {
			if(i == 0) {
				nameNormalizado += names[i];
			} else {
				nameNormalizado += names[i].substring(0, 1).toUpperCase() + names[i].substring(1);
			}
		}

		return nameNormalizado;
	}

	private void defineTamanho(String token) {
		token = token.replaceAll("[\\(\\) ]+", ""); // retira os parentesis !

		if( this.type == PropertyType.DECIMAL ) {
			String[] sizes = token.split(",");
			inteiro = Integer.parseInt(sizes[0]);
			decimal = Integer.parseInt(sizes[1]);
		} else if( this.type == PropertyType.CHAR ) {
			inteiro = Integer.parseInt(token);
			if(inteiro > 1) type = PropertyType.STRING;
		} else {
			inteiro = Integer.parseInt(token);
		}
	}

	public void setType(String type) {
		if( type.equals("VARCHAR") ) {
			this.type = PropertyType.STRING;
		} else if( type.equals("INTEGER") ) {
			this.type = PropertyType.NUMERO;
		} else if( type.equals("NUMERIC") ) {
			this.type = PropertyType.DECIMAL;
		} else if( type.equals("CHAR") ) {
			this.type = PropertyType.CHAR;
		} else if( type.equals("SMALLINT") ) {
			this.type = PropertyType.SHORT;
		} else if( type.equals("BIGINT") ) {
			this.type = PropertyType.LONG;
		} else if( type.equals("DATE") ) {
			this.type = PropertyType.DATE;
		} else if( type.equals("TIMESTAMP") ) {
			this.type = PropertyType.TIMESTAMP;
		} else {
			throw new RuntimeException("Ocorreu um erro ao tentar mapear a Property do tipo '" + type + "'. Ele não é um tipo valido!");
		}
	}

	public void setType(PropertyType type) {
		this.type = type;
	}

	public void setPrimaryKey(boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public String getName() {
		return name;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public PropertyType getType() {
		return type;
	}

	public int getInteiro() {
		return inteiro;
	}

	public int getDecimal() {
		return decimal;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public boolean isNullable() {
		return isNullable;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	@Override
	public int hashCode() {
		return name.hashCode() + type.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Property) {
			Property oTher = (Property)obj;
			return this.name.equals(oTher.getName());
		}

		return false;
	}

	@Override
	public String toString() {
		return name + "(" + variableName + "): " + type + "["+inteiro + "," + decimal + "] {primary: " + isPrimaryKey + ", nullable: " + isNullable + "}" + ((comentario.equals("") == false)? "(" + comentario + ")" : "");
	}

	@Override
	public int compareTo(Property o) {
		if(this.isPrimaryKey == true && o.isPrimaryKey == false) {
			return -1;
		} else if( this.isPrimaryKey == false && o.isPrimaryKey == true) {
			return 1;
		}

		if(this.type == o.type) {
			return this.name.compareTo(o.name);
		} else {
			return this.type.compareTo(o.type);
		}
	}
}
