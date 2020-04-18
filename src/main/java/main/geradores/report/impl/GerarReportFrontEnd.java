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
import java.util.ArrayList;
import java.util.List;

public class GerarReportFrontEnd implements IGerador {
    private static String mainPath = "..\\front\\src\\app\\modulos\\relatorios\\";

    @Override
    public void gerarArquivos(GenOptions options) throws IOException {
        System.out.println("===============================================");
        System.out.println("=========== GERANDO FRONT END =================");
        if (options.mainFront != null) {
            mainPath = options.mainFront + "modulos/relatorios/";
        } else {
            options.mainFront = mainPath;
        }

        if (options.onlyBackEnd) {
            System.out.println("Pulando a geração dos arquivos para o FrontEnd ...");
        }
        else {
            // 1 - Se não for um diretorio, deve criar o DOMINIO
            if(!Utils.isDirectory(mainPath + getDirectoryDomain(options))) {
                createDomainFiles(options);
            }
            mainPath += getDirectoryDomain(options) + options.frontBaseFolder + "/";

            System.out.println("Incluindo Modulo '" + options.frontBaseFolder + "' ao Path => " + mainPath);
            Utils.createDirectory(mainPath);
            gerarModule(options);
            gerarComponent(options);
            gerarTela(options);

            mainPath = options.mainFront + "modulos/relatorios/" + getDirectoryDomain(options);
            incluirModuloNoDominio(options);
        }

        System.out.println();
        System.out.println("===============================================");
        System.out.println("===============================================");
    }

    //region --- Funções para a criação do Modulo ---
    private void gerarModule(GenOptions options) throws IOException {
        String path = mainPath;
        String domainName = options.getReportGenerator().getReportModel().getReportType();
        String roleDescription = options.getReportGenerator().getReportModel().getRole().getDescription();

        // ----------------------- Creating Module
        String fileBody =
                "import { NgModule } from '@angular/core';\r\n" +
                "import { CommonModule } from '@angular/common';\r\n" +
                "import { RouterModule } from '@angular/router';\r\n" +
                "\r\n" +
                "import { AuthGuard } from '../../../../general/general-interceptors/guard/auth-guard';\r\n" +
                "\r\n" +
                "import { MaterialModule, MaterialDateModule } from '../../../../shared/utils/material-module.module';\r\n" +
                "import { SharedRelatoriosComponentModule } from '../../shared/components/shared-relatorios-component.module';\r\n" +
                "import { SharedComponentsModule } from '../../../../shared/components/shared-components.module';\r\n" +
                "import { SharedDirectivesModule } from '../../../../shared/directive/shared-directives.module';\r\n" +
                "import { UtilsModule } from '../../../../shared/utils/utils.module';\r\n" +
                "\r\n" +
                "import { "+options.frontBaseName+"Component } from './"+options.defaultRoute+".component';\r\n" +
                " \r\n" +
                "@NgModule({\r\n" +
                "    imports: [\r\n" +
                "        CommonModule,\r\n" +
                "        MaterialModule,\r\n" +
                "        MaterialDateModule,\r\n" +
                "        UtilsModule,\r\n" +
                "\r\n" +
                "        SharedRelatoriosComponentModule,\r\n" +
                "        SharedComponentsModule,\r\n" +
                "        SharedDirectivesModule,\r\n" +
                "        \r\n" +
                "        // rota padrao = /relatorios/"+domainName+"/"+options.defaultRoute+"\r\n" +
                "        RouterModule.forChild([\r\n" +
                "            { path: '', component: "+options.frontBaseName+"Component, canActivate: [AuthGuard], data: { role: ['SUPER', '"+roleDescription+"'] } }\r\n" +
                "        ])\r\n" +
                "    ],\r\n" +
                "    declarations: [\r\n" +
                "        "+options.frontBaseName+"Component\r\n" +
                "    ]\r\n" +
                "})\r\n" +
                "export class "+options.frontBaseName+"Module {}\r\n";

        Utils.writeContentTo(path + options.defaultRoute + ".module.ts", fileBody);
        System.out.println("Generated Module '" + options.defaultRoute + ".module.ts' into '" + path + "'");
        System.out.println("-----------------------------------------------\r\n");
    }

