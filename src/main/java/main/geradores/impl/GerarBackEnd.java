package main.geradores.impl;

import main.geradores.GenOptions;
import main.geradores.IGerador;
import main.geradores.Utils;
import main.geradores.model.ModelGenerator;
import main.geradores.model.utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GerarBackEnd implements IGerador {
    private static String mainPath = ".\\src\\main\\java\\com\\innovaro\\acs\\";

    @Override
    public void gerarArquivos(GenOptions options) throws IOException {
        System.out.println("===============================================");
        System.out.println("============ GERANDO BACK END =================");
        if (options.mainBack != null) {
            mainPath = options.mainBack;
        }
        else {
            options.mainBack = mainPath;
        }

        if (options.onlyFrontEnd) {
            System.out.println("Pulando a geração dos arquivos para o BackEnd ...");
        } else {
            if (options.generateModel) {
                gerarModelo(options);
                incluirViewClass(options);
            }

            if (!options.onlyModel) {
                gerarFiltro(options);
                gerarRepositoryQuery(options);
                gerarRepositoryImpl(options);
                gerarRepository(options);

                gerarResource(options);
            }
        }

        System.out.println();
        System.out.println("===============================================");
        System.out.println("===============================================");
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    // ############################### MODELS #######################################
    private void gerarModelo(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "model\\";

        String classBody = "";

        if (options.getModelGenerator() != null) {
            classBody = getModelClassBodyFromModelGenerator(options);

        }
        else {
            boolean pkClass = options.generateEmpresaEntity;
            if (pkClass)
                this.gerarClassPK(options);

            if (!options.generateModel) {
                System.out.println("Não foi gerado modelo para '" + options.entityName + "'");
                return;
            }

            classBody =
                    "package com.innovaro.acs.model;\r\n" +
                            "\r\n" +
                            ((pkClass)
                                    ? "import com.innovaro.acs.model.embeddedid." + options.entityName + "PK; \r\n"
                                    : "") +
                            "import org.hibernate.annotations.Fetch;\r\n" +
                            "import org.hibernate.annotations.FetchMode;\r\n" +
                            "\r\n" +
                            "import javax.persistence.*;\r\n" +
                            "\r\n" +
                            "/** ********************************************** \r\n" +
                            " * Classe criada AUTOMATICAMENTE a partir do programa 'CriarEntidadesACS'\r\n" +
                            " ** ********************************************** */\r\n" +
                            "@Entity\r\n" +
                            "@Table(name=\"" + options.entityTableName + "\")\r\n" +
                            ((pkClass)
                                    ? "@IdClass(" + options.entityName + "PK.class)\r\n"
                                    : "") +
                            "public class " + options.entityName + " extends AbstractModel {\r\n" +
                            "\r\n" +
                            "	@Id @GeneratedValue(strategy=GenerationType.TABLE)\r\n" +
                            "	private Integer id" + options.entityName + ";\r\n" +
                            "\r\n" +
                            "	//################ JOINS ################\r\n" +
                            ((pkClass)
                                    ? "	@Id @ManyToOne(fetch=FetchType.LAZY)\r\n" +
                                    "	@Fetch(FetchMode.JOIN)\r\n" +
                                    "	@JoinColumn(name=\"id_empresa\", foreignKey=@ForeignKey(name=\"FK_EMPRESA_" + options.entityName.toUpperCase() + "\"), nullable=false)\r\n" +
                                    "	private Empresa empresa;\r\n"
                                    : "") +
                            "\r\n" +
                            "	//################ PROPERTIES ################\r\n" +
                            "	@Column(name=\"descricao\", length = 25, nullable = false) private String descricao;\r\n" +
                            "\r\n" +
                            "	//###################################################\r\n" +
                            "	//################ GETTS AND SETTERS ################\r\n" +
                            "	public Integer getId" + options.entityName + "() {\r\n" +
                            "		return id" + options.entityName + ";\r\n" +
                            "	}\r\n" +
                            "\r\n" +
                            "	public void setId" + options.entityName + "(Integer id" + options.entityName + ") {\r\n" +
                            "		this.id" + options.entityName + " = id" + options.entityName + ";\r\n" +
                            "	}\r\n" +
                            "\r\n" +
                            ((pkClass)
                                    ? "	public Empresa getEmpresa() {\r\n" +
                                    "		return empresa;\r\n" +
                                    "	}\r\n" +
                                    "\r\n" +
                                    "	public void setEmpresa(Empresa empresa) {\r\n" +
                                    "		this.empresa = empresa;\r\n" +
                                    "	}\r\n" +
                                    "\r\n"
                                    : "") +
                            "	public String getDescricao() {\r\n" +
                            "		return descricao;\r\n" +
                            "	}\r\n" +
                            "\r\n" +
                            "	public void setDescricao(String descricao) {\r\n" +
                            "		this.descricao = descricao;\r\n" +
                            "	}\r\n" +
                            "}\r\n" +
                            "";
        }

        Utils.writeContentTo(path + options.entityName + ".java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private String getModelClassBodyFromModelGenerator(GenOptions options) throws IOException {
        ModelGenerator modelGen = options.getModelGenerator();

        String imports = "";
        String tableDefinitions = "";
        String pks = "";
        String pkClassComposto = "";

        String joins = "";
        String properties = "";
        // String getterAndSetter = "";
        String typeDefAcsDate = "";

        imports = // Imports Gerais !!
                "import com.fasterxml.jackson.annotation.JsonView;\r\n" +
                "import com.innovaro.acs.model.jsonviews.Views;\r\n" +
                "import lombok.Data;\r\n" +
                "import lombok.EqualsAndHashCode;\r\n" +
                "import org.hibernate.annotations.Fetch;\r\n" +
                "import org.hibernate.annotations.FetchMode;\r\n" +
                "\r\n" +
                "import java.util.Calendar;\r\n" +
                "import java.math.BigDecimal;\r\n" +
                "import javax.persistence.*;\r\n";

        /////////////////////////////////////////////////////////////////////////////////
        //
        tableDefinitions = // Table definitions Gerais !!
                "name = \"" + options.entityTableName + "\"";

        //
        String uniqueRes = modelGen.getConstraints().stream()
                .filter(con -> con.getType() == ConstraintType.UNIQUE )
                .map(con -> {
                    String uniqColumns = "";
                    for (String column : con.getKeys()) {
                        if (!uniqColumns.equals("")) uniqColumns += ", ";
                        uniqColumns += "\"" + column + "\"";
                    }

                    return "\t\t" + "@UniqueConstraint(columnNames = { " + uniqColumns + " })";
                })
                .reduce((lineA, lineB) -> lineA + ",\r\n" + lineB )
                .orElse("");

        if (!uniqueRes.equals("")) {
            tableDefinitions += ",\r\n" +
                    "\t" + "uniqueConstraints = {\r\n" +
                    uniqueRes + "\r\n" +
                    "\t}";
        }

        //
        String indexRes = modelGen.getConstraints().stream()
                .filter(con -> con.getType() == ConstraintType.INDEX )
                .map(con -> {
                    String indexColumns = "";
                    for (String column : con.getKeys()) {
                        if (!indexColumns.equals("")) indexColumns += ", ";
                        indexColumns += "\"" + column + "\"";
                    }
                    return "\t\t" + "@Index(name = \"" + con.getName() + "\", columnList = " + indexColumns + ")";
                })
                .reduce((lineA, lineB) -> lineA + ",\r\n" + lineB )
                .orElse("");

        if (!indexRes.equals("")) {
            tableDefinitions += ",\r\n" +
                    "\t" + "indexes = {\r\n" +
                    indexRes + "\r\n" +
                    "\t}";
        }

        // ---- Saber se há algum AcsDateTime nas propriedades
        long count = modelGen.getProperties().stream().filter(ele -> ele.getType().equals(PropertyType.ACS_DATE_TIME)).count();
        if (count != 0) {
            // import do ACs Date TIME !!
            imports =
                    "import com.innovaro.acs.repository.customtypes.AcsDateTime;\r\n" +
                    "import com.innovaro.acs.repository.customtypes.AcsDateTimeType;\r\n" +
                    "import org.hibernate.annotations.Columns;\r\n" +
                    imports +
                    "import org.hibernate.annotations.TypeDef;\r\n" +
                    "";

            typeDefAcsDate += "@TypeDef(name = \"AcsDateTime\", typeClass = AcsDateTimeType.class, defaultForType = AcsDateTime.class)\r\n";
        }

        //
        if (tableDefinitions.contains("uniqueConstraints") || tableDefinitions.contains("indexes")) {
            tableDefinitions += "\r\n";
        }

        /////////////////////////////////////////////////////////////////////////////////
        String jsonViewRef = "\t@JsonView(Views." + options.entityName + "View.class)\r\n";
        for (Property prop : modelGen.getProperties()) {
            // String typeMethod = "";
            if (prop.getType() == PropertyType.JOIN || prop.getType() == PropertyType.JOIN_COMPOSTO) { // joins
                Constraint con = modelGen.getJoinConstraintByKey(prop.getName());
                Reference ref = con.getReference();

                /////////////////////////////
                if (prop.isPrimaryKey()) {
                    joins += "\t" + "@Id\r\n";
                    options.addKey(prop);
                }

                joins += "\t" + "@ManyToOne(fetch=FetchType.LAZY)\r\n" +
                         "\t" + "@Fetch(FetchMode.JOIN)\r\n" +
                        jsonViewRef;

                String classVariableName = "";
                /////////////////////////////
                // JOIN SIMPLES
                if (prop.getType() == PropertyType.JOIN) {
                    if (con.getKeys()[0].equals(ref.getKeys()[0])) {
                        joins += "\t" + "@JoinColumn(name=\"" + con.getKeys()[0] + "\"" + ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n";
                        classVariableName = ref.getClassVariableName();

                    }
                    else {
                        joins += "\t" + "@JoinColumn(name=\"" + con.getKeys()[0] + "\"" + ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n";
                        classVariableName = ref.getFormatVariableName(prop.getName()); // retirar o id_ do nome da variavel !
                    }

                    // JOIN COMPOSTO
                }
                else {
                    String joinsColumns = "";
                    for (int i = 0; i < con.getKeys().length; i++) {
                        String varName = con.getKeys()[i];
                        String refName = ref.getKeys()[i];

                        if (!joinsColumns.equals("")) joinsColumns += ",\r\n";
                        joinsColumns += "\t\t" + "@JoinColumn(name=\"" + varName + "\", referencedColumnName = \"" + refName + "\", nullable = false)";
                    }

                    joins += "\t" + "@JoinColumns(foreignKey=@ForeignKey(name=\"" + con.getName() + "\"), value = {\r\n" +
                            joinsColumns + "\r\n" +
                            "\t" + "})\r\n";

                    classVariableName = ref.getClassVariableName();
                }

                joins += "\t" + "private " + ref.getClassName() + " " + classVariableName + ";\r\n\r\n";
                /////////////////////////////

                prop.setVariableName(classVariableName);
            }
            // JOINS COMPOSTO DE CHAVE não são representados como variáveis ou chave, somente sendo representdo no próprio BANCO !
            else if (prop.getType() == PropertyType.JOIN_COMPOSTO_CHAVE) { // joins compostos de chave
                continue;
            }
            else if (prop.isPrimaryKey()) { // pks
                String propLine = "\t" + "@Column(name=\"" + prop.getName();
                switch (prop.getType()) {
                    case STRING:
                    case CHAR:
                        propLine =
                                "\t@Id \r\n " +
                                        propLine +
                                        "\", length = " + prop.getInteiro() + ", nullable = false)\r\n" +
                                        "\t" + "private String ";
                        break;

                    default:
                    case NUMERO:
                        propLine =
                                "\t" + "@Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator=\"" + modelGen.getTableName() + "\")\r\n" +
                                        "\t" + "@SequenceGenerator(name=\"" + modelGen.getTableName() + "\", sequenceName=\"" + modelGen.getTableName() + "_seq\", allocationSize=1)\r\n" +
                                        "\t" + "@Column(name=\"" + prop.getName() + "\")\r\n" +
                                        "\t" + "private Integer ";
                        break;
                }

                pks += propLine + prop.getVariableName() + ";\r\n";
                options.addKey(prop); //adiciona para um possivel PK COMPOSTO !

            }
            else { // other properties
                String propLine = jsonViewRef + "\t@Column(name=\"" + prop.getName() + "\"";
                switch (prop.getType()) {
                    case STRING:
                    case CHAR:
                        if (prop.getInteiro() > 1) {
                            propLine += ", length = " + prop.getInteiro() + ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                    "\tprivate String ";
                        }
                        else if (prop.getInteiro() == 0) { // String sem tamanho definido
                            propLine += ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                    "\tprivate String ";
                        }
                        else {
                            propLine += ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                    "\tprivate Character ";
                        }
                        break;

                    case DATE:
                        propLine = "@Temporal(TemporalType.DATE) " + propLine + ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                "\tprivate Calendar ";
                        break;

                    case TIMESTAMP:
                        propLine = "@Temporal(TemporalType.TIMESTAMP) " + propLine + ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                "\tprivate Calendar ";
                        break;

                    case ACS_DATE_TIME:
                        propLine = "@Columns(columns = {" +
                                "@Column(name = \"offset_" + prop.getName() + "\"" + ((!prop.isNullable()) ? ", nullable = false" : "") + ")," +
                                "@Column(name = \"" + prop.getName() + "\"" + ((!prop.isNullable()) ? ", nullable = false" : "") + ") " +
                                "})\r\n" +
                                "\tprivate AcsDateTime ";
                        break;

                    case DECIMAL:
                        propLine += ", columnDefinition=\"numeric(" + prop.getInteiro() + "," + prop.getDecimal() + ")\"" + ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                "\tprivate BigDecimal ";
                        break;

                    case SHORT:
                        propLine += ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                "\tprivate Short ";
                        break;

                    case LONG:
                        propLine += ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                "\tprivate Long ";
                        break;

                    case TEXT:
                        propLine += ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                "\tprivate String ";
                        break;

                    case AUDITORIA:
                        // Propriedades de AUDITORIA devem ser ignoradas !!
                        continue;

                    default:
                    case NUMERO:
                        propLine += ((!prop.isNullable()) ? ", nullable = false" : "") + ")\r\n" +
                                "\tprivate Integer ";
                        break;
                }

                properties += propLine + prop.getVariableName() + ";\r\n\r\n";
            }
        }

        /////////////////////////////////////////////////////////////////////////////////

        if (options.isComposto()) {
            pkClassComposto = "@IdClass( " + options.entityName + "PK.class )\r\n";
            gerarClassPKFromList(options);

            imports += "import com.innovaro.acs.model.embeddedid." + options.entityName + "PK;\r\n";
        }

        /////////////////////////////////////////////////////////////////////////////////
        String classBody = "package com.innovaro.acs.model;\r\n" +
                "\r\n" +
                imports +
                "\r\n" +
                "/** ********************************************** \r\n" +
                " * Classe criada AUTOMATICAMENTE a partir do programa 'CriarEntidadesACS'\r\n" +
                " ** ********************************************** */\r\n" +
                "@Entity @Data @EqualsAndHashCode(callSuper = true)\r\n" +
                "@Table(" + tableDefinitions + ")\r\n" +
                ((!pkClassComposto.equals("")) ? pkClassComposto : "") +
                typeDefAcsDate +
                "public class " + options.entityName + " extends AbstractModel {\r\n" +
                pks +
                "\r\n" +
                ((!joins.equals(""))
                        ? "	//region ################ JOINS ################\r\n" +
                        joins +
                        "\t//endregion\r\n\r\n"
                        : "") +
                "	//region ################ PROPERTIES ################\r\n" +
                properties +
                " \t//endregion" + "\r\n" +
                "\r\n" +
                "     @Override" + "\r\n" +
                "     public Class<?> getViewJson() { " + "\r\n" +
                "          return Views." + options.entityName + "View.class; " + "\r\n" +
                "     } " + "\r\n" +
                "} \r\n" +
                "";

        return classBody;
    }

    private void gerarClassPKFromList(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "model\\embeddedid\\";

        String properties = "";
        String constructors = "";
        String constructorsAssign = "";
        String constructorsProperties = "";
        String getterAndSetter = "";
        String equalsReturn = "";
        String hashCodes = "";

        constructors += "\tpublic " + options.entityName + "PK() {  }\r\n\r\n";// construtor default !
        constructors += "\tpublic " + options.entityName + "PK(";

        /////////////////////////////////////////////////////////////////////////////////////////////

        for (Property prop : options.getOptionsKeys()) {
            if (!constructorsAssign.equals("")) constructorsAssign += ", ";
            if (!hashCodes.equals("")) hashCodes += ", ";
            if (!equalsReturn.equals("")) equalsReturn += " &&\r\n\t\t\t";

            String typeMethod = "";
            switch (prop.getType()) {
                case NUMERO:
                case SHORT:
                case LONG:
                case JOIN:
                case JOIN_COMPOSTO:
                default:
                    properties += "\t" + "private Integer " + prop.getVariableName() + ";\r\n";
                    typeMethod = "Integer";
                    constructorsAssign += "Integer " + prop.getVariableName();
                    break;

                case STRING:
                case CHAR:
                    properties += "\t" + "private String " + prop.getVariableName() + ";\r\n";
                    typeMethod = "String";
                    constructorsAssign += "String " + prop.getVariableName();
                    break;
            }

            hashCodes += prop.getVariableName();
            constructorsProperties += "\t\t" + "this." + prop.getVariableName() + " = " + prop.getVariableName() + ";\r\n";
            equalsReturn += "Objects.equals(" + prop.getVariableName() + ", model." + prop.getVariableName() + ")";
            getterAndSetter += parseToGetAndSet(typeMethod, prop.getVariableName()) + "\r\n";
        }

        constructors += constructorsAssign + ") {\r\n" +
                constructorsProperties +
                "\t}\r\n";

        /////////////////////////////////////////////////////////////////////////////////////////////

        String classBody =
                "package com.innovaro.acs.model.embeddedid;\r\n" +
                        "\r\n" +
                        "import java.io.Serializable;\r\n" +
                        "import java.util.Objects;\r\n" +
                        "\r\n" +

                        "public class " + options.entityName + "PK implements Serializable {\r\n" +
                        properties +
                        "\r\n" +
                        constructors +
                        "\r\n" +
                        getterAndSetter +
                        "\t" + "@Override\r\n" +
                        "\t" + "public boolean equals(Object o) {\r\n" +
                        "\t\t" + "if (this == o) return true;\r\n" +
                        "\t\t" + "if (o == null || getClass() != o.getClass()) return false;\r\n" +
                        "\t\t" + options.entityName + "PK model = (" + options.entityName + "PK) o;\r\n" +
                        "\t\t" + "return " + equalsReturn + ";\r\n" +
                        "\t" + "}\r\n" +
                        "\r\n" +
                        "\t" + "@Override\r\n" +
                        "\t" + "public int hashCode() {\r\n" +
                        "\t\t" + "return Objects.hash(" + hashCodes + ");\r\n" +
                        "\t" + "}\r\n" +
                        "}";

        Utils.writeContentTo(path + options.entityName + "PK.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "PK' into '" + path + "'");
    }

    private String parseToGetAndSet(String typeMethod, String variableName) {
        String methodName = variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
        return //GET
                "\t" + "public " + typeMethod + " get" + methodName + "() {\r\n" +
                        "\t\t" + "return " + variableName + ";\r\n" +
                        "\t" + "}\r\n" +
                        "\r\n" +
                        //SET
                        "\t" + "public void set" + methodName + "(" + typeMethod + " " + variableName + ") {\r\n" +
                        "\t\t" + "this." + variableName + " = " + variableName + ";\r\n" +
                        "\t" + "}\r\n";
    }

    private String parseJoinToPredicate(String variableName) {
        String methodName = "Id" + variableName.substring(0, 1).toUpperCase() + variableName.substring(1);

        return "\t\t" + "if( this.get" + methodName + "() != null) {\r\n" +
                "\t\t\t" + "predicates.add(\r\n" +
                "\t\t\t\t" + "builder.equal( root.get(\"" + variableName + "\").get(\"id\"), this.get" + methodName + "())\r\n" +
                "\t\t\t);\r\n" +
                "\t\t}\r\n";
    }

    private void gerarClassPK(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "model\\embeddedid\\";

        String classBody =
                "package com.innovaro.acs.model.embeddedid;\r\n" +
                        "\r\n" +
                        "import java.io.Serializable;\r\n" +
                        "import java.util.Objects;\r\n" +
                        "\r\n" +
                        "public class " + options.entityName + "PK implements Serializable {\r\n" +
                        "    private Integer id" + options.entityName + ";\r\n" +
                        "    private Integer empresa;\r\n" +
                        "\r\n" +
                        "    public " + options.entityName + "PK() {  }\r\n" +
                        "    public " + options.entityName + "PK(Integer id" + options.entityName + ", Integer empresa) {\r\n" +
                        "        this.id" + options.entityName + " = id" + options.entityName + ";\r\n" +
                        "        this.empresa = empresa;\r\n" +
                        "    }\r\n" +
                        "\r\n" +
                        "    public Integer getId" + options.entityName + "() {\r\n" +
                        "        return id" + options.entityName + ";\r\n" +
                        "    }\r\n" +
                        "\r\n" +
                        "    public void setId" + options.entityName + "(Integer id" + options.entityName + ") {\r\n" +
                        "        this.id" + options.entityName + " = id" + options.entityName + ";\r\n" +
                        "    }\r\n" +
                        "\r\n" +
                        "    public Integer getEmpresa() {\r\n" +
                        "        return empresa;\r\n" +
                        "    }\r\n" +
                        "\r\n" +
                        "    public void setEmpresa(Integer empresa) {\r\n" +
                        "        this.empresa = empresa;\r\n" +
                        "    }\r\n" +
                        "\r\n" +
                        "    @Override\r\n" +
                        "    public boolean equals(Object o) {\r\n" +
                        "        if (this == o) return true;\r\n" +
                        "        if (o == null || getClass() != o.getClass()) return false;\r\n" +
                        "        " + options.entityName + "PK model = (" + options.entityName + "PK) o;\r\n" +
                        "        return Objects.equals(id" + options.entityName + ", model.id" + options.entityName + ") &&\r\n" +
                        "                Objects.equals(empresa, model.empresa);\r\n" +
                        "    }\r\n" +
                        "\r\n" +
                        "    @Override\r\n" +
                        "    public int hashCode() {\r\n" +
                        "        return Objects.hash(id" + options.entityName + ", empresa);\r\n" +
                        "    }\r\n" +
                        "}";

        Utils.writeContentTo(path + options.entityName + "PK.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "PK' into '" + path + "'");
    }
    // ########################################################################

    private void gerarFiltro(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "repository\\filter\\";

        String properties = "";
        String fetch = "";
        String join = "";
        String predicates = "";

        ModelGenerator modelGen = options.getModelGenerator();
        for (Property prop : modelGen.getProperties()) {
            if (prop.getType() == PropertyType.JOIN) { // joins
                String varName = "id" + prop.getVariableName().substring(0, 1).toUpperCase() + prop.getVariableName().substring(1);

                properties += "\t" + "private Integer " + varName + ";\r\n";
                fetch += "\t\t\t" + "root.fetch(\"" + prop.getVariableName() + "\"" + ((prop.isNullable()) ? ", JoinType.LEFT" : "") + ");\r\n";
                join += "\t\t\t" + "root.join(\"" + prop.getVariableName() + "\"" + ((prop.isNullable()) ? ", JoinType.LEFT" : "") + ");\r\n";

                if (predicates.equals("") == false) predicates += "\r\n";
                predicates += parseJoinToPredicate(prop.getVariableName());
            }
        }

        String classBody = "package com.innovaro.acs.repository.filter;\r\n" +
                "\r\n" +
                "import java.util.List;\r\n" +
                "import java.util.ArrayList;\r\n" +
                "\r\n" +
                "import javax.persistence.criteria.CriteriaBuilder;\r\n" +
                "import javax.persistence.criteria.CriteriaQuery;\r\n" +
                "import javax.persistence.criteria.Predicate;\r\n" +
                "import javax.persistence.criteria.JoinType;\r\n" +
                "import javax.persistence.criteria.Root;\r\n" +
                "\r\n" +
                "import org.apache.commons.lang3.StringUtils;\r\n" +
                "\r\n" +
                "import com.innovaro.acs.model." + options.entityName + ";\r\n" +
                "import lombok.Data;\r\n" +
                "import lombok.EqualsAndHashCode;\r\n" +
                "\r\n" +
                "/** ********************************************** \r\n" +
                " * Classe criada AUTOMATICAMENTE a partir do programa 'CriarEntidadesACS'\r\n" +
                " ** ********************************************** */\r\n" +
                "@Data @EqualsAndHashCode(callSuper = false)\r\n" +
                "public class " + options.entityName + "Filter extends AbstractFilter<" + options.entityName + "> {\r\n" +
                "	//region ##################### PROPERTIES ############################\r\n" +
                properties +
                "\t//endregion" + "\r\n" +
                "\r\n" +

                "	//region ######################   M E T O D O S   D E   F I L T R A G E M   #########################\r\n" +
                "	@Override\r\n" +
                "	public Class<" + options.entityName + "> getDomainClass() {\r\n" +
                "		return " + options.entityName + ".class;\r\n" +
                "	}\r\n" +
                "\r\n" +
                "	@Override\r\n" +
                "	public Predicate[] createPredicates(CriteriaQuery<?> criteria, CriteriaBuilder builder, Root<" + options.entityName + "> root, FilterJoinMode fetch) {\r\n" +
                "		List<Predicate> predicates = new ArrayList<>();\r\n" +
                "		if(fetch == FilterJoinMode.FETCH) {\r\n" +
                fetch +
                "		} else if(fetch == FilterJoinMode.JOIN) {\r\n" +
                join +
                "		}\r\n" +
                "		\r\n" +

                "		// ------------------------- PREDICATES ------------------------- \r\n" +
                predicates +
                "		// -------------------------------------------------------------- \r\n" +
                "		\r\n" +
                "		// return super.orPredicates(builder, predicates);\r\n" +
                "		return predicates.toArray(new Predicate[predicates.size()]); // AND\r\n" +
                "	}\r\n" +
                "\t//endregion" + "\r\n" +
                "}\r\n";

        Utils.writeContentTo(path + options.entityName + "Filter.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "Filter' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private void gerarRepositoryQuery(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "repository\\impl\\";

        String classBody = "package com.innovaro.acs.repository.impl;\r\n" +
                "\r\n" +
                "import com.innovaro.acs.model." + options.entityName + ";\r\n" +
                "import com.innovaro.acs.repository.filter." + options.entityName + "Filter;\r\n" +
                "\r\n" +
                "/** ********************************************** \r\n" +
                " * Classe criada AUTOMATICAMENTE a partir do programa 'CriarEntidadesACS'\r\n" +
                " ** ********************************************** */\r\n" +
                "public interface " + options.entityName + "RepositoryQuery extends IRepositoryQuery<" + options.entityName + ", " + options.entityName + "Filter> {\r\n" +
                "\r\n" +
                "}";

        Utils.writeContentTo(path + options.entityName + "RepositoryQuery.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "RepositoryQuery' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private void gerarRepositoryImpl(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "repository\\impl\\";

        String classBody = "package com.innovaro.acs.repository.impl;\r\n" +
                "\r\n" +
                "import com.innovaro.acs.model." + options.entityName + ";\r\n" +
                "import com.innovaro.acs.repository.filter." + options.entityName + "Filter;\r\n" +
                "\r\n" +
                "\r\n" +
                "/** ********************************************** \r\n" +
                " * Classe criada AUTOMATICAMENTE a partir do programa 'CriarEntidadesACS'\r\n" +
                " ** ********************************************** */\r\n" +
                "public class " + options.entityName + "RepositoryImpl extends AbstractRepositoryImpl<" + options.entityName + ", " + options.entityName + "Filter> implements " + options.entityName + "RepositoryQuery {\r\n" +
                "\r\n" +
                "}";

        Utils.writeContentTo(path + options.entityName + "RepositoryImpl.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "RepositoryImpl' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private void gerarRepository(GenOptions options) throws IOException {
        boolean pkClass = options.generateEmpresaEntity;

        String path = GerarBackEnd.mainPath + "repository\\";

        String jpaClassKey = "Integer";
        if (options.isComposto()) {
            jpaClassKey = options.entityName + "PK";
        }

        String classBody = "package com.innovaro.acs.repository;\r\n" +
                "\r\n" +
                "import com.innovaro.acs.model." + options.entityName + ";\r\n" +
                ((pkClass)
                        ? "import com.innovaro.acs.model.embeddedid." + options.entityName + "PK;\r\n"
                        : "") +
                "import com.innovaro.acs.repository.impl." + options.entityName + "RepositoryQuery;\r\n" +
                "import org.springframework.data.jpa.repository.JpaRepository;\r\n" +
                "\r\n" +
                "/** ********************************************** \r\n" +
                " * Classe criada AUTOMATICAMENTE a partir do programa 'CriarEntidadesACS'\r\n" +
                " ** ********************************************** */\r\n" +
                "public interface " + options.entityName + "Repository extends " + options.entityName + "RepositoryQuery, JpaRepository<" + options.entityName + ", " + jpaClassKey + "> {\r\n" +
                "\r\n" +
                "}";

        Utils.writeContentTo(path + options.entityName + "Repository.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "Repository' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private void gerarResource(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "resource\\";

        String classBody = "";

        String imports = "";
        String serviceVariable = "";

        if (options.getModelGenerator() != null) {
            classBody = getResourceClassBodyFromModelGenerator(options);

        }
        else {
            boolean pkClass = options.generateEmpresaEntity;
            boolean serviceClass = options.generateResourceService;
            if (serviceClass)
                this.gerarClassService(options);

            String create = getCreateResource(options);
            String update = getUpdateResource(options);
            String delete = getDeleteResource(options);
            String byId = getByIdResource(options);

            if (pkClass) {
                imports += "import com.innovaro.acs.model.embeddedid." + options.entityName + "PK;\r\n" +
                        "import com.innovaro.acs.model.Empresa;\r\n";

            } else if (options.isComposto()) {
                imports += "import com.innovaro.acs.model.embeddedid." + options.entityName + "PK;\r\n";
            }

            if (serviceClass) {
                imports += "import com.innovaro.acs.service." + options.entityName + "Service;\r\n";

                serviceVariable +=
                        "	@Autowired\r\n" +
                                "	private " + options.entityName + "Service service;\r\n\r\n";
            }

            //////////////////////////////////////////////////////////////////////////////////////////////

            classBody = "package com.innovaro.acs.resource;\r\n" +
                    "\r\n" +
                    "import java.util.Map;\r\n" +
                    "\r\n" +
                    "import javax.servlet.http.HttpServletResponse;\r\n" +
                    "import javax.transaction.Transactional;\r\n" +
                    "import javax.validation.Valid;\r\n" +
                    "\r\n" +
                    "import org.springframework.beans.BeanUtils;\r\n" +
                    "import org.springframework.beans.factory.annotation.Autowired;\r\n" +
                    "import org.springframework.boot.autoconfigure.EnableAutoConfiguration;\r\n" +
                    "import org.springframework.context.ApplicationEventPublisher;\r\n" +
                    "import org.springframework.data.domain.Page;\r\n" +
                    "import org.springframework.data.domain.Pageable;\r\n" +
                    "import org.springframework.http.HttpStatus;\r\n" +
                    "import org.springframework.http.MediaType;\r\n" +
                    "import org.springframework.http.ResponseEntity;\r\n" +
                    "import org.springframework.security.access.prepost.PreAuthorize;\r\n" +
                    "import org.springframework.web.bind.annotation.DeleteMapping;\r\n" +
                    "import org.springframework.web.bind.annotation.GetMapping;\r\n" +
                    "import org.springframework.web.bind.annotation.PathVariable;\r\n" +
                    "import org.springframework.web.bind.annotation.PostMapping;\r\n" +
                    "import org.springframework.web.bind.annotation.PutMapping;\r\n" +
                    "import org.springframework.web.bind.annotation.RequestBody;\r\n" +
                    "import org.springframework.web.bind.annotation.RequestMapping;\r\n" +
                    "import org.springframework.web.bind.annotation.ResponseStatus;\r\n" +
                    "import org.springframework.web.bind.annotation.RestController;\r\n" +
                    "\r\n" +
                    "import com.innovaro.acs.event.RecursoCriadoEvent;\r\n" +
                    "import com.innovaro.acs.model." + options.entityName + ";\r\n" +
                    "import com.innovaro.acs.repository." + options.entityName + "Repository;\r\n" +
                    "import com.innovaro.acs.repository.filter." + options.entityName + "Filter;\r\n" +
                    imports +
                    "\r\n" +

                    "/** ********************************************** \r\n" +
                    " * Classe criada AUTOMATICAMENTE a partir do programa 'CriarEntidadesACS'\r\n" +
                    " ** ********************************************** */\r\n" +
                    "@EnableAutoConfiguration\r\n" +
                    "@RestController\r\n" +
                    "@RequestMapping(\"/" + options.defaultRoute + "\")\r\n" +
                    "public class " + options.entityName + "Resource extends BaseResource<" + options.entityName + ", " + options.entityName + "Filter, " + options.entityName + "Repository> {\r\n" +
                    "	\r\n" +
                    serviceVariable +
                    "	// @Transactional\r\n" +
                    "	@GetMapping()\r\n" +
                    "	// @PreAuthorize(\"hasAuthority('ACESSAR " + options.accessAlias + "')\")\r\n" +
                    "	public Page<" + options.entityName + "> search(" + options.entityName + "Filter filter, Pageable pageable, String orderBy) {\r\n" +
                    "	    return super.search(filter, pageable, orderBy);\r\n" +
                    "	}\r\n" +
                    "   \r\n" +
                    "	// @Transactional\r\n" +
                    "	@GetMapping(value = {\"/max/{atributo}\"}, produces=MediaType.APPLICATION_JSON_VALUE)\r\n" +
                    "	// @PreAuthorize(\"hasAuthority('ACESSAR " + options.accessAlias + "')\")\r\n" +
                    "	public String max(@PathVariable String atributo, " + options.entityName + "Filter filter) {\r\n" +
                    "		return super.max(atributo, filter);\r\n" +
                    "	}\r\n" +
                    "   \r\n" +

                    // ******* LISTING *******
                    "	@Transactional\r\n" +
                    "	@GetMapping(\"/list\")\r\n" +
                    "	// @PreAuthorize(\"hasAuthority('ACESSAR " + options.accessAlias + "')\")\r\n" +
                    "	public Page<Map<String,?>> searchListar(" + options.entityName + "Filter filter, Pageable pageable) {\r\n" +
                    "		// filter.addToGroupBy(\"id" + options.entityName + "\");\r\n" +
                    "		\r\n" +
                    "		String[] projection = { \"id" + options.entityName + "\", \"descricao\" };\r\n" +
                    "		Page<Map<String,?>> pageProjectionMap = repository.getPageProjectionMapFor(projection, filter, pageable);\r\n" +
                    "		\r\n" +
                    "		return pageProjectionMap;\r\n" +
                    "	}\r\n" +
                    "   \r\n" +

                    // GET BY ID
                    byId +

                    "	\r\n" +
                    "	//region ==============================   C R U D   M E T H O D S   =========================================\r\n" +
                    "	// ====================================================================================================\r\n" +
                    // METODO CREATE
                    create +
                    "	\r\n" +

                    // METODO UPDATE
                    update +
                    "	\r\n" +

                    // METODO DELETE
                    delete +
                    "\t//endregion" + "\r\n" +
                    "}\r\n" +
                    "";
        }

        //////////////////////////////////////////////////////////////////////////////////////////////

        Utils.writeContentTo(path + options.entityName + "Resource.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "Resource' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private String getResourceClassBodyFromModelGenerator(GenOptions options) throws IOException {
        ModelGenerator modelGen = options.getModelGenerator();
        boolean serviceClass = options.generateResourceService;
        if (serviceClass)
            gerarClassServiceFromList(options);

        String imports = "";
        String properties = "";

        String listGroupBy = "";
        String listProjections = "";

        String byIdMapping = "";
        String byIdAssign = "";
        String modelFind = "";
        String modelFindVariables = "";

        String createModelSalvo = "";
        // String createModelVariables = "";
        String createMapping = "";
        String createAssign = "";
        String createGetId = "";

        String updateModelSalvo = "";
        // String updateModelVariables ="";
        String updateBeansCopy = "";

        // String deleteModelSalvo = "";

        //////////////////////////////////////////////////////////////////////////////////////////////////

        imports = // Imports Gerais !!
                "import java.util.Map;\r\n" +
                        "\r\n" +
                        "import javax.servlet.http.HttpServletResponse;\r\n" +
                        "import javax.transaction.Transactional;\r\n" +
                        "import javax.validation.Valid;\r\n" +
                        "\r\n" +
                        "import org.springframework.beans.factory.annotation.Autowired;\r\n" +
                        "import org.springframework.boot.autoconfigure.EnableAutoConfiguration;\r\n" +
                        "import org.springframework.data.domain.Page;\r\n" +
                        "import org.springframework.data.domain.Pageable;\r\n" +
                        "import org.springframework.http.HttpStatus;\r\n" +
                        "import org.springframework.http.MediaType;\r\n" +
                        "import org.springframework.http.ResponseEntity;\r\n" +
                        "import org.springframework.security.access.prepost.PreAuthorize;\r\n" +
                        "import org.springframework.web.bind.annotation.*;\r\n" +
                        "\r\n" +
                        "import com.innovaro.acs.event.RecursoCriadoEvent;\r\n" +
                        "import com.innovaro.acs.repository." + options.entityName + "Repository;\r\n" +
                        "import com.innovaro.acs.repository.filter." + options.entityName + "Filter;\r\n" +
                        "import com.innovaro.acs.service." + options.entityName + "Service;\r\n" +
                        "import com.innovaro.acs.model." + options.entityName + ";\r\n";

        //////////////////////////////////////////////////////////////////////////////////////////////////
        for (Property prop : options.getOptionsKeys()) {
            String methodName = prop.getVariableName().substring(0, 1).toUpperCase() + prop.getVariableName().substring(1);
            if (createGetId.equals("")) {
                createGetId = "get" + methodName + "()";
            }

            // Properties listing ...
            listGroupBy += "\t\t" + "// filter.addToGroupBy(\"" + prop.getVariableName() + "\");\r\n";

            if (prop.getType() == PropertyType.JOIN) {
                listProjections += "\"" + prop.getVariableName() + ".descricao\", ";
            } else {
                listProjections += "\"" + prop.getVariableName() + "\", ";
            }

            // Mapping informations
            if (byIdMapping.equals("") == false) {
                byIdMapping += "/" + prop.getVariableName() + "/{" + prop.getVariableName() + "}";
            } else {
                byIdMapping += "{" + prop.getVariableName() + "}";
            }

            if (byIdAssign.equals("") == false) {
                byIdAssign += ", ";
            }

            switch (prop.getType()) {
                case STRING:
                case CHAR:
                    byIdAssign += "@PathVariable String " + prop.getVariableName();
                    break;

                case JOIN:
                case JOIN_COMPOSTO:
                    createMapping += "/" + prop.getVariableName() + "/{" + prop.getVariableName() + "}";
                    createAssign += "@PathVariable Integer " + prop.getVariableName() + ", ";
                    // createModelVariables += prop.getVariableName() + ", ";
                default:
                    byIdAssign += "@PathVariable Integer " + prop.getVariableName();
                    // updateModelVariables += prop.getVariableName() + ", ";
                    break;
            }

            if (modelFindVariables.equals("") == false) {
                modelFindVariables += ", ";
            }
            modelFindVariables += prop.getVariableName();

            if (updateBeansCopy.equals("") == false) {
                updateBeansCopy += ", ";
            }
            updateBeansCopy += "\"" + prop.getVariableName() + "\"";

            if (prop.getVariableName().replaceAll("id", "").equalsIgnoreCase(options.entityName)) {
                createModelSalvo += "\t\t" + "model.set" + methodName + "(0);\r\n";
            } else {
                Constraint con = modelGen.getConstraintByKey(prop.getName());

                if (con != null) { // Se não tiver uma Constraint é que trata-se de uma chave que não tem o mesmo nome da ENTIDADE
                    String className = con.getReference().getClassName();
                    createModelSalvo += "\t\t" + "model.set" + className + "(new " + className + "(" + prop.getVariableName() + "));\r\n";

                    String novoImport = "import com.innovaro.acs.model." + className;
                    if (imports.contains(novoImport) == false) // Se acrescenta o import se ele já não existir !
                        imports += novoImport + ";\r\n";
                } else {
                    createModelSalvo += "\t\t" + "// variavel '" + prop.getVariableName() + "' não é autogerada! \r\n";
                }
            }
        }

        listProjections += "\"descricao\"";

        if (options.isComposto()) {
            modelFind = "new " + options.entityName + "PK(" + modelFindVariables + ")";
        } else {
            modelFind = modelFindVariables;
        }

        if (serviceClass) {
            createModelSalvo = "\t\t" + options.entityName + " modelSalvo = service.save(model); \r\n";
            updateModelSalvo = "\t\t" + options.entityName + " modelSalvo = service.update(id, model); \r\n";
        } else {
            createModelSalvo += "\t\t" + options.entityName + " modelSalvo = repository.save(model); \r\n";
            updateModelSalvo =
                    "\t\t" + options.entityName + " modelBanco = repository.findOne(" + modelFind + ");\r\n" +
                            "\r\n" +
                            "\t\t" + "BeanUtils.copyProperties(model, modelBanco, " + updateBeansCopy + ");\r\n";
        }

        if (options.isComposto()) {
            imports += "import com.innovaro.acs.model.embeddedid." + options.entityName + "PK;\r\n";
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////
        String classBody = "package com.innovaro.acs.resource;\r\n" +
                "\r\n" +
                imports +
                "import static com.innovaro.acs.security.AcsAuthConsts.HAS_AUTHORITY_ACESSAR_" + options.accessAlias + ";\r\n" +
                "import static com.innovaro.acs.security.AcsAuthConsts.HAS_AUTHORITY_INCLUIR_" + options.accessAlias + ";\r\n" +
                "import static com.innovaro.acs.security.AcsAuthConsts.HAS_AUTHORITY_ALTERAR_" + options.accessAlias + ";\r\n" +
                "\r\n" +
                "/** ********************************************** \r\n" +
                " * Classe criada AUTOMATICAMENTE a partir do programa 'CriarEntidadesACS'\r\n" +
                " ** ********************************************** */\r\n" +
                "@EnableAutoConfiguration\r\n" +
                "@RestController\r\n" +
                "@RequestMapping(\"/" + options.defaultRoute + "\")\r\n" +
                "public class " + options.entityName + "Resource extends BaseResource<" + options.entityName + ", " + options.entityName + "Filter, " + options.entityName + "Repository> {\r\n" +
                "\t" + "\r\n" +
                properties +
                "\t" + "@Autowired" + "\r\n" +
                "\t" + "private " + options.entityName + "Service service;" + "\r\n" +
                "\t" + "\r\n" +
                "\t" + "// @Transactional\r\n" +
                "\t" + "@GetMapping()\r\n" +
                "\t" + "// @PreAuthorize(HAS_AUTHORITY_ACESSAR_"+ options.accessAlias + ")\r\n" +
                "\t" + "public Page<" + options.entityName + "> search(" + options.entityName + "Filter filter, Pageable pageable, String orderBy) {\r\n" +
                "\t\t" + "return super.search(filter, pageable, orderBy);\r\n" +
                "\t" + "}\r\n" +
                "\r\n" +
                "\t" + "// @Transactional\r\n" +
                "\t" + "@GetMapping(value = {\"/max/{atributo}\"}, produces = MediaType.APPLICATION_JSON_VALUE)\r\n" +
                "\t" + "// @PreAuthorize(HAS_AUTHORITY_ACESSAR_"+ options.accessAlias + ")\r\n" +
                "\t" + "public String max(@PathVariable String atributo, " + options.entityName + "Filter filter) {\r\n" +
                "\t\t" + "return super.max(atributo, filter);\r\n" +
                "\t" + "}\r\n" +
                "\r\n" +

                // ******* LISTING *******
                "\t" + "@Transactional\r\n" +
                "\t" + "@GetMapping(\"/list\")\r\n" +
                "\t" + "// @PreAuthorize(HAS_AUTHORITY_ACESSAR_"+ options.accessAlias + ")\r\n" +
                "\t" + "public Page<Map<String,?>> searchListar(" + options.entityName + "Filter filter, Pageable pageable) {\r\n" +
                listGroupBy +
                "\t\t" + "\r\n" +
                "\t\t" + "String[] projection = { " + listProjections + " };\r\n" +
                "\t\t" + "Page<Map<String,?>> pageProjectionMap = repository.getPageProjectionMapFor(projection, filter, pageable);\r\n" +
                "\t\t" + "\r\n" +
                "\t\t" + "return pageProjectionMap;\r\n" +
                "\t" + "}\r\n" +
                "\r\n" +

                // ******* GET BY ID *******
                "\t" + "@GetMapping(\"/" + byIdMapping + "\")\r\n" +
                "\t" + "// @PreAuthorize(HAS_AUTHORITY_ACESSAR_"+ options.accessAlias + ")\r\n" +
                "\t" + "public ResponseEntity<" + options.entityName + "> findById(" + byIdAssign + ") {\r\n" +
                "\t\t" + "return ResponseEntity.ok().body(service.findOne(id));\r\n" +
                "\t" + "}\r\n" +

                "\t" + "\r\n" +
                "\t" + "//region ==============================   C R U D   M E T H O D S   =========================================\r\n" +
                "\t" + "// ====================================================================================================\r\n" +

                // -------------- METODO CREATE --------------
                "\t" + "@PostMapping(\"" + createMapping + "\")\r\n" +
                "\t" + "@PreAuthorize(HAS_AUTHORITY_INCLUIR_"+ options.accessAlias + ")\r\n" +
                "\t" + "public ResponseEntity<" + options.entityName + "> create(" + createAssign + "@Valid @RequestBody " + options.entityName + " model, HttpServletResponse response) {\r\n" +
                createModelSalvo +
                "\r\n" +
                "\t\t" + "publisher.publishEvent(new RecursoCriadoEvent(this, response, modelSalvo." + createGetId + " ));\r\n" +
                "\t\t" + "return ResponseEntity.status(HttpStatus.CREATED).body(modelSalvo);\r\n" +
                "\t" + "}\r\n" +
                "\t" + "\r\n" +

                // -------------- METODO UPDATE --------------
                "\t" + "@PutMapping(\"/" + byIdMapping + "\")\r\n" +
                "\t" + "@PreAuthorize(HAS_AUTHORITY_ALTERAR_"+ options.accessAlias + ")\r\n" +
                "\t" + "public ResponseEntity<" + options.entityName + "> update(" + byIdAssign + ", @Valid @RequestBody " + options.entityName + " model) {\r\n" +
                updateModelSalvo +
                "\r\n" +
                "\t\t" + "return ResponseEntity.ok(modelSalvo);\r\n" +
                "\t" + "}\r\n" +
                "\r\n" +

                // -------------- METODO DELETE --------------
                "\t" + "@DeleteMapping(\"/" + byIdMapping + "\")\r\n" +
                "\t" + "@ResponseStatus(HttpStatus.NO_CONTENT)\r\n" +
                "\t" + "@PreAuthorize(HAS_AUTHORITY_ALTERAR_"+ options.accessAlias + ")\r\n" +
                "\t" + "public void delete(" + byIdAssign + ") {\r\n" +
                "\t\t" + "service.delete(" + modelFind + ");\r\n" +
                "\t" + "}\r\n" +
                "\t//endregion" + "\r\n" +
                "}\r\n" +
                "";

        return classBody;
    }

    private String getByIdResource(GenOptions options) {
        boolean pkClass = options.generateEmpresaEntity;

        return ((pkClass)
                ? "	@GetMapping(\"/{id" + options.entityName + "}/empresa/{idEmpresa}\")\r\n" +
                "	// @PreAuthorize(\"hasAuthority('ACESSAR " + options.accessAlias + "')\")\r\n" +
                "	public ResponseEntity<" + options.entityName + "> findById(@PathVariable Integer id" + options.entityName + ", @PathVariable Integer idEmpresa) {\r\n" +
                "		" + options.entityName + " model = repository.findOne( new " + options.entityName + "PK(id" + options.entityName + ", idEmpresa) );\r\n" +
                "	    if(model == null) {\r\n" +
                "	        return ResponseEntity.notFound().build();\r\n" +
                "	    }\r\n" +
                "	    return ResponseEntity.ok().body(model);\r\n" +
                "	}\r\n"
                : "	@GetMapping(\"/{id" + options.entityName + "}\")\r\n" +
                "	// @PreAuthorize(\"hasAuthority('ACESSAR " + options.accessAlias + "')\")\r\n" +
                "	public ResponseEntity<" + options.entityName + "> findById(@PathVariable Integer id" + options.entityName + ") {\r\n" +
                "		" + options.entityName + " model = repository.findOne(id" + options.entityName + ");\r\n" +
                "	    if(model == null) {\r\n" +
                "	        return ResponseEntity.notFound().build();\r\n" +
                "	    }\r\n" +
                "	    return ResponseEntity.ok().body(model);\r\n" +
                "	}\r\n");
    }

    private String getCreateResource(GenOptions options) {
        boolean pkClass = options.generateEmpresaEntity;
        boolean serviceClass = options.generateResourceService;

        return ((pkClass)
                ? "	@PostMapping(\"/empresa/{idEmpresa}\")\r\n" +
                "	@PreAuthorize(\"hasAuthority('INCLUIR " + options.accessAlias + "')\")\r\n" +
                "	public ResponseEntity<" + options.entityName + "> create(@PathVariable Integer idEmpresa, @Valid @RequestBody " + options.entityName + " model, HttpServletResponse response) {\r\n" +
                ((serviceClass)
                        ? "		" + options.entityName + " modelSalvo = service.save(idEmpresa, model); \r\n"
                        : "		//?? TODO: Verificar regras de negocio que devem ser usados antes do SAVE !!\r\n" +
                        "		model.setId" + options.entityName + "( 0 );\r\n" +
                        "		Empresa empresa = new Empresa(idEmpresa);\r\n" +
                        "		model.setEmpresa(empresa);\r\n" +
                        "		\r\n" +
                        "		" + options.entityName + " modelSalvo = repository.save(model); \r\n") +
                "		publisher.publishEvent(new RecursoCriadoEvent(this, response, modelSalvo.getId" + options.entityName + "() ));\r\n" +
                "		return ResponseEntity.status(HttpStatus.CREATED).body(modelSalvo);\r\n" +
                "	}\r\n"
                : "	@PostMapping()\r\n" +
                "	@PreAuthorize(\"hasAuthority('INCLUIR " + options.accessAlias + "')\")\r\n" +
                "	public ResponseEntity<" + options.entityName + "> create(@Valid @RequestBody " + options.entityName + " model, HttpServletResponse response) {\r\n" +
                ((serviceClass)
                        ? "		" + options.entityName + " modelSalvo = service.save(model); \r\n"
                        : "		//?? TODO: Verificar regras de negocio que devem ser usados antes do SAVE !!\r\n" +
                        "		" + options.entityName + " modelSalvo = repository.save(model); \r\n") +
                "		publisher.publishEvent(new RecursoCriadoEvent(this, response, modelSalvo.getId" + options.entityName + "() ));\r\n" +
                "		return ResponseEntity.status(HttpStatus.CREATED).body(modelSalvo);\r\n" +
                "	}\r\n");
    }

    private String getUpdateResource(GenOptions options) {
        boolean pkClass = options.generateEmpresaEntity;
        boolean serviceClass = options.generateResourceService;

        return ((pkClass)
                ? "	@PutMapping(\"/{id" + options.entityName + "}/empresa/{idEmpresa}\")\r\n" +
                "	@PreAuthorize(\"hasAuthority('ALTERAR " + options.accessAlias + "')\")\r\n" +
                "	public ResponseEntity<" + options.entityName + "> update(@PathVariable Integer id" + options.entityName + ", @PathVariable Integer idEmpresa, @Valid @RequestBody " + options.entityName + " model) {\r\n" +
                ((serviceClass)
                        ? "		" + options.entityName + " modelSalvo = service.update(id" + options.entityName + ", idEmpresa, model); \r\n"
                        : "		//?? TODO: Verificar regras de negocio que devem ser usados antes do SAVE !!\r\n" +
                        "		" + options.entityName + " modelBanco = repository.findOne( new " + options.entityName + "PK(id" + options.entityName + ", idEmpresa) );\r\n" +
                        "		BeanUtils.copyProperties(model, modelBanco, \"id" + options.entityName + "\", \"empresa\");\r\n" +
                        "		" + options.entityName + " modelSalvo = repository.save(modelBanco);\r\n") +
                "		\r\n" +
                "		return ResponseEntity.ok(modelSalvo);\r\n" +
                "	}\r\n"
                : "	@PutMapping(\"/{id" + options.entityName + "}\")\r\n" +
                "	@PreAuthorize(\"hasAuthority('ALTERAR " + options.accessAlias + "')\")\r\n" +
                "	public ResponseEntity<" + options.entityName + "> update(@PathVariable Integer id" + options.entityName + ", @Valid @RequestBody " + options.entityName + " model) {\r\n" +
                ((serviceClass)
                        ? "		" + options.entityName + " modelSalvo = service.update(id" + options.entityName + ", model); \r\n"
                        : "		//?? TODO: Verificar regras de negocio que devem ser usados antes do SAVE !!\r\n" +
                        "		" + options.entityName + " modelBanco = repository.findOne(id" + options.entityName + ");\r\n" +
                        "		BeanUtils.copyProperties(model, modelBanco, \"id" + options.entityName + "\");\r\n" +
                        "		" + options.entityName + " modelSalvo = repository.save(modelBanco);\r\n") +
                "		\r\n" +
                "		return ResponseEntity.ok(modelSalvo);\r\n" +
                "	}\r\n");
    }

    private String getDeleteResource(GenOptions options) {
        boolean pkClass = options.generateEmpresaEntity;

        return ((pkClass)
                ? "	@DeleteMapping(\"/{id" + options.entityName + "}/empresa/{idEmpresa}\")\r\n" +
                "	@ResponseStatus(HttpStatus.NO_CONTENT)\r\n" +
                "	@PreAuthorize(\"hasAuthority('ALTERAR " + options.accessAlias + "')\")\r\n" +
                "	public void delete(@PathVariable Integer id" + options.entityName + ", @PathVariable Integer idEmpresa) {\r\n" +
                "		// repository.delete(new " + options.entityName + "PK(id" + options.entityName + ", idEmpresa) );//TODO: Forma de deletar da entidade " + options.entityName + "\r\n" +
                "	}\r\n"
                : "	@DeleteMapping(\"/{id" + options.entityName + "}\")\r\n" +
                "	@ResponseStatus(HttpStatus.NO_CONTENT)\r\n" +
                "	@PreAuthorize(\"hasAuthority('ALTERAR " + options.accessAlias + "')\")\r\n" +
                "	public void delete(@PathVariable Integer id" + options.entityName + ") {\r\n" +
                "		// repository.delete(id" + options.entityName + ");//TODO: Forma de deletar da entidade " + options.entityName + "\r\n" +
                "	}\r\n");
    }

    private void gerarClassService(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "service\\";

        String classBody = "package com.innovaro.acs.service;\r\n" +
                "\r\n" +
                "import com.innovaro.acs.exceptionhandler.exception.ACSBadRequestException;\r\n" +
                "import com.innovaro.acs.exceptionhandler.exception.ACSException;\r\n" +
                "import com.innovaro.acs.exceptionhandler.exception.ACSNotFoundException;\r\n" +
                "import com.innovaro.acs.model.Produto;\r\n" +
                "import com.innovaro.acs.repository.ProdutoRepository;\r\n" +
                "import org.apache.commons.lang3.ArrayUtils;\r\n" +
                "import org.apache.commons.lang3.StringUtils;\r\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\r\n" +
                "import org.springframework.stereotype.Service;\r\n" +
                "\r\n" +
                "@Service \r\n" +
                "public class " + options.entityName + "Service extends AbstractService<" + options.entityName + ", " + options.entityName + "Repository> {\r\n" +
                "\r\n" +
                "\t" + "@Override" + "\r\n" +
                "\t" + "public " + options.entityName + " findOne(Integer id) throws ACSNotFoundException {" + "\r\n" +
                "\t\t" + " " + options.entityName + " model = repository.findOne(id);" + "\r\n" +
                "\t\t" + "if (model == null) {" + "\r\n" +
                "\t\t\t" + "throw new ACSNotFoundException(" + options.entityTableName + ".nao.encontrado" + ");" + "\r\n" +
                "\t\t" + "}" + "\r\n" +
                "\t\t" + "return model;" + "\r\n" +
                "\t" + "}" + "\r\n" +
                "\r\n" +
                "\t" + "@Override" + "\r\n" +
                "\t" + "void validarDadosModel(" + options.entityName + " model) throws ACSException {" + "\r\n" +
                "\t" + "// Inserir os tratamentos de validação para SAVE e UPDATE " + "\r\n" +
                "\t" + "}" + "\r\n" +
                "\r\n" +
                "\t" + "@Override" + "\r\n" +
                "\t" + "String[] getUpdateIgnoredProperties() {" + "\r\n" +
                "\t\t" + "return ArrayUtils.toArray(\"id\");" + "\r\n" +
                "\t" + "}" + "\r\n" +
                "\r\n" +
                "}\r\n" +
                "";

        Utils.writeContentTo(path + options.entityName + "Service.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "Service' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private void gerarClassServiceFromList(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "service\\";

        String classBody = "package com.innovaro.acs.service;\r\n" +
                "\r\n" +
                "import com.innovaro.acs.exceptionhandler.exception.ACSBadRequestException;\r\n" +
                "import com.innovaro.acs.exceptionhandler.exception.ACSException;\r\n" +
                "import com.innovaro.acs.exceptionhandler.exception.ACSNotFoundException;\r\n" +
                "import com.innovaro.acs.model." + options.entityName + ";\r\n" +
                "import com.innovaro.acs.repository." + options.entityName + "Repository;\r\n" +
                "import org.apache.commons.lang3.ArrayUtils;\r\n" +
                "import org.apache.commons.lang3.StringUtils;\r\n" +
                "import org.springframework.beans.factory.annotation.Autowired;\r\n" +
                "import org.springframework.stereotype.Service;\r\n" +
                "\r\n" +
                "@Service \r\n" +
                "public class " + options.entityName + "Service extends AbstractService<" + options.entityName + ", " + options.entityName + "Repository> {\r\n" +
                "\r\n" +
                "\t" + "@Override" + "\r\n" +
                "\t" + "public " + options.entityName + " findOne(Integer id) throws ACSNotFoundException {" + "\r\n" +
                "\t\t" + options.entityName + " model = repository.findOne(id);" + "\r\n" +
                "\t\t" + "if (model == null) {" + "\r\n" +
                "\t\t\t" + "throw new ACSNotFoundException(\"" + options.defaultRoute + ".nao.encontrado" + "\");" + "\r\n" +
                "\t\t" + "}" + "\r\n" +
                "\t\t" + "return model;" + "\r\n" +
                "\t" + "}" + "\r\n" +
                "\r\n" +
                "\t" + "@Override" + "\r\n" +
                "\t" + "void validarDadosModel(" + options.entityName + " model) throws ACSException {" + "\r\n" +
                "\t\t" + "// Inserir os tratamentos de validação para SAVE e UPDATE " + "\r\n" +
                "\t" + "}" + "\r\n" +
                "\r\n" +
                "\t" + "@Override" + "\r\n" +
                "\t" + "String[] getUpdateIgnoredProperties() {" + "\r\n" +
                "\t\t" + "return ArrayUtils.toArray(\"id\");" + "\r\n" +
                "\t" + "}" + "\r\n" +
                "}\r\n" +
                "";

        Utils.writeContentTo(path + options.entityName + "Service.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "Service' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    // #############################################################################
    private void incluirViewClass(GenOptions options) throws IOException {
        String pathToFile = options.mainBack;

        pathToFile += "model/jsonviews/Views.java";
        // pathToFile = "./Views.java"; // REMOVER !!!

        String newLine = "    public static class " + options.entityName + "View extends AbstractClassView {}";
        String tmpFile = "./tmp.txt";

        if (Utils.isAuditionMode()) {
            System.out.println("PATH     => '" + pathToFile + "'");
            System.out.println("New LINE => '" + newLine + "'");
            System.out.println("-----------------------------------------------");
            return;
        }

        new File(tmpFile).createNewFile();

        // 1 - Inicia lendo o arquivo de cadastros.module
        Path readModule = Paths.get(pathToFile);
        List<String> lines = Files.readAllLines(readModule);
        boolean writted = false;
        String viewRef = options.entityName + "View";
        for (String line : lines) {
            if (!writted) {
                // 2 - Quando estiver lendo as LINHAS
                //   2.1 - Verifica a ordem pelo nome da View Referencia (viewRef)
                //   * Verificar se está no início ou no final da class !
                if (line.startsWith("    public static class ")) {
                    // 2.2 -> Verifica a Ordem alfabética
                    List<String> lsTokens = Arrays
                                                .stream(line.split("public static class|AbstractClassView \\{[ ]*\\}|extends|[ ]+"))
                                                .filter(ele -> !ele.equals("") )
                                            .collect(Collectors.toList());

                    if(lsTokens.size() != 0) {
                        int comp = viewRef.compareTo(lsTokens.get(0));
                        if (comp < 0) {
                            writted = true;
                            writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                        } else if (comp == 0) { // Se estiver repetindo (Mesmo nome do token)
                            writted = true;
                        }
                    }

                } else if (line.startsWith("}")) {
                    writted = true;
                    writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }

            writeToFile(Paths.get(tmpFile), (line + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

        Files.delete(readModule);
        new File(tmpFile).renameTo(new File(pathToFile));
    }

    private void writeToFile(Path path, byte[] bytes, StandardOpenOption... append) throws IOException {
        // System.out.print( new String(bytes) );
        Files.write(path, bytes, append);
    }
}
