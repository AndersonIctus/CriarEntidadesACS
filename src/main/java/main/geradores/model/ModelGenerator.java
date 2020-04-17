package main.geradores.model;

import main.geradores.model.utils.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Classe para poder ler um arquivo e criar um MODELO para ser escrito no BACK ou no FRONT
 *
 * @author Usuário
 */
public class ModelGenerator {
    private String tableName;
    private String className;
    private String defaultRoute;
    private SortedSet<Property> lsProperties;
    private List<Constraint> lsConstraints;

    public ModelGenerator(String pathToFile) throws IOException {
        List<MyMatching> lsMatch = Files.lines(Paths.get(pathToFile))
                .map(line -> {
                    String[] patterns = Arrays.asList(
                            "CREATE [Tt][Aa][Bb][Ll][Ee][\\s]+[Pp][Uu][Bb][Ll][Ii][Cc].",
                            "CREATE [Tt][Aa][Bb][Ll][Ee]",
                            "CREATE [Ii][Nn][Dd][Ee][Xx]",
                            "CREATE SEQUENCE ([\\w\\s\\n\\.])+",           // SEQUENCE
                            "CACHE [0-9\\s]+;",                            // FINAL SEQUENCE
                            "ALTER TABLE [A-Za-z0-9_]+ ADD CONSTRAINT",    // CONSTRAINT (do alter table)
                            "ALTER [Tt][Aa][Bb][Ll][Ee]",                  // ALTER TABLE
                            "NOT NULL",
                            "UNIQUE",
                            "FOREIGN KEY",
                            "PRIMARY KEY",
                            "REFERENCES[\\s]+[Pp][Uu][Bb][Ll][Ii][Cc].",
                            "ONLY[\\s]+[Pp][Uu][Bb][Ll][Ii][Cc].",
                            "ADD CONSTRAINT",                     // CONSTRAINT (do alter table)
                            "CONSTRAINT",                         // CONSTRAINT
                            "[0-9]+",                             // NUMBER
                            "\\([[A-Z0-9_]+\\s,]+\\)",            // PARAMETER
                            "[A-Za-z0-9_]+",                      // ALPHA NUMBER
                            "--[()\\w\\s\\u00C0-\\u00FF,-]+",     // COMMENT
                            "\\s+",                               // SPACE
                            "//.*"
                    ).toArray(new String[54 - 38]);

                    Matcher match = Pattern.compile(
                            String.join("|", patterns)
                    ).matcher(line.trim());

                    return match.find() ? new MyMatching(line, match) : null;
                })
                .filter(matcher -> matcher != null)
                .collect(Collectors.toList());

        if (lsMatch.size() == 0) throw new RuntimeException("Arquivo não é um script de CRIAÇÃO DE TABELA");

//		System.out.println("-------------- MATCHERS ---------------");
//		lsMatch.stream()
//				.forEach( matching -> {
//					System.out.print( matching.toString() );
//					System.out.println("##########");
//				});
//		System.out.println("---------------------------------------");

        // 2 - Criar as bases que MAPEIA o MODEL que deve ser criado !
        System.out.println("## Faz transformacao");
        doTransformation(lsMatch);
        System.out.println(this);
    }

    private void doTransformation(List<MyMatching> matches) {
        if (this.lsProperties == null)
            this.lsProperties = new TreeSet<>();

        if (this.lsConstraints == null)
            this.lsConstraints = new ArrayList<>();

        boolean sequence = false;

        // Tranforma cada MATCH numa parte do Modelo
        for (MyMatching mat : matches) {
            String matType = mat.get(0);

            // Linhas de Sequence
            if (sequence) {
                if (matType.contains(";")) {
                    sequence = false;
                }
                continue;
            }

            if (matType.startsWith("CREATE TABLE")) { // NOME DA TABLE
                addTableName(mat);
            } else if (matType.equals("CONSTRAINT")) { // Varias CONSTRAINTS
                addConstraint(mat);
            } else if (matType.equals("ADD CONSTRAINT")) { // Varias CONSTRAINTS DO ALTER TABLE
                addConstraint(mat);
            } else if (matType.equals("CREATE INDEX")) { // Varias CONSTRAINTS
                addIndex(mat);
            } else if (matType.startsWith("CREATE SEQUENCE")) { // LENDO O SEQUENCE (QUE DEVE SER IGNORADO)
                sequence = true;
                continue;
            } else if (matType.startsWith("--")) { // COMENTARIOS SÃO IGNORADOS
                continue; //
            } else if (matType.startsWith("ALTER TABLE")) { // ALTER TABLE TAMBEM É IGNORADO NA LEITURA !!
                if (mat.size() > 2 && mat.get(0).contains("ADD CONSTRAINT")) {
                    addConstraint(mat);
                } else {
                    continue;
                }
            } else { // Nova PROPRIEDADE !
                Property property = new Property(mat);
                this.lsProperties.add(property);
            }
        }

        /////////////////////////////////////////////////////////////////////////////////////
        normalizaConstraints();
        normalizaDateTimeFields();
        normalizaCamposDeAuditoria();
        /////////////////////////////////////////////////////////////////////////////////////
    }

