package main.geradores.report;

import java.util.*;
import java.util.stream.Collectors;

public class ReportFileModel {
    private String name;
    private String title;
    private String domain;
    private String role;
    private List<ReportProperty> properties;

    //region // ----------- GETTERs and SETTERs ----------- //
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<ReportProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<ReportProperty> properties) {
        this.properties = properties;
    }
    //endregion

    public void normalizeProperties() {
        properties = properties.stream()
                .filter(Objects::nonNull)
                .map( prop -> {
                    prop.normalizeValues();
                    return prop;
                }).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String out = "###### REPORT MODEL ######\r\n" +
                "## name = '" + name + "'\r\n" +
                "## title = '" + title + "'\r\n" +
                "## domain = '" + domain + "'\r\n" +
                "## role = " + role + "\r\n" +
                "#### ATRIBUTES ####\r\n";

        for(ReportProperty prop: properties) {
            out += "## " + prop + "\r\n";
        }

        out += "####################\n";
        return out;
    }

    public static class ReportProperty {
        private String name;
        private String type;
        private String entity;
        private String value;
        private Boolean required;
        private PropertyFrontValue front;

        //region // ----------- GETTERs and SETTERs ----------- //
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = (type == null)? "Integer" : type;
        }

        public String getEntity() {
            return entity;
        }

        public void setEntity(String entity) {
            this.entity = entity;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Boolean isRequired() {
            return (required == null)? false: required;
        }

        public void setRequired(Boolean required) {
            this.required = (required == null)? false : required;
        }

        public PropertyFrontValue getFront() {
            return front;
        }

        public void setFront(PropertyFrontValue front) {
            this.front = front;
        }
        //endregion

        public void normalizeValues() {
            setType(type);
            setRequired(required);

            if(entity == null) {
                if(type.equalsIgnoreCase("SEARCH")) {
                    if(name.startsWith("id")) { // Espera-se que os campos search iniciaem com 'id'
                        setEntity(name.substring(2)); // pula o id
                    } else {
                        setEntity(name); // O proprio nome é o grupo !
                    }
                } else {
                    setEntity(type);
                }
            }

            if(front == null) {
                front = new ReportFileModel.ReportProperty.PropertyFrontValue();
            }

            if(front.getLabel() == null)
                front.setLabel(name);
            if(front.getType() == null)
                front.setTypeByProperty(type);
            if(front.getGroup() == null)
                front.setGroup(entity.substring(0, 1).toLowerCase() + entity.substring(1));
            if(front.getInteiro() == null)
                front.setInteiro(0);
            if(front.getDecimal() == null)
                front.setDecimal(0);
            if(front.getZerosLeft() == null)
                front.setZerosLeft(false);
            if(front.getOptions() == null)
                front.setOptions(new HashMap<>());
        }

        @Override
        public String toString() {
            return name + ": " + entity + " [" + type + ", " + value + ", " + required + "] " + front;
        }

        public static class PropertyFrontValue {
            private String label;
            private String type;
            private String group; // Usado para tipos SEARCH
            private Integer inteiro = 0; // Usado para numericos
            private Integer decimal = 0; // Usado para numericos
            private Boolean zerosLeft = false; // Usado para numericos
            private Map<String, String> options;

            //region // ----------- GETTERs and SETTERs ----------- //
            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getGroup() {
                return group;
            }

            public void setGroup(String group) {
                this.group = group;
            }

            public Integer getInteiro() {
                return inteiro;
            }

            public void setInteiro(Integer inteiro) {
                this.inteiro = inteiro;
            }

            public Integer getDecimal() {
                return decimal;
            }

            public void setDecimal(Integer decimal) {
                this.decimal = decimal;
            }

            public Boolean getZerosLeft() {
                return zerosLeft;
            }

            public void setZerosLeft(Boolean zerosLeft) {
                this.zerosLeft = zerosLeft;
            }

            public Map<String, String> getOptions() {
                return options;
            }

            public void setOptions(Map<String, String> options) {
                this.options = options;
            }
            //endregion

            public void setTypeByProperty(String propType) {
                if(propType.equalsIgnoreCase("AcsDateTime")) {
                    this.type = "DATE";
                } else if(propType.equalsIgnoreCase("Boolean")) {
                    this.type = "CHECKBOX";
                } else if(propType.equalsIgnoreCase("SEARCH")) {
                    this.type = "SEARCH";
                } else if(propType.equalsIgnoreCase("FILTER")) {
                    this.type = "FILTER";
                } else if(propType.equalsIgnoreCase("BigDecimal")) {
                    this.type = "DECIMAL";
                } else {
                    this.type = "INPUT";
                }
            }

            @Override
            public String toString() {
                String out = "('" + label + "', '" + type.toUpperCase() + "', '" + group + "') [" + inteiro + ", " + decimal + ", " + zerosLeft + "]";
                if(options.size() > 0) {
                    out+= "\r\n             options: " + options.toString();
                }
                return out;
            }
        }
    }
}
