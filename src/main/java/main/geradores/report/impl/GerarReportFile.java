package main.geradores.report.impl;

import main.geradores.GenOptions;
import main.geradores.IGerador;
import main.geradores.Utils;
import main.geradores.report.ReportGenerator;

import java.io.IOException;

public class GerarReportFile implements IGerador {
    private static String mainPath = "..\\reports\\";

    @Override
    public void gerarArquivos(GenOptions options) throws IOException {
        System.out.println("===============================================");
        System.out.println("========== GERANDO REPORT FILE ================");

        if (options.onlyBackEnd || options.onlyFrontEnd || !options.generateReportFile) {
            System.out.println("Pulando a geração dos arquivos para o Report File (JXML)...");
        }
        else {
            mainPath = mainPath + getDomainDirectoryReport(options);

            // 1 - Se não existir, criar o diretório de domínio para o arquivo .jrxml
            if(!Utils.isDirectory(mainPath)) {
                Utils.createDirectory(mainPath);
            }
            mainPath += options.defaultRoute + "/";

            // 2 - Criar o Arquivo .jrxml Padrão
            Utils.createDirectory(mainPath); // Diretorio do Relatório em Si !
            gerarReportFile(options);
        }

        System.out.println();
        System.out.println("===============================================");
        System.out.println("===============================================");
    }

    private void gerarReportFile(GenOptions options) throws IOException {
        String fileBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jasperReport xmlns=\"http://jasperreports.sourceforge.net/jasperreports\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd\"" +
                " name=\"" + options.defaultRoute + "\"" + " " + getPageProperties(options) + ">\n" +
                "    <property name=\"com.jaspersoft.studio.data.sql.tables\" value=\"\"/>\n" +
                "    <property name=\"com.jaspersoft.studio.report.description\" value=\"\"/>\n" +
                "    <property name=\"com.jaspersoft.studio.data.defaultdataadapter\" value=\"acs\"/>\n" +
                "    <property name=\"com.jaspersoft.studio.data.sql.SQLQueryDesigner.sash.w1\" value=\"154\"/>\n" +
                "    <property name=\"com.jaspersoft.studio.data.sql.SQLQueryDesigner.sash.w2\" value=\"841\"/>\n" +
                "    <property name=\"com.jaspersoft.studio.property.dataset.dialog.DatasetDialog.sash.w1\" value=\"718\"/>\n" +
                "    <property name=\"com.jaspersoft.studio.property.dataset.dialog.DatasetDialog.sash.w2\" value=\"268\"/>\n" +
                "    <parameter name=\"DESCRICAO_RELATORIO\" class=\"java.lang.String\"/>\n" +
                "    <parameter name=\"DESCRICAO_EMPRESA\" class=\"java.lang.String\"/>\n" +
                "    <parameter name=\"OPCAO_RELATORIO\" class=\"java.lang.String\"/>\n" +
                "    <parameter name=\"SISTEMA_VERSAO\" class=\"java.lang.String\"/>\n" +
                "    <parameter name=\"SUBREPORT_DIR\" class=\"java.lang.String\"/>\n" +
                "    <parameter name=\"DATA_HORA_IMPRESSAO\" class=\"java.lang.String\"/>\n" +
                "    <queryString>\n" +
                "        <![CDATA[]]>\n" +
                "    </queryString>\n" +
                getPageHeader(options) + "\n" +
                "    <detail>\n" +
                "        <band height=\"12\">\n" +
                "            <property name=\"com.jaspersoft.studio.unit.height\" value=\"px\"/>\n" +
                "            <staticText>\n" +
                "                <reportElement x=\"2\" y=\"0\" width=\"178\" height=\"12\" >\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.height\" value=\"px\"/>\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.y\" value=\"px\"/>\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.x\" value=\"px\"/>\n" +
                "                </reportElement>\n" +
                "                <textElement>\n" +
                "                    <font size=\"8\" isBold=\"true\" isItalic=\"true\"/>\n" +
                "                </textElement>\n" +
                "                <text><![CDATA[Gerando Dados do relatório ...]]></text>\n" +
                "            </staticText>\n" +
                "        </band>\n" +
                "    </detail>\n" +
                getPageFooter(options) + "\n" +
                "</jasperReport>\n";

        Utils.writeContentTo(mainPath + options.defaultRoute + ".jrxml", fileBody);
        System.out.println("Generated Report File '" + options.defaultRoute + ".jrxml' into '" + mainPath + "'");
        System.out.println("-----------------------------------------------\r\n");
    }

