package main.geradores.report.impl;

import main.geradores.GenOptions;
import main.geradores.IGerador;
import main.geradores.Utils;
import main.geradores.report.ReportFileModel;
import main.geradores.report.ReportGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class GerarReportBackEnd implements IGerador {
    private static String mainPath = ".\\src\\main\\java\\com\\innovaro\\acs\\modulo\\relatorio\\";

    @Override
    public void gerarArquivos(GenOptions options) throws IOException {
        System.out.println("===============================================");
        System.out.println("============ GERANDO BACK END =================");
        if (options.mainBack != null) {
            mainPath = options.mainBack + "modulo/relatorio/";
        }
        else {
            options.mainBack = mainPath;
        }

        if (options.onlyFrontEnd) {
            System.out.println("Pulando a geração dos arquivos para o BackEnd ...");
        }
        else {
            gerarReportFilter(options);

            includeConstDiretoriosRelatorios(options);
            includeResourceLine(options);
            includeServiceLine(options);
        }

        System.out.println();
        System.out.println("===============================================");
        System.out.println("===============================================");
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    private void gerarReportFilter(GenOptions options) throws IOException {
        String path = mainPath + "filter/";

        ReportGenerator reportGenerator = options.getReportGenerator();
        String imports =
                "import com.innovaro.acs.exceptionhandler.exception.ACSBadRequestException;\r\n" +
                "import com.innovaro.acs.exceptionhandler.exception.ACSNotFoundException;\r\n"+
                "import com.innovaro.acs.repository.customtypes.AcsDateTime;\r\n" +
                "import lombok.Data;\r\n" +
                "\r\n"+
                "import java.math.BigDecimal;\r\n" +
                "import java.time.OffsetDateTime;\r\n" +
                "import java.util.HashMap;\r\n" +
                "import java.util.List;\r\n" +
                "import java.util.Map;\r\n";

        String properties = "";
        String parametersRequired = "";
        String messageProperties = "";

        for(ReportFileModel.ReportProperty prop: reportGenerator.getReportModel().getProperties()) {
            properties += "    private " + getTypeByProperty(prop.getType()) + " " + prop.getName();
            if(!prop.isRequired()) {
                if(prop.getValue() != null) {
                    if(prop.getType().equalsIgnoreCase("String")) {
                        properties += " = \"" + prop.getValue() + "\"";
                    } else {
                        properties += " = " + prop.getValue();
                    }
                }
            }
            else {
                String methodName = prop.getName().substring(0, 1).toUpperCase() + prop.getName().substring(1);

                if(prop.getType().equalsIgnoreCase("String")) {
                    parametersRequired += "        if (StringUtils.isEmpty(this.get" + methodName + "())) {\r\n";
                } else {
                    parametersRequired += "        if (this.get" + methodName + "() == null) {\r\n";
                }

                parametersRequired +=
                        "            throw new ACSNotFoundException(\""+options.defaultRoute+"."+prop.getName()+".obrigatorio\");\r\n" +
                        "        }\r\n\r\n";

                messageProperties += "        " + options.defaultRoute + "." + prop.getName() + ".obrigatorio = O campo "+ prop.getFront().getLabel() + " é obrigatório.\r\n";
            }

            properties += ";\r\n";
        }

        messageProperties =
                "        /**  messages.properties \r\n" +
                "        ################### " + options.frontBaseName + "\r\n" +
                messageProperties +
                "        */\r\n";

        String classBody = "package com.innovaro.acs.modulo.relatorio.filter;\r\n" +
                "\r\n" +
                imports +
                "\r\n" +
                "/** ********************************************** \r\n" +
                " * Classe criada AUTOMATICAMENTE a partir do programa 'CriarEntidadesACS'\r\n" +
                " ** ********************************************** */\r\n" +
                "@Data\r\n" +
                "public class " + options.entityName + "FilterReport extends AbstractFilterReport {\r\n" +
                properties +
                "\r\n" +
                "    @Override\r\n" +
                "    protected void validarParametros() {\r\n" +
                parametersRequired +
                messageProperties +
                "    }\r\n" +
                "\r\n" +
                "    @Override\r\n" +
                "    protected Map<String, Object> getParametersFromFilter() {\r\n" +
                "        Map<String, Object> parameters = new HashMap<>();\r\n" +
                "        return parameters;\r\n" +
                "    }\r\n" +
                "}\r\n" +
                "";

        Utils.writeContentTo(path + options.entityName + "FilterReport.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "FilterReport' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private void includeConstDiretoriosRelatorios(GenOptions options) throws IOException {
        String pathToFile = options.mainBack;
        pathToFile += "modulo/relatorio/ConstDiretoriosRelatorios.java";

        ReportGenerator reportGenerator = options.getReportGenerator();
        String reportName = "RELATORIO_" + reportGenerator.getReportName().toUpperCase();
        String reportType = reportGenerator.getReportModel().getDomain();

        String newLine = "    String " + reportName + " = \"/report/" + reportType +
                         "/" + options.defaultRoute + "/" + options.defaultRoute + ".jasper\";";
        String tmpFile = "./tmp.txt";

        if (Utils.isAuditionMode()) {
            System.out.println("PATH     => '" + pathToFile + "'");
            System.out.println("New LINE => '\r\n" + newLine + "'");
            System.out.println("-----------------------------------------------");
            return;
        }

        new File(tmpFile).createNewFile();

        // 1 - Inicia lendo o arquivo de cadastros.module
        Path readModule = Paths.get(pathToFile);
        List<String> lines = Files.readAllLines(readModule);
        boolean writted = false;
        boolean iniciaBusca = false;

        for (String line : lines) {
            if (!writted) {
                if( line.startsWith("    //") ) {
                    if(line.contains(reportType.toUpperCase())) {
                        iniciaBusca = true;
                        writeToFile(Paths.get(tmpFile), (line + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                        continue;
                    }
                }

                if(iniciaBusca && line.contains("String")) {
                    // LE se a linha está na ordem alfabetica
                    int inicio = "    String ".length();
                    int fin = line.lastIndexOf(" =");

                    String token = line.substring(inicio, fin);

                    int comp = reportName.compareTo(token);
                    if (comp < 0) {
                        writted = true;
                        writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    } else if (comp == 0) { // Se estiver repetindo (Mesmo nome do token)
                        writted = true;
                    }
                }

                if (line.startsWith("}")) {
                    writted = true;
                    writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                } else if(iniciaBusca && line.trim().equals("") ) {
                    writted = true;
                    writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }

            writeToFile(Paths.get(tmpFile), (line + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

        Files.delete(readModule);
        new File(tmpFile).renameTo(new File(pathToFile));

        System.out.println("Generated a linha '" + newLine);
        System.out.println("Into '" + pathToFile + "'");
        System.out.println("-----------------------------------------------");
    }

    private void includeResourceLine(GenOptions options) throws IOException {
        String pathToFile = options.mainBack;
        pathToFile += "modulo/relatorio/resource/RelatorioResource.java";

        ReportGenerator reportGenerator = options.getReportGenerator();
        String reportDomain = reportGenerator.getReportModel().getDomain();
        String role = options.accessAlias.toUpperCase().replaceAll("\\.", "").replaceAll(" ", "_");
        String serviceName = "service";
        if(!reportDomain.equalsIgnoreCase("cadastro")) {
            serviceName = reportDomain + "Service";
        }

        String newLine =
                "    @GetMapping(\"/" + reportDomain + "/" + options.defaultRoute + "\")\r\n" +
                "    @PreAuthorize(HAS_AUTHORITY_"+role+")\r\n" +
                "    public ResponseEntity<Relatorio> report" + options.entityName + "(" + options.entityName + "FilterReport filter) {\r\n" +
                "        return ResponseEntity.ok().body(" + serviceName + ".gerarReport" + options.entityName + "(filter));\r\n" +
                "    }\r\n";
        String tmpFile = "./tmp.txt";

        if (Utils.isAuditionMode()) {
            System.out.println("PATH     => '" + pathToFile + "'");
            System.out.println("New LINE => '\r\n" + newLine + "'");
            System.out.println("-----------------------------------------------");
            return;
        }

        new File(tmpFile).createNewFile();

        // 1 - Inicia lendo o arquivo de cadastros.module
        Path readModule = Paths.get(pathToFile); // RelatorioResource.java
        List<String> lines = Files.readAllLines(readModule);
        boolean writted = false;
        boolean iniciaBusca = false;

        String reportRoute = "/" + reportDomain + "/" + options.defaultRoute;
        for (String line : lines) {
            if (!writted) {
                if(line.contains("@GetMapping") && line.contains(reportDomain)) {
                    iniciaBusca = true;

                    // Compara o que foi escrito
                    int inicio = "    @GetMapping(\"".length();
                    int fin = line.lastIndexOf("\")");

                    String token = line.substring(inicio, fin);

                    int comp = reportRoute.compareTo(token);
                    if (comp < 0) {
                        writted = true;
                        writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    } else if (comp == 0) { // Se estiver repetindo (Mesmo nome do token)
                        writted = true;
                    }
                }
                else if(iniciaBusca && line.contains("//endregion")) {
                    writted = true;
                    writeToFile(Paths.get(tmpFile), ("\r\n" + newLine).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }

            writeToFile(Paths.get(tmpFile), (line + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

         Files.delete(readModule);
         new File(tmpFile).renameTo(new File(pathToFile));

        System.out.println("Generated a linha '" + newLine);
        System.out.println("Into '" + pathToFile + "'");
        System.out.println("-----------------------------------------------");
    }

    private void includeServiceLine(GenOptions options) throws IOException {
        String pathToFile = options.mainBack;
        pathToFile += "modulo/relatorio/service/";

        ReportGenerator reportGenerator = options.getReportGenerator();
        String reportName = "RELATORIO_" + reportGenerator.getReportName().toUpperCase();
        String reportTitle = reportGenerator.getReportModel().getTitle();
        String reportType = reportGenerator.getReportModel().getDomain();

        pathToFile += getReportServiceName(reportType) + ".java";

        String newLine =
                "    public Relatorio gerarReport" + options.entityName + "(" + options.entityName + "FilterReport filter) {\r\n" +
                "        try {\r\n" +
                "            String urlReport = jasperReportService.gerarReportPDF(" + reportName + ", filter.getParameters());\r\n" +
                "            return new Relatorio(urlReport, \"" + reportTitle + "\");\r\n" +
                "        } catch (SQLException e) {\r\n" +
                "            throw new ACSBadRequestException(\"error.report.invalido\");\r\n" +
                "        }\r\n" +
                "    }\r\n";

        String tmpFile = "./tmp.txt";

        if (Utils.isAuditionMode()) {
            System.out.println("PATH     => '" + pathToFile + "'");
            System.out.println("New LINE => '\r\n" + newLine + "'");
            System.out.println("-----------------------------------------------");
            return;
        }

        new File(tmpFile).createNewFile();

        // 1 - Inicia lendo o arquivo de cadastros.module
        Path readModule = Paths.get(pathToFile); // RelatorioResource.java
        List<String> lines = Files.readAllLines(readModule);
        boolean writted = false;

        String methodName = "gerarReport" + options.entityName;
        for (String line : lines) {
            if (!writted) {
                if(line.startsWith("    public Relatorio")) {
                    // Compara o que foi escrito
                    int inicio = "    public Relatorio ".length();
                    int fin = line.lastIndexOf("(");

                    String token = line.substring(inicio, fin);

                    int comp = methodName.compareTo(token);
                    if (comp < 0) {
                        writted = true;
                        writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    } else if (comp == 0) { // Se estiver repetindo (Mesmo nome do token)
                        writted = true;
                    }
                }

                if (line.startsWith("}")) {
                    writted = true;
                    writeToFile(Paths.get(tmpFile), ("\r\n" + newLine).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }

            writeToFile(Paths.get(tmpFile), (line + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

        Files.delete(readModule);
        new File(tmpFile).renameTo(new File(pathToFile));

        System.out.println("Generated a linha '" + newLine);
        System.out.println("Into '" + pathToFile + "'");
        System.out.println("-----------------------------------------------");
    }

    private String getReportServiceName(String reportType) {
        String type = reportType.substring(0, 1).toUpperCase() + reportType.substring(1);
        return "Relatorio" + type + "Service";
    }

    private String getTypeByProperty(String type) {
        if(type.equalsIgnoreCase("SEARCH"))
            return "Integer";
        else if(type.equalsIgnoreCase("FILTER"))
            return "List<Integer>";
        return type;
    }

    private void writeToFile(Path path, byte[] bytes, StandardOpenOption... append) throws IOException {
        Files.write(path, bytes, append);
    }
}