    private void gerarComponent(GenOptions options) throws IOException {
        String path = mainPath;
        ReportFileModel reportModel = options.getReportGenerator().getReportModel();
        String domainName = reportModel.getReportType();
        String title = reportModel.getTitle();

        String imports =
                "import { Component, OnInit } from '@angular/core';\r\n" +
                "import { Validators } from '@angular/forms';\r\n" +
                "\r\n" +
                "import { RelatorioBaseComponent } from '../../shared/base/relatorio-base.component';\r\n" +
                "import { RelatorioBaseService } from '../../shared/base/relatorio-base.service';\r\n" +
                "\r\n" +
                "import { AcsDateTime } from '../../../../shared/utils/AcsDateTime';\r\n" +
                "import { Moment } from '../../../../shared/utils/Moment';\r\n";

        String bindProperties = "";
        String getParameters = "";
        List<ReportFileModel.ReportProperty> searchProperties = new ArrayList<>();

        for(ReportFileModel.ReportProperty prop: reportModel.getProperties()) {
            // Properties ...
            String propLine = "";
            if(prop.getType().equalsIgnoreCase("SEARCH")) {
                propLine = "" +
                        "            " + prop.getName() + "_group: this.formBuilder.group({\r\n" +
                        "                id: [''],\r\n" +
                        "                descricao_" + prop.getName() + ": ['']\r\n" +
                        "            }),\r\n";
                searchProperties.add(prop);

            }
            else {
                String value = prop.getValue();
                if(value != null && prop.getType().equalsIgnoreCase("String")) {
                    value = "'" + value + "'";
                }

                propLine = "            " + prop.getName() + ": [" + value;
                if(prop.isRequired()) {
                    propLine += ", Validators.required],\r\n";
                }
                else {
                    propLine += "],\r\n";
                }
            }

            bindProperties += propLine;

            // Parameters ...
            String paramLine = "        filterList.push(`" + prop.getName() + "=${"+getParamValue(prop)+"}`);";
            if(!prop.isRequired()) {
                paramLine =
                        "        if(this.formModel.get('" + prop.getName() + "').value !== '') {\r\n" +
                        "    " + paramLine + "\r\n" +
                        "        }\r\n";
            }
            getParameters += paramLine + "\r\n";
        }

        // ----------------------- Creating Component
        String fileBody =
                imports +
                "\r\n" +
                "@Component({\r\n" +
                "    templateUrl: './"+options.defaultRoute+".component.html'\r\n" +
                "})\r\n" +
                "export class "+options.frontBaseName+"Component extends RelatorioBaseComponent implements OnInit {\r\n" +
                "    \r\n" +
                "    constructor(\r\n" +
                "        public baseServices: RelatorioBaseService\r\n" +
                "    ) { super(baseServices); }\r\n" +
                "\r\n" +
                "    ngOnInit(): void {\r\n" +
                "        super.ngOnInit();\r\n" +
                "\r\n" +
                "        this.urlAction = '/"+domainName+"/"+options.defaultRoute+"'; // end-point do relatorio no back\r\n" +
                "        this.title = '"+title+"';\r\n" +
                "    }\r\n" +
                "\r\n" +
                "    // Funções do Fomulário //\r\n" +
                "    bindFormValidators() {\r\n" +
                "        this.formModel = this.formBuilder.group({\n" +
                bindProperties +
                "        });\r\n" +
                "    }\r\n" +
                "\r\n" +
                "    // Funções de Filters //\r\n" +
                "    getParameters(): string {\r\n" +
                "        const filterList: string[] = [];\r\n" +
                getParameters +
                "        return filterList.join('&');\r\n" +
                "    }\r\n" +
                "}\r\n";

        Utils.writeContentTo(path + options.defaultRoute + ".component.ts", fileBody);
        System.out.println("Generated Module '" + options.defaultRoute + ".component.ts' into '" + path + "'");
        System.out.println("-----------------------------------------------\r\n");
    }