    private String getPageProperties(GenOptions options) {
        String optionPropertie = "";
        // Opções para propriedades de Landscape
        if(options.reportOrientation.equalsIgnoreCase("landscape")) {
            optionPropertie = "pageWidth=\"842\" pageHeight=\"595\" orientation=\"Landscape\" columnWidth=\"802\"";
        }
        else { // Padrão é o retorno PORTRAIT
            optionPropertie = "pageWidth=\"595\" pageHeight=\"842\" columnWidth=\"555\"";
        }

        return optionPropertie + " whenNoDataType=\"AllSectionsNoDetail\" leftMargin=\"20\" rightMargin=\"20\" topMargin=\"20\" bottomMargin=\"20\"";
    }

    private String getPageHeader(GenOptions options) {
        boolean portrait = options.reportOrientation.equalsIgnoreCase("portrait");
        return  "    <pageHeader>\n" +
                "        <band height=\"59\">\n" +
                "            <property name=\"com.jaspersoft.studio.unit.height\" value=\"px\"/>\n" +
                "            <textField isBlankWhenNull=\"true\">\n" +
                "                <reportElement x=\"2\" y=\"4\" width=\"" + ((portrait)?"438": "680") + "\" height=\"22\" >\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.width\" value=\"px\"/>\n" +
                "                </reportElement>\n" +
                "                <textElement verticalAlignment=\"Middle\" markup=\"none\">\n" +
                "                    <font fontName=\"Roboto Mono\" size=\"14\" isBold=\"true\"/>\n" +
                "                </textElement>\n" +
                "                <textFieldExpression><![CDATA[$P{DESCRICAO_EMPRESA}]]></textFieldExpression>\n" +
                "            </textField>\n" +
                "            <textField isBlankWhenNull=\"true\">\n" +
                "                <reportElement x=\"2\" y=\"27\" width=\"" + ((portrait)?"551": "796") + "\" height=\"16\" forecolor=\"#9E1602\" >\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.width\" value=\"px\"/>\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.height\" value=\"px\"/>\n" +
                "                </reportElement>\n" +
                "                <textElement verticalAlignment=\"Middle\">\n" +
                "                    <font fontName=\"Roboto Mono\" size=\"12\" isBold=\"true\"/>\n" +
                "                </textElement>\n" +
                "                <textFieldExpression><![CDATA[$P{DESCRICAO_RELATORIO}]]></textFieldExpression>\n" +
                "            </textField>\n" +
                "            <textField textAdjust=\"ScaleFont\" isBlankWhenNull=\"true\">\n" +
                "                <reportElement x=\"2\" y=\"44\" width=\"" + ((portrait)?"551": "796") + "\" height=\"14\" forecolor=\"#ADADAD\" >\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.width\" value=\"px\"/>\n" +
                "                </reportElement>\n" +
                "                <textElement verticalAlignment=\"Middle\">\n" +
                "                    <font fontName=\"Roboto Mono\" isBold=\"true\" isItalic=\"true\"/>\n" +
                "                </textElement>\n" +
                "                <textFieldExpression><![CDATA[$P{OPCAO_RELATORIO}]]></textFieldExpression>\n" +
                "            </textField>\n" +
                "            <textField isBlankWhenNull=\"true\">\n" +
                "                <reportElement x=\"" + ((portrait)?"467": "718") + "\" y=\"12\" width=\"" + ((portrait)?"86": "80") + "\" height=\"14\" >\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.x\" value=\"pixel\"/>\n" +
                "                </reportElement>\n" +
                "                <textElement textAlignment=\"Left\">\n" +
                "                    <font fontName=\"Roboto Mono\"/>\n" +
                "                </textElement>\n" +
                "                <textFieldExpression><![CDATA[$P{SISTEMA_VERSAO}]]></textFieldExpression>\n" +
                "            </textField>\n" +
                "            <image>\n" +
                "                <reportElement x=\"" + ((portrait)?"443": "692") + "\" y=\"4\" width=\"22\" height=\"22\">\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.height\" value=\"px\"/>\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.width\" value=\"px\"/>\n" +
                "                </reportElement>\n" +
                "                <imageExpression><![CDATA[\"https://imagens-reports.s3.us-east-2.amazonaws.com/icone_rel_acs.png\"]]></imageExpression>\n" +
                "            </image>\n" +
                "        </band>\n" +
                "    </pageHeader>";
    }

