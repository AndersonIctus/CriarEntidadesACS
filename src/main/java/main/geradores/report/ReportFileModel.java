package main.geradores.report;

import java.util.List;

public class ReportFileModel {
    private String name;
    private String title;
    private String reportType;
    private ReportRole role;
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

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public ReportRole getRole() {
        return role;
    }

    public void setRole(ReportRole role) {
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
        for(ReportProperty prop: properties) {
            prop.normalizeValues();
        }
    }

    @Override
    public String toString() {
        String out = "###### REPORT MODEL ######\r\n" +
                "## name = '" + name + "'\r\n" +
                "## title = '" + title + "'\r\n" +
                "## reportType = '" + reportType + "'\r\n" +
                "## role = " + role + "\r\n" +
                "#### ATRIBUTES ####\r\n";

        for(ReportProperty prop: properties) {
            out += "## " + prop + "\r\n";
        }

        out += "####################\n";
        return out;
    }

    public static class ReportRole {
        private Integer code;
        private String description;

        //region // ----------- GETTERs and SETTERs ----------- //
        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
        //endregion

        @Override
        public String toString() {
            return "'" + description + "' [" + code + "]";
        }
    }

    public static class ReportProperty {
        private String name;
        private String type;
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

            if(front == null) {
                front = new ReportFileModel.ReportProperty.PropertyFrontValue();
            }

            if(front.getLabel() == null)
                front.setLabel(name);
            if(front.getType() == null)
                front.setTypeByProperty(type);
        }

        @Override
        public String toString() {
            return name + " [" + type + ", " + value + ", " + required + "] " + front;
        }

        public static class PropertyFrontValue {
            private String label;
            private String type;

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
            //endregion

            public void setTypeByProperty(String propType) {
                if(propType.equalsIgnoreCase("AcsDateTime")) {
                    this.type = "DATE";
                } else if(propType.equalsIgnoreCase("Boolean")) {
                    this.type = "CHECKBOX";
                } else {
                    this.type = "INPUT";
                }
            }

            @Override
            public String toString() {
                return "('" + label +"', '" + type + "')";
            }


        }
    }
}