    private void gerarTela(GenOptions options) throws IOException {
        String path = mainPath;
        ReportFileModel reportModel = options.getReportGenerator().getReportModel();
        String domainName = reportModel.getReportType();

        String posFormElements = "";
        String filters = "";
        boolean hasSearchProperty = false;
        for(ReportFileModel.ReportProperty prop: reportModel.getProperties()) {
            filters += "\r\n" + getHtmlElement(prop);

            if(prop.getType().equalsIgnoreCase("SEARCH")) {
                hasSearchProperty = true;
            }
        }

        if(hasSearchProperty) {
            posFormElements += "\r\n" +
                    "<!-- SEARCH DIALOGS -->\r\n" +
                    "<dialogs_search class=\"search-relatorios-" + options.defaultRoute + "\" #search_dialog></dialogs_search>";
        }

        // ----------------------- Creating Component
        String fileBody =
                "<form [formGroup]=\"formModel\" data-toggle=\"validator\" role=\"form\">\r\n" +
                "    <others_panel [panelTitle]=\"title\">\r\n" +
                "        <div class=\"panel-content\">\r\n" +
                "            <acs_relatorio #relatorio_filter class=\"row filter filter--edges m-0 p-0\" (doClear)=\"doClear();\" (visualizarRelatorio)=\"visualizarRelatorio();\">\r\n" +
                filters +
                "            </acs_relatorio>\r\n" +
                "        </div>\r\n" +
                "    </others_panel>\r\n" +
                "</form>" +
                posFormElements;

        Utils.writeContentTo(path + options.defaultRoute + ".component.html", fileBody);
        System.out.println("Generated Template '" + options.defaultRoute + ".component.html' into '" + path + "'");
        System.out.println("-----------------------------------------------\r\n");
    }

