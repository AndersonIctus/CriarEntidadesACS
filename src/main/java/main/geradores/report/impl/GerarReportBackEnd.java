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
import java.util.Map;

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

        if (options.onlyFrontEnd || options.onlyReportFile) {
            System.out.println("Pulando a geração dos arquivos para o BackEnd ...");
        }
        else {
            if (options.generateModel) {
                gerarReportFilter(options);
            }

            if (!options.onlyModel) {
                includeResourceLine(options);
                includeServiceLine(options);
            }
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
        String reportName = "RELATORIO_" + options.fileRelatorios.get(0).getConstantName();

        String imports =
                "import com.innovaro.acs.exceptionhandler.exception.ACSBadRequestException;\r\n" +
                "import com.innovaro.acs.exceptionhandler.exception.ACSNotFoundException;\r\n" +
                "import com.innovaro.acs.model.Empresa;\r\n"+
                "import com.innovaro.acs.repository.customtypes.AcsDateTime;\r\n" +
                "import com.innovaro.acs.service.EmpresaService;\r\n" +
                "import lombok.Data;\r\n" +
                "import org.apache.commons.lang3.StringUtils;\r\n" +
                "\r\n"+
                "import java.math.BigDecimal;\r\n" +
                "import java.time.OffsetDateTime;\r\n" +
                "import java.time.ZoneOffset;\r\n" +
                "import java.util.HashMap;\r\n" +
                "import java.util.List;\r\n" +
                "import java.util.Map;\r\n" +
                "\r\n";

        String properties = "";
        String parametersRequired = "";
        String messageProperties = "";
        String dadosEmpresa = "";
        String constantesParametro = "";
        String tipoApresentacaoParam = "";
        String resourcePathParam = "";

        for(ReportFileModel.ReportProperty prop: reportGenerator.getReportModel().getPropriedades()) {
            String typeProp = prop.getType();

            properties += "    private " + getTypeByProperty(typeProp) + " " + prop.getName();
            if(!prop.isRequired()) {
                if(prop.getValue() != null && !(typeProp.equalsIgnoreCase("SEARCH") || typeProp.equalsIgnoreCase("FILTER"))) {
                    if(typeProp.equalsIgnoreCase("String")) {
                        properties += " = \"" + prop.getValue() + "\"";
                    } else {
                        properties += " = " + prop.getValue();
                    }
                }
            }
            else {
                String methodName = prop.getName().substring(0, 1).toUpperCase() + prop.getName().substring(1);

                if(typeProp.equalsIgnoreCase("String")) {
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

            // ----
            if(prop.getName().equalsIgnoreCase("idEmpresa")) {
                if(prop.getType().equalsIgnoreCase("FILTER")) {
                    dadosEmpresa = "        if(this.getIdEmpresa().size() > 1) {\n" +
                                   "            parametros.put(\"DESCRICAO_EMPRESA\", \"Empresa: Seleção\");\n" +
                                   "            parametros.put(\"PARAM_USA_EMPRESA_SELECAO\", \"S\");\n" +
                                   "        }\n" +
                                   "        else {\n" +
                                   "            Empresa empresa = empresaService.findOne(getIdEmpresa().get(0));\n" +
                                   "            parametros.put(\"DESCRICAO_EMPRESA\", empresa.getRazaoSocial());\n" +
                                   "            parametros.put(\"PARAM_USA_EMPRESA_SELECAO\", \"N\");\n" +
                                   "        }\n";
                }
                else {
                    dadosEmpresa = "        Empresa empresa = empresaService.findOne(getIdEmpresa());\n" +
                                   "        parametros.put(\"DESCRICAO_EMPRESA\", empresa.getRazaoSocial());";
                }
            }

            // Verificando as contantes e a existência do tipo do relatório
            final String frontType = prop.getFront().getType();
            if((frontType.equalsIgnoreCase("SELECT") || frontType.equalsIgnoreCase("RADIO")) && typeProp.equalsIgnoreCase("INTEGER")) {
                constantesParametro += "    // Constante do " + prop.getName() + "\r\n";
                final Map<String, String> optionsProp = prop.getFront().getOptions();
                for (String key : optionsProp.keySet()) {
                    String nomeConstante = prop.getName().toUpperCase() + "_" + optionsProp.get(key).toUpperCase().replaceAll(" ", "_");
                    nomeConstante = normalizaNomeConstante(nomeConstante);
                    constantesParametro += "    public static final int " + nomeConstante + " = " + key + ";\r\n";

                    if(prop.getName().equalsIgnoreCase("tipoRelatorio")) {
                        tipoApresentacaoParam += "            case " + nomeConstante + ":\r\n" +
                                                 "                return \"" + reportGenerator.getReportModel().getTitulo() + " - " + optionsProp.get(key) + "\"; \r\n";

                        String reportType = reportGenerator.getReportModel().getDominio();

                        String nameReport = "";
                        int keyPos = Integer.parseInt(key);
                        if(keyPos > options.fileRelatorios.size()) {
                            nameReport = options.fileRelatorios.get(0).getName();
                        } else {
                            nameReport = options.fileRelatorios.get(keyPos).getName();
                        }
                        resourcePathParam += "            case " + nomeConstante + ":\r\n" +
                                             "                return \"/report/" + reportType + "/" + options.defaultRoute + "/" + nameReport + ".jasper\"; \r\n";
                    }
                }

                constantesParametro += "\r\n";
            }
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
                "    //region --- Propriedades\r\n" +
                properties +
                "\r\n" +
                constantesParametro +
                "    //endregion" + "\r\n" +
                "\r\n" +
                "    @Override\r\n" +
                "    public String getTituloRelatorio() { \r\n" +
                (( !(tipoApresentacaoParam.equals("")) )
                    ? "        switch (getTipoRelatorio()) { \r\n" +
                      "            default: \r\n" +
                      tipoApresentacaoParam +
                      "        }"
                    : "        return \""+ reportGenerator.getReportModel().getTitulo() + "\";") + "\r\n" +
                "    }\r\n" +
                "\r\n" +
                "    @Override\r\n" +
                "    public String getResourcePath() {\r\n" +
                (( !(resourcePathParam.equals("")) )
                        ? "        switch (getTipoRelatorio()) { \r\n" +
                          "            default: \r\n" +
                          resourcePathParam +
                          "        }"
                        : "        return " + reportName + ";") + "\r\n" +
                "    }\r\n" +
                "\r\n" +
                "    @Override\r\n" +
                "    protected void validarParametros() {\r\n" +
                parametersRequired +
                messageProperties +
                "    }\r\n" +
                "\r\n" +
                "    @Override\r\n" +
                "    protected Map<String, Object> getParametrosDoFiltro() {\r\n" +
                "        Map<String, Object> parametros = new HashMap<>();\r\n" +
                "\r\n" +
                dadosEmpresa + "\r\n" +
                "        parametros.put(\"DESCRICAO_RELATORIO\", getTituloRelatorio());" + "\r\n" +
                "        parametros.put(\"OPCAO_RELATORIO\", this.getOpcaoRelatorio());" + "\r\n" +
                "\r\n" +
                "        // Data e Hora da Impressão" + "\r\n" +
                "        parametros.put(\"DATA_HORA_IMPRESSAO\", getDataHoraImpressao() );" + "\r\n" +
                "\r\n" +
                "        return parametros;\r\n" +
                "    }\r\n" +
                "\r\n" +
                "    private String getOpcaoRelatorio() {" + "\r\n" +
                "        String descricao = \"Filtros:\";" + "\r\n" +
                "        return descricao;" + "\r\n" +
                "    }\r\n" +
                "\r\n" +
                "    //region ----- OUTROS METODOS" + "\r\n" +
                "    //endregion" + "\r\n" +
                "}\r\n" +
                "";

        Utils.writeContentTo(path + options.entityName + "FilterReport.java", classBody);
        System.out.println("Generated Entity '" + options.entityName + "FilterReport' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private void includeResourceLine(GenOptions options) throws IOException {
        String pathToFile = options.mainBack;
        pathToFile += "modulo/relatorio/resource/RelatorioResource.java";

        ReportGenerator reportGenerator = options.getReportGenerator();
        String reportDomain = reportGenerator.getReportModel().getDominio();
        String role = options.accessAlias.toUpperCase().replaceAll("\\.", "").replaceAll(" ", "_");
        String serviceName = reportDomain + "Service";

        String newLine =
                "    @GetMapping(\"/" + reportDomain + "/" + options.defaultRoute + "\")\r\n" +
                "    @PreAuthorize(HAS_AUTHORITY_"+role+")\r\n" +
                "    public ResponseEntity<Relatorio> relatorio" + options.entityName + "(" + options.entityName + "FilterReport filtro) {\r\n" +
                "        return ResponseEntity.ok().body(" + serviceName + ".gerarRelatorio" + options.entityName + "(filtro));\r\n" +
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
        String reportType = reportGenerator.getReportModel().getDominio();

        pathToFile += getReportServiceName(reportType) + ".java";

        String newLine =
                "    public Relatorio gerarRelatorio" + options.entityName + "(" + options.entityName + "FilterReport filtro) {\r\n" +
                "        try {\r\n" +
                "            return jasperReportService.gerarRelatorio(filtro);\r\n" +
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

        String methodName = "gerarRelatorio" + options.entityName;
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

    private String normalizaNomeConstante(String nomeConstante) {
        return nomeConstante
                .replaceAll("[ÁÀÃÂ]", "A")
                .replaceAll("[ÉÈÊ]", "E")
                .replaceAll("[ÍÌÎ]", "I")
                .replaceAll("[ÓÒÕÔ]", "O")
                .replaceAll("[ÚÙÛ]", "U")
                .replaceAll("[Ç]", "C")
                ;
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
