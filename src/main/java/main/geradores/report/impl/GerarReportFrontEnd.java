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
import java.util.Map;

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
        String domainName = options.getReportGenerator().getReportModel().getDominio();
        String roleDescription = options.getReportGenerator().getReportModel().getPermissao();

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
        String domainName = reportModel.getDominio();
        String title = reportModel.getTitulo();

        // --------- Gerando Imports
        String imports =
            "import { Component, OnInit, ViewChild } from '@angular/core';\r\n" +
            "import { Validators } from '@angular/forms';\r\n" +
            "\r\n" +
            "import { DialogDesignModel } from '../../../../shared/components/dialogs/custom-dialog/DialogDesignModel';\r\n" +
            "import { SearchDialogComponent, SearchResponseModel } from '../../../../shared/components/dialogs/search-dialog/search-dialog.component';\r\n" +
            "import { InputFilterComponent } from '../../../../shared/components/others/input-filter/input-filter.component';\r\n" +
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
        List<ReportFileModel.ReportProperty> filterProperties = new ArrayList<>();
        String incializarEmpresaProperty = "";

        // --------- Gerando os properties
        for(ReportFileModel.ReportProperty prop: reportModel.getPropriedades()) {
            // Properties (bindForm) ...
            String propLine = "";
            if(prop.getType().equalsIgnoreCase("SEARCH")) {
                propLine = "" +
                        "            " + prop.getFront().getGroup() + ": this.formBuilder.group({\r\n" +
                        "                id: [''" + ((prop.isRequired())? ", Validators.required" : "") + "],\r\n" +
                        "                descricao_" + prop.getFront().getGroup() + ": ['']\r\n" +
                        "            }),\r\n";
                searchProperties.add(prop);

            }
            else if(prop.getType().equalsIgnoreCase("FILTER")) {
                propLine = "" +
                        "            " + prop.getFront().getGroup() + ": [[]],\r\n";
                filterProperties.add(prop);

            }
            else {
                String value = prop.getValue();
                if(value == null && !prop.getType().equals("AcsDateTime")) {
                    value = "''";
                }
                else if((prop.getType().equalsIgnoreCase("String") || prop.getType().equalsIgnoreCase("Character"))) {
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
            if(prop.getType().equalsIgnoreCase("FILTER")) {
                paramLine =
                        "        filtro['" + prop.getFront().getGroup() + "'].forEach( element => {\r\n" +
                        "            filterList.push(`"+prop.getName()+"=${element.id}`);\r\n" +
                        "        });\r\n";

            } else if(!prop.isRequired()) {
                paramLine =
                        "        if("+getParamValue(prop)+" !== '') {\r\n" +
                        "    " + paramLine + "\r\n" +
                        "        }\r\n";
            }
            getParameters += paramLine + "\r\n";

            if(prop.getFront().getType().equalsIgnoreCase("SELECT") || prop.getFront().getType().equalsIgnoreCase("RADIO")) {
                atributesOpcoes += getAtributeOpcao(prop);
            }

            if(prop.getFront().getGroup().equals("empresa")) {
                incializarEmpresaProperty += "\r\n" +
                        "        " + "this.formModel.get('empresa.id').setValue(this.empresa.id);\r\n" +
                        "        " + "this.formModel.get('empresa.descricao_empresa').setValue(`${this.empresa.razaoSocial} (${Util.formatCpfCnpj(this.empresa.cnpj)})`);\r\n";
            }
        }

        if(!atributesOpcoes.equals("")) {
            atributesOpcoes = "\r\n" + atributesOpcoes;
        }

        // Atributos para o Search e o Filter
        String entityService = "";
        List<String> lsImportModels = new ArrayList<>();

        // --------- Gerando o openSearch
        String openSearchDialog = "";
        if(searchProperties.size() > 0) {
            String searchIf = "";
            String searchDialog = "" +
                    "        this.searchDialog\r\n" +
                    "          .showDialog()\r\n" +
                    "          .subscribe((resp: SearchResponseModel) => {\r\n";

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
                        "                new DialogDesignModel('Id', 'id'),\r\n" +
                        "                new DialogDesignModel('Descrição', 'descricao')\r\n" +
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

                if(!lsImportModels.contains(entity)) {
                    lsImportModels.add(entity);
                }
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
        }

        // --------- Gerando o initFilter
        String initFilterFunction = "";
        if(filterProperties.size() > 0) {
            String initFilterEntity = "";
            String filterAtributes = "\r\n    // ------ FILTERS ------ \r\n";

            boolean isFirst = true;
            for (ReportFileModel.ReportProperty prop: filterProperties) {
                if(!isFirst) {
                    initFilterEntity += "\r\n";
                }

                ReportFileModel.ReportProperty.PropertyFrontValue front = prop.getFront();
                String entity = prop.getEntity();

                filterAtributes += "    @ViewChild('"+ front.getGroup() + "Filter', {static: true}) "+front.getGroup()+"Filter: InputFilterComponent<"+ entity +">;\r\n";
                initFilterEntity += getInitFilterData(prop);

                if(!lsImportModels.contains(entity)) {
                    lsImportModels.add(entity);
                }

                isFirst = false;
            }

            atributesOpcoes += filterAtributes + "\r\n";

            initFilterFunction = "\r\n" +
                    "    iniciarFiltros() {\r\n" +
                    initFilterEntity +
                    "    }\r\n\r\n";
        }

        if(lsImportModels.size() > 0) {
            String importModels = "";
            String importServices = "";

            for(String entity: lsImportModels) {
                String entityVariable = entity.substring(0,1).toLowerCase() + entity.substring(1);

                importModels += "import { " + entity + " } from '../../../../model/"+entity+"';\r\n";
                importServices += "import { "+entity+"Service } from '../../../../services/"+getServiceName(entity)+".service';\r\n";
                entityService += "        public "+entityVariable+"Service: "+entity+"Service,\r\n";
            }

            entityService += "\r\n";

            imports += "\r\n" +
                    importModels +
                    "\r\n" +
                    importServices;
        }

        //region // ----------------------- Creating Component (fileBody)
        String fileBody =
                imports +
                "\r\n" +
                "@Component({\r\n" +
                "    templateUrl: './"+options.defaultRoute+".component.html'\r\n" +
                "})\r\n" +
                "export class "+options.frontBaseName+"Component extends RelatorioBaseComponent implements OnInit {\r\n" +
                ((!openSearchDialog.equals(""))? "    @ViewChild('search_dialog') searchDialog: SearchDialogComponent;\r\n" : "") +
                ((!atributesOpcoes.equals(""))? atributesOpcoes : "    \r\n") +
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
                ((!initFilterFunction.equals(""))? "\r\n        this.iniciarFiltros();\r\n" : "") +
                "    }\r\n" +
                "\r\n" +
                "    // Funções do Fomulário //\r\n" +
                "    bindFormValidators() {\r\n" +
                "        this.formModel = this.formBuilder.group({\n" +
                bindProperties +
                "        });\r\n" +
                incializarEmpresaProperty +
                "    }\r\n" +
                "\r\n" +
                initFilterFunction +
                "    // Funções de Filters //\r\n" +
                "    getParameters(): string {\r\n" +
                "        const filterList: string[] = [];\r\n" +
                "        const filtro = this.formModel.getRawValue();\r\n\r\n" +
                getParameters +
                "        return filterList.join('&');\r\n" +
                "    }\r\n" +
                openSearchDialog +
                "}\r\n";
        //endregion

        Utils.writeContentTo(path + options.defaultRoute + ".component.ts", fileBody);
        System.out.println("Generated Module '" + options.defaultRoute + ".component.ts' into '" + path + "'");
        System.out.println("-----------------------------------------------\r\n");
    }

    private void gerarTela(GenOptions options) throws IOException {
        String path = mainPath;
        ReportFileModel reportModel = options.getReportGenerator().getReportModel();

        String posFormElements = "";
        String filters = "";
        boolean hasSearchProperty = false;
        boolean hasFilterProperty = false;
        for(ReportFileModel.ReportProperty prop: reportModel.getPropriedades()) {
            filters += "\r\n" + getHtmlElement(prop);

            if(prop.getType().equalsIgnoreCase("SEARCH")) {
                hasSearchProperty = true;
            } else if(prop.getType().equalsIgnoreCase("FILTER")) {
                hasFilterProperty = true;
            }
        }

        if(hasSearchProperty) {
            posFormElements += "\r\n\r\n" +
                    "<!-- SEARCH DIALOGS -->\r\n" +
                    "<dialogs_search class=\"search-relatorios-" + options.defaultRoute + "\" #search_dialog></dialogs_search>";
        }

        if(hasFilterProperty) {
            posFormElements += "\r\n\r\n" +
                    "<!-- MODAL FILTER -->\r\n" +
                    "<dialogs_filter class=\"filter-relatorios-" + options.defaultRoute + "\" #filter_dialog></dialogs_filter>";
        }

        // ----------------------- Creating Component
        String fileBody =
                "<form [formGroup]=\"formModel\" data-toggle=\"validator\" role=\"form\">\r\n" +
                "    <others_panel [panelTitle]=\"title\" class=\"panel panel--report\">\r\n" +
                "        <div class=\"panel-content\">\r\n" +
                "            <acs_relatorio #relatorio_filter (doClear)=\"doClear();\" (visualizarRelatorio)=\"visualizarRelatorio();\">\r\n" +
                "                <div class=\"conteudo-sessao\">\r\n" +
                filters +
                "                </div>\r\n" +
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
        String domainName = options.getReportGenerator().getReportModel().getDominio();

        String pathToFile = mainPath + domainName + "-routing.module.ts";

        String newLine = "    { path: '"+options.defaultRoute+"', loadChildren: () => import('./"+options.defaultRoute+"/"+options.defaultRoute+".module).then(m => m."+options.frontBaseName+"Module) },";
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

        String domainName = options.getReportGenerator().getReportModel().getDominio();
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

        String domainName = options.getReportGenerator().getReportModel().getDominio();
        String domainClass = domainName.substring(0,1).toUpperCase() + domainName.substring(1);

        String newLine = "    { path: '"+domainName+"', loadChildren: () => import('./"+domainName+"/"+domainName+".module).then("+domainClass+"Module) },";
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
        return reportGenerator.getReportModel().getDominio() + "/";
    }

    private String getParamValue(ReportFileModel.ReportProperty prop) {
        String ret = "filtro['" + prop.getName() + "']";
        if(prop.getType().equalsIgnoreCase("AcsDateTime")) {
            ret = "Moment.format(" + ret + ")";
        } else if(prop.getType().equalsIgnoreCase("SEARCH")) {
            ret = "filtro['" + prop.getFront().getGroup() + "']['id']";
        }

        return ret;
    }

    private String getHtmlElement(ReportFileModel.ReportProperty prop) {
        String spc = "                    ";

        String div = "";
        String matFormField = "";
        String matFormFieldInner = "";
        String matLabel = "";
        String matElement = "";
        String errors = "";

        ReportFileModel.ReportProperty.PropertyFrontValue front = prop.getFront();

        // Material Form
        matFormFieldInner = "<mat-form-field appearance=\"fill\" hideRequiredMarker=\"true\" floatLabel=\"always\"";
        if(prop.isRequired() &&
           !(front.getType().equalsIgnoreCase("SELECT") || front.getType().equalsIgnoreCase("RADIO"))
        ) {
                matFormFieldInner += " required";
        }
        matFormFieldInner += ">";

        // HTML pelo Type -- Element And Label //
        matLabel = "\t" + "<mat-label><b>" + front.getLabel() + "</b></mat-label>" + "\r\n";
        if(front.getType().equalsIgnoreCase("INPUT")) {
            div = "<div class=\"form-group col-6\">";
            matElement = "\t" + "<input matInput formControlName=\"" + prop.getName() + "\" ";
            if(prop.getValue() != null) {
                matElement += "placeholder=\""+prop.getValue()+"\"";
            }
            matElement += ">\r\n";
        }
        else if(front.getType().equalsIgnoreCase("NUMBER")) {
            div = "<div class=\"form-group col-6\">";
            matElement = "\t" + "<input matInput formControlName=\"" + prop.getName() + "\" maxLength=\""+front.getInteiro()+"\" ";

            String mask = getNumberMask(front.getInteiro());
            if(front.getZerosLeft()) {
                matElement += "[numberMask]=\"{ mask: '"+mask+"', zerosLeft: true }\" ";
            } else {
                matElement += "numberMask=\""+mask.replaceAll("9", "0")+"\" ";
            }

            matElement += "placeholder=\""+mask.replaceAll("9", "0")+"\">\r\n";
        }
        else if(front.getType().equalsIgnoreCase("DECIMAL")) {
            div = "<div class=\"form-group col-6\">";
            matElement = "\t" + "<input matInput formControlName=\"" + prop.getName() + "\" maxLength=\""+(front.getInteiro()+front.getDecimal() + 1)+"\" ";

            String acsCurrency = "acsCurrencyMask [options]=\"{ prefix: ' ', precision: {min: " + front.getDecimal() + ", max: " + front.getDecimal() + "} }\"";

            String mask = "0," + getNumberMask(front.getDecimal());
            matElement += acsCurrency + " placeholder=\""+mask.replaceAll("9", "0")+"\">\r\n";
        }
        else if(front.getType().equalsIgnoreCase("DATE")) {
            div = "<div class=\"form-group col-6\">";
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

            div = "<div class=\"form-group col-6\">";
            matFormFieldInner = "<mat-form-field appearance=\"fill\">";

            matElement = "\t"  + "<mat-select formControlName=\"" + prop.getName() + "\" title=\"Escolher " + prop.getFront().getLabel() + "\">\n" +
                    spc + "\t\t\t"  + "<mat-option *ngFor=\"let opcao of "+opcoes+"\" [value]=\"opcao.value\"> {{opcao.name}} </mat-option>\n" +
                    spc + "\t\t" + "</mat-select>\r\n";
        }
        else if(front.getType().equalsIgnoreCase("RADIO")) {
            if(prop.getName().equals("tipoRelatorio")) {
                div = "<div class=\"form-group row col-12 m-0 p-0\">";
                matElement = "<div class=\"form-group col-8\">\r\n" +
                        spc + "\t\t" + "<mat-radio-group formControlName=\"tipoRelatorio\">\r\n" +
                        spc + "\t\t\t" + "<mat-radio-button *ngFor=\"let opcao of opcoesTipoRelatorio\" class=\"acs-radio-button\" [value]=\"opcao.value\">{{opcao.name}}</mat-radio-button>\r\n" +
                        spc + "\t\t" + "</mat-radio-group>\r\n" +
                        spc + "\t" + "</div>";

                matFormField = "\t" + "<label class=\"form-group col-4\"><b>Apresentação:</b></label>\r\n" +
                        spc + "\t" + matElement;

            } else {
                String opcoes = "opcoes" + prop.getName().substring(0,1).toUpperCase()+prop.getName().substring(1);

                div = "<div class=\"form-group mat-form-field col-12 m-0 p-0\">";

                matElement = ""  + "<mat-radio-group formControlName=\"" + prop.getName() + "\" class=\"col-6 m-0 p-0\">\r\n" +
                        spc + "\t\t" + "<label style=\"cursor: pointer;\" *ngFor=\"let opcao of " + opcoes + "\">\r\n" +
                        spc + "\t\t\t" + "<mat-radio-button [value]=\"opcao.value\" class=\"col-12\"> {{opcao.name}} </mat-radio-button>\r\n" +
                        spc + "\t\t" + "</label>\r\n" +
                        spc + "\t" + "</mat-radio-group>";

                matFormField = "\t" + "<label class=\"col-12\"><b>"+front.getLabel()+"</b></label>" + "\r\n" +
                        spc + "\t" + matElement;
            }
        }
        else if(front.getType().equalsIgnoreCase("CHECKBOX")) {
            div = "<div class=\"form-group mat-form-field col-6\">";

            if(prop.getType().equalsIgnoreCase("String")) {
                matElement = "\t" + "<mat-checkbox type=\"checkbox\" (change)=\"changeValue($event, '" + prop.getName() + "');\" [checked]=\"isCheked('" + prop.getName() + "')\" class=\"mr-1\"></mat-checkbox>" + "\r\n";
            } else {
                matElement = "\t" + "<mat-checkbox type=\"checkbox\" formControlName=\"" + prop.getName() + "\" class=\"mr-1\"></mat-checkbox>" + "\r\n";
            }

            matElement += spc + "\t\t" + "<b>" + front.getLabel() + "</b>\r\n";

            matFormField = "\t" + "<label style=\"cursor: pointer;\" >" + "\r\n" +
                    spc + "\t" + matElement +
                    spc + "\t" + "</label>";

        }
        else if(front.getType().equalsIgnoreCase("SEARCH")) {
            div = "<div class=\"form-group col-6\" formGroupName=\""+prop.getFront().getGroup()+"\" " + ((prop.isRequired())? "required" : "") + ">";
            matFormFieldInner = "<mat-form-field appearance=\"fill\" hideRequiredMarker=\"true\" floatLabel=\"always\" title=\""+front.getLabel()+"\" style=\"cursor: pointer;\" (click)=\"openSearchDialog('"+prop.getFront().getGroup()+"');\" >";

            matElement = "\t" + "<input matInput formControlName=\"descricao_" + prop.getFront().getGroup() + "\" style=\"cursor: pointer;\" readonly>" + "\r\n" +
                    "\r\n" +
                    spc + "\t\t" + "<button mat-button matSuffix mat-icon-button type=\"button\" aria-label=\"Search\">\r\n" +
                    spc + "\t\t\t" + "<mat-icon>search</mat-icon>\r\n" +
                    spc + "\t\t" + "</button>\r\n";

            errors = spc + "\t" + "<others_alert-errors [submitted]=\"submitted\" [control]=\"formModel.get('"+prop.getFront().getGroup()+".id')\"></others_alert-errors>\r\n";

        }
        else if(front.getType().equalsIgnoreCase("FILTER")) {
            String frontGroupName = prop.getFront().getGroup();
            div = ""; // Nap usa DIV nos filtros
            matFormField = "<others_input-filter #" + frontGroupName + "Filter [control]=\"formModel.get('" + frontGroupName + "')\" [filterDialogComponent]=\"filter_dialog\" class=\"col-6\"></others_input-filter>";

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

        if(errors.equals("") && prop.isRequired() &&
                !front.getType().equalsIgnoreCase("CHECKBOX") &&
                !front.getType().equalsIgnoreCase("SELECT") &&
                !front.getType().equalsIgnoreCase("RADIO") &&
                !front.getType().equalsIgnoreCase("FILTER") ) {
            errors = spc + "\t" + "<others_alert-errors [submitted]=\"submitted\" [control]=\"formModel.get('"+prop.getName()+"')\"></others_alert-errors>\r\n";
        }

        // Montando DIV //
        if(div.equals("")) {
            div = spc + matFormField + "\r\n\r\n";

        } else {
            div = spc + div + "\r\n" +
                  spc + matFormField + "\r\n" +
                  errors +
                  spc + "</div>\r\n";
        }

        return spc + "<!-- " + prop.getName().toUpperCase() + " -->" + "\r\n" + div;
    }

    private String getInitFilterData(ReportFileModel.ReportProperty prop) {
        String spc = "        ";
        String entity = prop.getEntity();
        String entityVariable = entity.substring(0,1).toLowerCase() + entity.substring(1);
        ReportFileModel.ReportProperty.PropertyFrontValue front = prop.getFront();

        return
                spc + "////////////////////////////////////////////////////////\r\n" +
                spc + "// Filtro de " + entity + " ("+ front.getLabel() +")\r\n" +
                spc + "this."+front.getGroup()+"Filter.label = '" + front.getLabel() + "';\r\n" +
                spc + "this."+front.getGroup()+"Filter.dialog_title = 'Filtrar "+front.getLabel()+"';\r\n" +
                spc + "this."+front.getGroup()+"Filter.service = this."+entityVariable+"Service;\r\n" +
                spc + "this."+front.getGroup()+"Filter.urlAction = `/filter/empresa/${this.empresa.id}`;\r\n" +
                spc + "this."+front.getGroup()+"Filter.dataParameter = ``;\r\n" +
                spc + "this."+front.getGroup()+"Filter.table_design = [\r\n" +
                spc + "    new DialogDesignModel('Id', 'id'),\r\n" +
                spc + "    new DialogDesignModel('Descrição', 'descricao')\r\n" +
                spc + "];\n" +
                spc + "this."+front.getGroup()+"Filter.input_transform_descriptor = {\r\n" +
                spc + "    id: 'id',\r\n" +
                spc + "    title: [ { prefix: '', field: 'descricao' } ]\r\n" +
                spc + "};\r\n";
    }

    private String getNumberMask(Integer inteiro) {
        String ret = "";
        for(int i = 0; i < inteiro; i++) {
            ret += "9";
        }
        return ret;
    }

    private String getAtributeOpcao(ReportFileModel.ReportProperty prop) {
        String opcoes   = "opcoes" + prop.getName().substring(0,1).toUpperCase()+prop.getName().substring(1);
        String opcaoRet = "    " + opcoes + " = [\r\n";
        if(prop.getFront().getOptions().size() > 0) { // Usa opções customizadas
            opcaoRet = "    " + opcoes + " = [";
            Map<String, String> options = prop.getFront().getOptions();

            boolean isFirst = true;
            for (Object value: options.keySet()) {
                if(!isFirst) {
                    opcaoRet +=",";
                }

                String name = options.get(value);
                opcaoRet += "\r\n        { name: '" + name + "', value: '"+value+"' }";
                isFirst = false;
            }
            opcaoRet += "\r\n";

        } else if(prop.getType().equalsIgnoreCase("BOOLEAN")) {
            opcaoRet += "        { name: 'Sim', value: true },\r\n" +
                        "        { name: 'Não', value: false }\r\n";
        } else if(prop.getType().equalsIgnoreCase("STRING") || prop.getType().equalsIgnoreCase("CHARACTER")) {
            opcaoRet += "        { name: 'Sim', value: 'S' },\r\n" +
                        "        { name: 'Não', value: 'N' }\r\n";
        } else {
            opcaoRet += "        { name: 'Opção 1', value: 0 },\r\n" +
                        "        { name: 'Opção 2', value: 1 }\r\n";
        }
        opcaoRet += "    ];\r\n\r\n";

        return opcaoRet;
    }

    private String getServiceName(String entity) {
        String out = "";
        boolean isFirst = true;
        for(char l: entity.toCharArray()) {
            String letra = l + "";
            if(isFirst) {
                out += letra.toLowerCase();
                isFirst = false;
                continue;
            }

            if(letra.equals(letra.toLowerCase())) {
                out += letra;
            } else {
                out += "-" + letra.toLowerCase();
            }
        }

        return out;
    }

    private void writeToFile(Path path, byte[] bytes, StandardOpenOption... append) throws IOException {
        Files.write(path, bytes, append);
    }
}
