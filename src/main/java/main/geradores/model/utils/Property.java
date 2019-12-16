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
        setType(mat.get(1));

        // Outros possiveis tokens !!
        for (int i = 2; i < mat.size(); i++) {
            String token = mat.get(i);

            if (token.matches("\\([0-9, ]+\\)")) { // Um tamanho !
                defineTamanho(token);
            } else if (token.matches("NOT NULL")) { // Verifica se pode ser NOT NULL
                this.isNullable = false;
            } else if (token.matches("--[()\\w\\s\\u00C0-\\u00FF,-]+")) { // Verifica se houve algum comentário
                this.comentario = token;
            } else if (token.matches("DEFAULT")) { // INICIALIZACAO DEFAULT (simplesmente IGNORA)
                continue;
            } else if (token.matches("NOW")) { // INICIALIZACAO NOW (simplesmente IGNORA)
                continue;
            } else {
                throw new RuntimeException("Houve um problema na geração de um TOKEN (" + token + ") na propriedade da linha : '" + mat.getLine() + "'");
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
        String[] names = name.split("_");
        StringBuilder nameNormalizado = new StringBuilder();

        for (int i = 0; i < names.length; i++) {
            if (i == 0) {
                nameNormalizado.append(names[i]);
            } else {
                nameNormalizado.append(names[i].substring(0, 1).toUpperCase()).append(names[i].substring(1));
            }
        }

        return nameNormalizado.toString();
    }

    private void defineTamanho(String token) {
        token = token.replaceAll("[\\(\\) ]+", ""); // retira os parentesis !

        if (this.type == PropertyType.DECIMAL) {
            String[] sizes = token.split(",");
            inteiro = Integer.parseInt(sizes[0]);
            decimal = Integer.parseInt(sizes[1]);
        } else if (this.type == PropertyType.CHAR) {
            inteiro = Integer.parseInt(token);
            if (inteiro > 1) type = PropertyType.STRING;
        } else {
            inteiro = Integer.parseInt(token);
        }
    }

    public void setType(String type) {
        switch (type) {
            case "VARCHAR":
                this.type = PropertyType.STRING;
                break;
            case "INTEGER":
                this.type = PropertyType.NUMERO;
                break;
            case "NUMERIC":
                this.type = PropertyType.DECIMAL;
                break;
            case "CHAR":
                this.type = PropertyType.CHAR;
                break;
            case "SMALLINT":
                this.type = PropertyType.SHORT;
                break;
            case "TIMESTAMP":
                this.type = PropertyType.TIMESTAMP;
                break;
            case "BIGINT":
                this.type = PropertyType.LONG;
                break;
            case "DATE":
                this.type = PropertyType.DATE;
                break;
            case "TEXT":
                this.type = PropertyType.TEXT;
                break;
            default:
                throw new RuntimeException("Ocorreu um erro ao tentar mapear a Property do tipo '" + type + "'. Ele não é um tipo valido!");
        }
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    //region // --------------- GET AND SET --------------- //
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

    public void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
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
    //endregion

    //region // ---------- IMPLEMENTATION OBJECT ---------- //
    @Override
    public int hashCode() {
        return name.hashCode() + type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Property) {
            Property oTher = (Property) obj;
            return this.name.equals(oTher.getName());
        }

        return false;
    }

    @Override
    public String toString() {
        return name + "(" + variableName + "): " + type + "[" + inteiro + "," + decimal + "] {primary: " + isPrimaryKey + ", nullable: " + isNullable + "}" + ((!comentario.equals("")) ? "(" + comentario + ")" : "");
    }

    @Override
    public int compareTo(Property o) {
        if (this.isPrimaryKey && !o.isPrimaryKey) {
            return -1;
        } else if (!this.isPrimaryKey && o.isPrimaryKey) {
            return 1;
        }

        if (this.type == o.type) {
            return this.name.compareTo(o.name);
        } else {
            return this.type.compareTo(o.type);
        }
    }
    //endregion
}