    private String getPageFooter(GenOptions options) {
        boolean portrait = options.reportOrientation.equalsIgnoreCase("portrait");
        return  "    <pageFooter>\n" +
                "        <band height=\"20\">\n" +
                "            <property name=\"com.jaspersoft.studio.unit.height\" value=\"px\"/>\n" +
                "            <textField>\n" +
                "                <reportElement x=\"2\" y=\"2\" width=\"114\" height=\"14\" >\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.height\" value=\"px\"/>\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.x\" value=\"px\"/>\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.y\" value=\"px\"/>\n" +
                "                </reportElement>\n" +
                "                <textElement verticalAlignment=\"Middle\">\n" +
                "                    <font fontName=\"Roboto Mono\" size=\"6\"/>\n" +
                "                </textElement>\n" +
                "                <textFieldExpression><![CDATA[$P{DATA_HORA_IMPRESSAO}]]></textFieldExpression>\n" +
                "            </textField>\n" +
                "            <textField textAdjust=\"ScaleFont\">\n" +
                "                <reportElement x=\"" + ((portrait)?"428": "648") + "\" y=\"2\" width=\"100\" height=\"14\" >\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.height\" value=\"px\"/>\n" +
                "                </reportElement>\n" +
                "                <textElement textAlignment=\"Right\" verticalAlignment=\"Middle\">\n" +
                "                    <font fontName=\"Roboto Mono\" size=\"6\"/>\n" +
                "                </textElement>\n" +
                "                <textFieldExpression><![CDATA[\"Página \" + $V{PAGE_NUMBER}]]></textFieldExpression>\n" +
                "            </textField>\n" +
                "            <textField textAdjust=\"ScaleFont\" evaluationTime=\"Report\">\n" +
                "                <reportElement x=\"" + ((portrait)?"529": "749") + "\" y=\"2\" width=\"" + ((portrait)?"30": "49") + "\" height=\"14\">\n" +
                "                    <property name=\"com.jaspersoft.studio.unit.height\" value=\"px\"/>\n" +
                "                </reportElement>\n" +
                "                <textElement textAlignment=\"Left\" verticalAlignment=\"Middle\">\n" +
                "                    <font fontName=\"Roboto Mono\" size=\"6\"/>\n" +
                "                </textElement>\n" +
                "                <textFieldExpression><![CDATA[\" de \" + $V{PAGE_NUMBER}]]></textFieldExpression>\n" +
                "            </textField>" +
                "        </band>\n" +
                "    </pageFooter>";
    }

    private String getDomainDirectoryReport(GenOptions options) {
        ReportGenerator reportGenerator = options.getReportGenerator();
        return reportGenerator.getReportModel().getDominio() + "/";
    }
}