    private void addIndex(MyMatching mat) {
        if (mat.size() > 4) {
            return;
        }

        Constraint constraint = Constraint.createIndex(mat.get(1), mat.get(3), mat.get(4));
        this.lsConstraints.add(constraint);
    }

    private void addConstraint(MyMatching mat) {
        Constraint constraint = new Constraint(mat);
        this.lsConstraints.add(constraint);
    }

    // Adicionando valores !!
    private void addTableName(MyMatching mat) {
        if (tableName != null)
            throw new RuntimeException("O arquivo está errado, pois tem duas definições de CREATE TABLE !");

        this.tableName = mat.get(1).toLowerCase();
        this.className = ClasseNormalizada.normalizeClassName(mat.get(1));
        this.defaultRoute = normalizaRotaPadrao(this.tableName);
    }

    // NORMALIZA a lista de constraints (PRIMARY KEYS and FOREIGN KEYS)
    private void normalizaConstraints() {
        System.out.println("## normalizando as constraints !!");
        for (int i = lsConstraints.size() - 1; i >= 0; i--) {
            Constraint c = lsConstraints.get(i);

            // Chave Primaria
            if (c.getType() == ConstraintType.PRIMARY_KEY) {
                for (String pk : c.getKeys()) {
                    String propName = pk.toLowerCase();
                    Property prop = getPropertyByName(propName);
                    if (prop == null) {
                        throw new RuntimeException("Erro encontrado ao tentar eleger a chave primaria '" + pk + "'. Verifique se as CONSTRAINTS de Primary Key mapeiam corretamente as variáveis do SCRIPT que foi passado !");
                    }

                    lsProperties.remove(prop);
                    prop.setPrimaryKey(true); // Para incluir uma propriedade que é PRIMARIA !
                    this.lsProperties.add(prop);
                }

                lsConstraints.remove(c);

                // Chave Estrangeira
            } else if (c.getType() == ConstraintType.FOREIGN_KEY) {
                for (String pk : c.getKeys()) {
                    String propName = pk.toLowerCase();
                    Property prop = getPropertyByName(propName);
                    if (prop == null) {
                        throw new RuntimeException("Erro encontrado ao tentar eleger o JOIN '" + pk + "'. Verifique se as CONSTRAINTS de Foreign Key mapeiam corretamente as variáveis do SCRIPT que foi passado !");
                    }

                    lsProperties.remove(prop);
                    if (c.getReference().getKeys().length > 1) { // Verifica se a variável faz parte de um JOIN COMPOSTO !
                        prop.setType(PropertyType.JOIN_COMPOSTO); // Retira propriedades com o mesmo nome da variavel !!

                        // Verifica nas Referencias as outras keys para setar elas para JOIN_COMPOSTO_CHAVE
                        //     isso indica ao front que essa propriedade não deve ser escrita
                        //     caso a variavel já seja vinda de um JOIN, então não faz nada, pois chaves que tem o JOIN são variaveis !
                        for (String comp_chave : c.getReference().getKeys()) {
                            if (!comp_chave.equals(prop.getName())) {
                                Property compProp = getPropertyByName(comp_chave);
                                if (compProp == null)
                                    continue; // Se não achar a KEY da Referencia na Lista de PROPERTIES, então não precisa fazer mais nada !

                                if (compProp.getType() != PropertyType.JOIN) {
                                    this.lsProperties.remove(compProp);
                                    compProp.setType(PropertyType.JOIN_COMPOSTO_CHAVE);
                                    this.lsProperties.add(compProp);
                                }
                            }
                        }

                    } else {
                        prop.setType(PropertyType.JOIN);
                    }
                    this.lsProperties.add(prop);
                }
            }
        }
    }