    private void incluirModuloNoDominio(GenOptions options) throws IOException {
        String domainName = options.getReportGenerator().getReportModel().getReportType();

        String pathToFile = mainPath + domainName + "-routing.module.ts";

        String newLine = "    { path: '"+options.defaultRoute+"', loadChildren: './"+options.defaultRoute+"/"+options.defaultRoute+".module#"+options.frontBaseName+"Module' },";
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

        for (String line : lines) {
            if (!writted) {
                if(line.startsWith("    { path: '")) {
                    // Compara o que foi escrito
                    int inicio = "    { path: '".length();
                    int fin = line.lastIndexOf("',");

                    String token = line.substring(inicio, fin);

                    int comp = domainName.compareTo(token);
                    if (comp < 0) {
                        writted = true;
                        writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    } else if (comp == 0) { // Se estiver repetindo (Mesmo nome do token)
                        writted = true;
                    }
                }

                if (line.startsWith("];")) {
                    writted = true;
                    writeToFile(Paths.get(tmpFile), (newLine+"\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }

            writeToFile(Paths.get(tmpFile), (line + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

        Files.delete(readModule);
        new File(tmpFile).renameTo(new File(pathToFile));
    }
    //endregion

    //region -- Funções para criação de Domínio ---
    private void createDomainFiles(GenOptions options) throws IOException {
        System.out.println("############## Gerando Arquivos de Domínio");
        gerarDomainModule(options);
        incluirDomainModule(options);
        System.out.println("##########################################");
    }

    private void gerarDomainModule(GenOptions options) throws IOException {
        String path = mainPath + getDirectoryDomain(options);
        Utils.createDirectory(path);

        String domainName = options.getReportGenerator().getReportModel().getReportType();
        String domainClass = domainName.substring(0,1).toUpperCase() + domainName.substring(1);

        // ----------------------- Creating Module
        String fileBody =
                "import { NgModule } from '@angular/core';\r\n" +
                "import { CommonModule } from '@angular/common';\r\n" +
                "\r\n" +
                "import { " + domainClass + "RoutingModule } from './"+domainName+"-routing.module';\r\n" +
                "\r\n" +
                "@NgModule({\r\n" +
                "    imports: [ \r\n" +
                "        CommonModule, \r\n" +
                "        \r\n" +
                "        "+domainClass+"RoutingModule \r\n" +
                "    ]\r\n" +
                "})\r\n" +
                "export class "+domainClass+"Module { }\r\n";

        Utils.writeContentTo(path + domainName + ".module.ts", fileBody);
        System.out.println("Generated Domain '" + domainName + ".module.ts' into '" + path + "'");
        System.out.println("-----------------------------------------------\r\n");

        // ----------------------- Creating Routing
        fileBody =
                "import { NgModule } from '@angular/core';\r\n" +
                "import { Routes, RouterModule } from '@angular/router';\r\n" +
                "\r\n" +
                "// rota padrao = /relatorios/"+domainName+"\r\n" +
                "const routes: Routes = [\r\n" +
                "    { path: '', redirectTo: '/home', pathMatch: 'full' },\r\n" +
                "];\r\n" +
                "\r\n" +
                "@NgModule({\r\n" +
                "  imports: [RouterModule.forChild(routes)],\r\n" +
                "  exports: [ ]\r\n" +
                "})\r\n" +
                "export class "+domainClass+"RoutingModule { }\r\n";

        Utils.writeContentTo(path + domainName + "-routing.module.ts", fileBody);
        System.out.println("Generated Domain Routing '" + domainName + "-routing.module.ts' into '" + path + "'");
        System.out.println("-----------------------------------------------\r\n");
    }

    private void incluirDomainModule(GenOptions options) throws IOException {
        String pathToFile = mainPath + "relatorios-routing.module.ts";

        String domainName = options.getReportGenerator().getReportModel().getReportType();
        String domainClass = domainName.substring(0,1).toUpperCase() + domainName.substring(1);

        String newLine = "    { path: '"+domainName+"', loadChildren: './"+domainName+"/"+domainName+".module#"+domainClass+"Module' },";
        String tmpFile = "./tmp.txt";

        if (Utils.isAuditionMode()) {
            System.out.println("PATH     => '" + pathToFile + "'");
            System.out.println("New LINE => '" + newLine + "'");
            System.out.println("-----------------------------------------------");
            return;
        }

        new File(tmpFile).createNewFile();

        // 1 - Inicia lendo o arquivo de cadastros.module
        Path readModule = Paths.get(pathToFile); // RelatorioResource.java
        List<String> lines = Files.readAllLines(readModule);
        boolean writted = false;

        for (String line : lines) {
            if (!writted) {
                if(line.startsWith("    { path: '")) {
                    // Compara o que foi escrito
                    int inicio = "    { path: '".length();
                    int fin = line.lastIndexOf("',");

                    String token = line.substring(inicio, fin);

                    int comp = domainName.compareTo(token);
                    if (comp < 0) {
                        writted = true;
                        writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    } else if (comp == 0) { // Se estiver repetindo (Mesmo nome do token)
                        writted = true;
                    }
                }

                if (line.startsWith("];")) {
                    writted = true;
                    writeToFile(Paths.get(tmpFile), (newLine+"\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }

            writeToFile(Paths.get(tmpFile), (line + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

        Files.delete(readModule);
        new File(tmpFile).renameTo(new File(pathToFile));
    }
    //endregion

    private String getDirectoryDomain(GenOptions options) {
        ReportGenerator reportGenerator = options.getReportGenerator();
        return reportGenerator.getReportModel().getReportType() + "/";
    }

    private String getParamValue(ReportFileModel.ReportProperty prop) {
        String ret = "this.formModel.get('" + prop.getName() + "').value";
        if(prop.getType().equalsIgnoreCase("AcsDateTime")) {
            ret = "Moment.format(" + ret + ")";
        } else if(prop.getType().equalsIgnoreCase("SEARCH")) {
            ret = "this.formModel.get('"+ prop.getName() + "_group.id').value";
        }

        return ret;
    }

    private String getHtmlElement(ReportFileModel.ReportProperty prop) {
        String spc = "                ";

        String div = "";
        String matFormField = "";
        String matFormFieldInner = "";
        String matLabel = "";
        String matElement = "";
        String errors = "";

        ReportFileModel.ReportProperty.PropertyFrontValue front = prop.getFront();

        // Material Form
        matFormFieldInner = "<mat-form-field appearance=\"fill\" hideRequiredMarker=\"true\" floatLabel=\"always\"";
        if(prop.isRequired()) {
            matFormFieldInner += " required";
        }
        matFormFieldInner += ">";

        // HTML pelo Type -- Element And Label //
        matLabel = "\t" + "<mat-label><b>" + front.getLabel() + "</b></mat-label>" + "\r\n";
        if(front.getType().equalsIgnoreCase("INPUT")) {
            div = "<div class=\"form-group col-4 col-sm-4 col-md-3 col-lg-2\">";
            matElement = "\t" + "<input matInput formControlName=\"" + prop.getName() + "\" ";
            if(prop.getValue() != null) {
                matElement += "placeholder=\""+prop.getValue()+"\"";
            }
            matElement += ">\r\n";

        }
        else if(front.getType().equalsIgnoreCase("DATE")) {
            div = "<div class=\"form-group col-6 col-sm-6 col-md-4 col-lg-3\">";
            matElement = "\t" + "<input formControlName=\"" + prop.getName() + "\" matInput [matDatepicker]=\"" + prop.getName() + "_picker\" " +
                                "maxLength=\"10\" title=\""+front.getLabel()+"\" placeholder=\"00/00/0000\">\r\n" +
                          "\r\n" +
                          spc + "\t\t" + "<mat-datepicker-toggle matSuffix [for]=\""+prop.getName()+"_picker\"></mat-datepicker-toggle>\r\n" +
                          spc + "\t\t" + "<mat-datepicker #"+prop.getName()+"_picker></mat-datepicker>\r\n";
            errors = spc + "\t" + "<others_alert-errors [submitted]=\"submitted\" [control]=\"formModel.get('"+prop.getName()+"')\"></others_alert-errors>\r\n";

        }
        else if(front.getType().equalsIgnoreCase("TEXTAREA")) {
            div = "<div class=\"form-group col-12\">";
            matLabel   = "\t" + "<mat-label><b>" + front.getLabel() + " ({{formModel.get('" + prop.getName() + "').value.length || 0}}/250)</b></mat-label>" + "\r\n";
            matElement = "\t" + "<textarea matInput formControlName=\"" + prop.getName() + "\" maxLength=\"250\" rows=\"3\"></textarea>" + "\r\n";

        }
        else if(front.getType().equalsIgnoreCase("CHECKBOX")) {
            div = "<div class=\"form-group col-4 col-sm-4 col-md-3 col-lg-3\">";
            matElement = "\t" + "<mat-checkbox type=\"checkbox\" formControlName=\"" + prop.getName() + "\" class=\"mr-1\"></mat-checkbox>" + "\r\n" +
                   spc + "\t\t" + "<b>" + front.getLabel() + "</b>\r\n";

            matFormField = "\t" + "<label style=\"cursor: pointer;\" >" + "\r\n" +
                    spc + "\t" + matElement +
                    spc + "\t" + "</label>";

        }
        else if(front.getType().equalsIgnoreCase("SEARCH")) {
            div = "<div class=\"form-group col-12 col-md-4 col-lg-4\" formGroupName=\""+prop.getName()+"_group\" " + ((prop.isRequired())? "required" : "") + ">";
            matFormFieldInner = "<mat-form-field appearance=\"fill\" hideRequiredMarker=\"true\" floatLabel=\"always\" title=\""+front.getLabel()+"\" style=\"cursor: pointer;\" (click)=\"openSearchDialog('"+prop.getName()+"');\" >";

            matElement = "\t" + "<input matInput formControlName=\"descricao_" + prop.getName() + "\" style=\"cursor: pointer;\" readonly>" + "\r\n" +
                    "\r\n" +
                    spc + "\t\t" + "<button mat-button matSuffix mat-icon-button type=\"button\" aria-label=\"Search\">\r\n" +
                    spc + "\t\t\t" + "<mat-icon>search</mat-icon>\r\n" +
                    spc + "\t\t" + "</button>\r\n";
        }
        else {
            throw new RuntimeException("Tipo [" + front.getType() + "] passado é desconhecido");
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        // Montando MatForm //
        if(matFormField.equals("")) {
            matFormField = "\t" + matFormFieldInner + "\r\n" +
                    spc + "\t" + matLabel +
                    spc + "\t" + matElement +
                    spc + "\t" + "</mat-form-field>";
        }

        if(errors.equals("") && prop.isRequired() && !front.getType().equalsIgnoreCase("CHECKBOX")) {
            errors = spc + "\t" + "<others_alert-errors [submitted]=\"submitted\" [control]=\"formModel.get('"+prop.getName()+"')\"></others_alert-errors>\r\n";
        }

        // Montando DIV //
        div = spc + div + "\r\n" +
              spc + matFormField + "\r\n" +
              errors +
              spc + "</div>\r\n";

        return spc + "<!-- " + prop.getName().toUpperCase() + " -->" + "\r\n" + div;
    }

    private void writeToFile(Path path, byte[] bytes, StandardOpenOption... append) throws IOException {
        Files.write(path, bytes, append);
    }
}
