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

    //region // --- Funções para a criação do Modulo --- //
    private void gerarModule(GenOptions options) throws IOException {
        String path = mainPath;
        String domainName = options.getReportGenerator().getReportModel().getReportType();
        String roleDescription = options.getReportGenerator().getReportModel().getRole();

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

        // --------- Gerando Imports
        String imports =
            "import { Component, OnInit, ViewChild } from '@angular/core';\r\n" +
            "import { Validators } from '@angular/forms';\r\n" +
            "\r\n" +
            "import { DialogDesignModel } from '../../../../shared/components/dialogs/custom-dialog/DialogDesignModel';\r\n" +
            "import { SearchDialogComponent, SearchResponseModel } from '../../../../shared/components/dialogs/search-dialog/search-dialog.component';\r\n" +
            "import { Util } from '../../../../shared/utils/Util';\r\n" +
            "\r\n"+
            "import { RelatorioBaseComponent } from '../../shared/base/relatorio-base.component';\r\n" +
            "import { RelatorioBaseService } from '../../shared/base/relatorio-base.service';\r\n" +
            "\r\n" +
            "import { AcsDateTime } from '../../../../shared/utils/AcsDateTime';\r\n" +
            "import { Moment } from '../../../../shared/utils/Moment';\r\n";

        String bindProperties = "";
        String getParameters = "";
        String atributesOpcoes = "";
        List<ReportFileModel.ReportProperty> searchProperties = new ArrayList<>();

        // --------- Gerando os properties
        for(ReportFileModel.ReportProperty prop: reportModel.getProperties()) {
            // Properties ...
            String propLine = "";
            if(prop.getType().equalsIgnoreCase("SEARCH")) {
                propLine = "" +
                        "            " + prop.getFront().getGroup() + ": this.formBuilder.group({\r\n" +
                        "                id: [''" + ((prop.isRequired())? ", Validators.required" : "") + "],\r\n" +
                        "                descricao_" + prop.getFront().getGroup() + ": ['']\r\n" +
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
                        "        if("+getParamValue(prop)+" !== '') {\r\n" +
                        "    " + paramLine + "\r\n" +
                        "        }\r\n";
            }
            getParameters += paramLine + "\r\n";

            if(prop.getFront().getType().equalsIgnoreCase("SELECT")) {
                atributesOpcoes += getAtributeOpcao(prop);
            }
        }

        if(!atributesOpcoes.equals("")) {
            atributesOpcoes = "\r\n" + atributesOpcoes;
        }

        // --------- Gerando o openSearch
        String openSearchDialog = "";
        String entityService = "";
        if(searchProperties.size() > 0) {
            String importModels = "";
            String importServices = "";

            String searchIf = "";
            String searchDialog = "" +
                    "        this.searchDialog\r\n" +
                    "          .showDialog()\r\n" +
                    "          .subscribe((resp: SearchResponseModel) => {\r\n";
            // for(ReportFileModel.ReportProperty prop : searchProperties) {
            for (int i = 0; i < searchProperties.size(); i++) {
                ReportFileModel.ReportProperty prop = searchProperties.get(i);
                String label = prop.getFront().getLabel();
                String group = prop.getFront().getGroup();
                String entity = prop.getEntity();
                String entityVariable = entity.substring(0, 1).toLowerCase() + entity.substring(1);

                // ************ Faz o searchIf
                if(i == 0) {
                    searchIf += "        if(sourceControl === '" + group + "') {\r\n";
                }
                else {
                    searchIf += "\r\n" +
                            "        } else if(sourceControl === '" + group + "') {\r\n";
                }

                if(group.equals("empresa")) {
                    searchIf += "" +
                            "            const empresaDesign = [\r\n" +
                            "                new DialogDesignModel('Razão Social', 'razaoSocial'),\r\n" +
                            "                new DialogDesignModel('CNPJ', 'cnpj', false, 'cnpj', Util.formatCpfCnpj),\r\n" +
                            "                new DialogDesignModel('UF', 'uf')\r\n" +
                            "            ];\r\n" +
                            "            this.searchDialog.changeSearchValues('PESQUISAR Empresas', 'empresa', empresaDesign, this.empresaService,\r\n" +
                            "                            `/search`, `idUsuario=${this.usuario.id}&sort=razaoSocial`);\r\n";

                } else {
                    searchIf += "" +
                        "            const " + group + "Design = [\r\n" +
                        "                new DialogDesignModel('Id', '" + group + ".id'),\r\n" +
                        "                new DialogDesignModel('Descrição', '" + group + ".descricao')\r\n" +
                        "            ];\r\n" +
                        "            this.searchDialog.changeSearchValues('PESQUISAR "+label+"', sourceControl, "+group+"Design, this."+entityVariable+"Service,\r\n" +
                        "                            `/search`, ``);\r\n";
                }

                // ************ Faz o SearchDialog
                if(i == 0) {
                    searchDialog += "            if (resp.sourceControl === '"+group+"') {\r\n";
                }
                else {
                    searchDialog += "\r\n" +
                            "            } else if (resp.sourceControl === '"+group+"') {\r\n";
                }

                if(group.equals("empresa")) {
                    searchDialog +=
                        "                const result = resp.result as Empresa;\r\n" +
                        "                this.formModel.get('empresa.id').setValue(result.id);\r\n" +
                        "                this.formModel.get('empresa.descricao_empresa').setValue(`${result.razaoSocial} (${Util.formatCpfCnpj(result.cnpj)})`);\r\n";

                } else {
                    searchDialog +=
                        "                const result = resp.result as " + entity + ";\r\n" +
                        "                this.formModel.get('"+group+".id').setValue(result.id);\r\n" +
                        "                this.formModel.get('"+group+".descricao_"+group+"').setValue(`${result.descricao}`);\r\n";
                }

                entityService += "        public "+entityVariable+"Service: "+entity+"Service,\r\n";

                importModels += "import { " + entity + " } from '../../../../model/"+entity+"';\r\n";
                importServices += "import { "+entity+"Service } from '../../../../services/"+entityVariable+".service';\r\n";
            }

            searchIf += "        }\r\n";
            searchDialog += ""+
                    "            }\r\n" +
                    "        });\r\n";

            openSearchDialog = "\r\n" +
                    "    openSearchDialog(sourceControl: string) {\r\n" +
                    "        if (this.formModel.disabled) return; // Sai se estiver desabilitado\r\n" +
                    "\r\n" +
                    "        this.searchDialog.modal_lg = false;\r\n" +
                    searchIf +
                    "\r\n" +
                    searchDialog +
                    "    }\r\n";

            entityService += "\r\n";

            imports += "\r\n" +
                    importModels +
                    "\r\n" +
                    importServices;
        }

        // ----------------------- Creating Component
        String fileBody =
                imports +
                "\r\n" +
                "@Component({\r\n" +
                "    templateUrl: './"+options.defaultRoute+".component.html'\r\n" +
                "})\r\n" +
                "export class "+options.frontBaseName+"Component extends RelatorioBaseComponent implements OnInit {\r\n" +
                ((!openSearchDialog.equals(""))? "    @ViewChild('search_dialog') searchDialog: SearchDialogComponent;\r\n" : "") +
                atributesOpcoes +
                "    \r\n" +
                "    constructor(\r\n" +
                entityService +
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
                openSearchDialog +
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
            posFormElements += "\r\n\r\n" +
                    "<!-- SEARCH DIALOGS -->\r\n" +
                    "<dialogs_search class=\"search-relatorios-" + options.defaultRoute + "\" #search_dialog></dialogs_search>\r\n";
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

                    int comp = options.defaultRoute.compareTo(token);
                    if (comp < 0) {
                        writted = true;
                        writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    } else if (comp == 0) { // Se estiver repetindo (Mesmo nome do token)
                        writted = true;
                    }
                }
                else if (line.startsWith("];")) {
                    writted = true;
                    writeToFile(Paths.get(tmpFile), (newLine+"\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
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
    //endregion

    //region // --- Funções para criação de Domínio --- //
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

        System.out.println("Generated a linha '" + newLine);
        System.out.println("Into '" + pathToFile + "'");
        System.out.println("-----------------------------------------------");
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
            ret = "this.formModel.get('"+ prop.getFront().getGroup() + ".id').value";
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
        else if(front.getType().equalsIgnoreCase("SELECT")) {
            String opcoes = "opcoes" + prop.getName().substring(0,1).toUpperCase()+prop.getName().substring(1);

            div = "<div class=\"form-group col-6 col-sm-4 col-md-3 col-lg-2\">";
            matFormFieldInner = "<mat-form-field appearance=\"fill\">";

            matElement = "\t"  + "<mat-select formControlName=\"" + prop.getName() + "\" title=\"Escolher " + prop.getFront().getLabel() + "\">\n" +
                    spc + "\t\t\t"  + "<mat-option *ngFor=\"let opcao of "+opcoes+"\" [value]=\"opcao.value\"> {{opcao.name}} </mat-option>\n" +
                    spc + "\t\t" + "</mat-select>\r\n";
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
            div = "<div class=\"form-group col-12 col-md-4 col-lg-4\" formGroupName=\""+prop.getFront().getGroup()+"\" " + ((prop.isRequired())? "required" : "") + ">";
            matFormFieldInner = "<mat-form-field appearance=\"fill\" hideRequiredMarker=\"true\" floatLabel=\"always\" title=\""+front.getLabel()+"\" style=\"cursor: pointer;\" (click)=\"openSearchDialog('"+prop.getFront().getGroup()+"');\" >";

            matElement = "\t" + "<input matInput formControlName=\"descricao_" + prop.getFront().getGroup() + "\" style=\"cursor: pointer;\" readonly>" + "\r\n" +
                    "\r\n" +
                    spc + "\t\t" + "<button mat-button matSuffix mat-icon-button type=\"button\" aria-label=\"Search\">\r\n" +
                    spc + "\t\t\t" + "<mat-icon>search</mat-icon>\r\n" +
                    spc + "\t\t" + "</button>\r\n";

            errors = spc + "\t" + "<others_alert-errors [submitted]=\"submitted\" [control]=\"formModel.get('"+prop.getFront().getGroup()+".id')\"></others_alert-errors>\r\n";

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

        if(errors.equals("") && prop.isRequired() && !front.getType().equalsIgnoreCase("CHECKBOX") && !front.getType().equalsIgnoreCase("SELECT")) {
            errors = spc + "\t" + "<others_alert-errors [submitted]=\"submitted\" [control]=\"formModel.get('"+prop.getName()+"')\"></others_alert-errors>\r\n";
        }

        // Montando DIV //
        div = spc + div + "\r\n" +
              spc + matFormField + "\r\n" +
              errors +
              spc + "</div>\r\n";

        return spc + "<!-- " + prop.getName().toUpperCase() + " -->" + "\r\n" + div;
    }

    private String getAtributeOpcao(ReportFileModel.ReportProperty prop) {
        String opcoes   = "opcoes" + prop.getName().substring(0,1).toUpperCase()+prop.getName().substring(1);
        String opcaoRet = "    " + opcoes + " = [\r\n";
        if(prop.getType().equalsIgnoreCase("BOOLEAN")) {
            opcaoRet += "        { name: 'Sim', value: true },\r\n" +
                        "        { name: 'Não', value: false }\r\n";
        } else if(prop.getType().equalsIgnoreCase("STRING")) {
            opcaoRet += "        { name: 'Sim', value: 'S' },\r\n" +
                        "        { name: 'Não', value: 'N' }\r\n";
        } else {
            opcaoRet += "        { name: 'Opção 1', value: 0 },\r\n" +
                        "        { name: 'Opção 2', value: 1 }\r\n";
        }
        opcaoRet += "    ];\r\n";

        return opcaoRet;
    }

    private void writeToFile(Path path, byte[] bytes, StandardOpenOption... append) throws IOException {
        Files.write(path, bytes, append);
    }
}