    // NORMALIZA os campos de DATE TIME que vem com o offset
    private void normalizaDateTimeFields() {
        // 1 - Pega todos os campos date time
        Set<Property> lsDates = lsProperties.stream()
                .filter(ele -> ele.getType().equals(PropertyType.TIMESTAMP) || ele.getType().equals(PropertyType.DATE) )
                .collect(Collectors.toSet());

        // 2 - Procura campos offset similares
        List<AcsDateProperty> lsAcsDates = lsDates.stream()
                .map(ele -> {
                    Property offsetProp = lsProperties.stream()
                            .filter(prop -> {
                                final String name = prop.getName().toLowerCase();
                                return name.startsWith("offset_") && name.endsWith(ele.getName().toLowerCase());
                            })
                            .findAny().orElse(null);

                    if (offsetProp == null) return null;
                    return new AcsDateProperty(ele, offsetProp);
                })
                .filter(ele -> ele != null)
                .collect(Collectors.toList());

        // 3 - Deve remover os dois campos e COLOCAR com um novo tipo chamado ACS_DATE_TIME
        //  na transformacao esse campo vai se tornar um campo que vale a esses dois campos q serão removidos. Sera um campo composto !!
        //  as propriedades desse campo deverá ser a mesma contida no DATE_TIME apagado !
        for (AcsDateProperty acsProp : lsAcsDates) {
            lsProperties.remove(acsProp.getDate());
            lsProperties.remove(acsProp.getOffset());

            Property acsProperty = acsProp.toProperty();
            lsProperties.add(acsProperty);
        }
    }

    // NORMALIZA os campos de AUDITORIA que devem ser ignorados quando forem criados no back e no front
    private void normalizaCamposDeAuditoria() {
        List<String> camposAuditoria = Arrays.asList("criado_em", "alterado_em");

        // Procura pelos campos de Auditoria e troca o tipo para AUDITORIA
        for (Property prop : lsProperties) {
            if(camposAuditoria.contains(prop.getName())) {
                prop.setType(PropertyType.AUDITORIA);
            }
        }
    }

    private String normalizaRotaPadrao(String tableName) {
        return tableName.replaceAll("_", "-");
    }

    //region // -------------------- GETs AND SETTERs -------------------- //
    public Reference getReferenceFromProperty(Property prop) {
        Constraint c = getConstraintByKey(prop.getName());
        return c.getReference();
    }

    public Reference getJoinReferenceFromProperty(Property prop) {
        Constraint c = getJoinConstraintByKey(prop.getName());
        return c.getReference();
    }

    public String getTableName() {
        return tableName;
    }

    public String getClassName() {
        return this.className;
    }

    public String getDefaultRoute() {
        return this.defaultRoute;
    }

    public SortedSet<Property> getProperties() {
        return lsProperties;
    }

    public Property getPropertyByName(String propName) {
        for (Property prop : lsProperties) {
            if (prop.getName().equals(propName))
                return prop;
        }

        return null;
    }

    public Constraint getConstraintByKey(String key) {
        for (Constraint con : lsConstraints) {
            if (con.hasKey(key)) {
                return con;
            }
        }

        return null;
    }

    public Constraint getJoinConstraintByKey(String key) {
        for (Constraint con : lsConstraints) {
            if (con.getType() == ConstraintType.FOREIGN_KEY) {
                if (con.hasKey(key)) {
                    return con;
                }
            }
        }

        return null;
    }

    public List<Constraint> getConstraints() {
        return lsConstraints;
    }
    //endregion

    @Override
    public String toString() {
        String out = "*****************************\r\n" +
                "** tableName => " + tableName + "\r\n" +
                "** className => " + className + "\r\n" +
                "** route     => " + defaultRoute + "\r\n";

        out += "********* PROPERTIES ********\r\n";
        if (lsProperties.size() > 0) {
            for (Property prop : lsProperties) {
                out += "** " + prop + "\r\n";
            }
        } else {
            out += "** \r\n";
        }

        out += "******** CONSTRAINTS ********\r\n";
        if (lsConstraints.size() > 0) {
            for (Constraint cntr : lsConstraints) {
                out += "** " + cntr + "\r\n";
            }
        } else {
            out += "** \r\n";
        }

        out += "*****************************";
        return out;
    }

    class AcsDateProperty {
        private Property date;
        private Property offset;

        public AcsDateProperty(Property date, Property offset) {
            this.date = date;
            this.offset = offset;
        }

        public Property toProperty() {
            Property property = new Property(date.getName(), PropertyType.ACS_DATE_TIME, 0, 0, date.isNullable(), false, date.getComentario());
            return property;
        }

        //////////////////////////////////////////////////////////////////////////////////
        public Property getDate() {
            return date;
        }

        public void setDate(Property date) {
            this.date = date;
        }

        public Property getOffset() {
            return offset;
        }

        public void setOffset(Property offset) {
            this.offset = offset;
        }
    }
}
