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

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = (required == null)? false : required;
        }
        //endregion

        public void normalizeValues() {
            setType(type);
            setRequired(required);
        }

        @Override
        public String toString() {
            return name + " [" + type + ", " + value + ", " + required + "]";
        }
    }
}
