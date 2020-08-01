package main.geradores.model.impl;

import main.geradores.GenOptions;
import main.geradores.IGerador;
import main.geradores.Utils;
import main.geradores.model.ModelGenerator;
import main.geradores.model.utils.Property;
import main.geradores.model.utils.PropertyType;
import main.geradores.model.utils.Reference;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GerarFrontEnd implements IGerador {
    protected static String mainPath = "..\\front\\src\\app\\";

    @Override
    public void gerarArquivos(GenOptions options) throws IOException {
        System.out.println("===============================================");
        System.out.println("=========== GERANDO FRONT END =================");
        if (options.mainFront != null) {
            mainPath = options.mainFront;
        } else {
            options.mainFront = mainPath;
        }

        if (options.onlyBackEnd) {
            System.out.println("Pulando a geração dos arquivos para o FrontEnd ...");
        }
        else {
            if (options.generateModel) {
                gerarModelo(options);
                gerarServico(options);
            }

            // Verifica se deve gerar todos arquivos mais do front end ou não
            if (options.fullFrontEnd) {
                if (options.frontModuleName.equalsIgnoreCase("cadastros")) {
                    GerarFrontEnd.mainPath += "cadastros\\" + options.frontBaseFolder + "\\";
                } else {
                    GerarFrontEnd.mainPath += "modulos\\" + options.frontModuleName + "\\" + options.frontBaseFolder + "\\";
                }

                // Criar pasta do novo módulo
                Utils.createDirectory(GerarFrontEnd.mainPath);
                if (!options.frontModuleName.contains("\\")) { // So gera se não tiver sub-modulos
                    gerarModule(options);
                    gerarRoutingModule(options);
                } else {
                    System.out.println("Pulando geracao do Module e RoutingModule, pois trata-se de um sub-modulo");
                    System.out.println("------------------------------------------------------------------------------\r\n");
                }

//                // Listagem
//                Utils.createDirectory(GerarFrontEnd.mainPath + "listar-" + options.frontBaseFolder + "\\");
//                gerarTelaListar(options);
//                gerarSassListar(options);
//                gerarComponentListar(options);
//
//                // Criar / Editar
//                Utils.createDirectory(GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\");
//                gerarTelaCriarEditar(options);
//                gerarSassCriarEditar(options);
//                gerarComponentCriarEditar(options);
//
//                // Criar
//                Utils.createDirectory(GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\criar-" + options.frontBaseFolder);
//                gerarComponentCriar(options);
//
//                // Editar
//                Utils.createDirectory(GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\editar-" + options.frontBaseFolder);
//                gerarComponentEditar(options);
//
//                // Incluir o novo modulo no Routing Module Escolhido
//                if (!options.frontModuleName.contains("\\")) { // So gera se não tiver sub-modulos
//                    incluirCadastroModulo(options);
//                } else {
//                    System.out.println("Pulando inclusão nas rotas PADRAO, pois trata-se de um sub-modulo");
//                    System.out.println("------------------------------------------------------------------------------\r\n");
//                }
            }
        }

        System.out.println();
        System.out.println("===============================================");
        System.out.println("===============================================");
    }

    protected void incluirCadastroModulo(GenOptions options) throws IOException {
        String pathToFile = options.mainFront;

        if (options.frontModuleName.equalsIgnoreCase("cadastros")) {
            pathToFile += "cadastros\\";
        } else {
            pathToFile += "modulos\\" + options.frontModuleName + "\\";
        }

        pathToFile += options.frontModuleName + "-routing.module.ts";

        String newLine = "  { path: '" + options.frontBaseFolder + "', loadChildren: './" + options.frontBaseFolder + "/" + options.frontBaseFolder + ".module#" + options.frontBaseName + "Module' }";
        String tmpFile = "./tmp.txt";

        if (Utils.isAuditionMode()) {
            System.out.println("PATH     => " + pathToFile);
            System.out.println("New LINE => " + newLine);
            return;
        }

        new File(tmpFile).createNewFile();

        // 1 - Inicia lendo o arquivo de cadastros.module
        Path readModule = Paths.get(pathToFile);
        List<String> lines = Files.readAllLines(readModule);
        boolean writted = false;
        boolean printComma = false;
        for (String line : lines) {
            printComma = false;
            if (!writted) {
                // 2 - Quando estiver lendo as ROTAS
                //   2.1 - Verifica a ordem pelo nome do PATH e inclui a newLine no local certo
                //   * Verificar se está no início ou no final do array de rotas
                if (line.startsWith("  { path:")) {
                    // 2.2 -> Verifica a Ordem alfabética
                    String[] tokens = line.split("'");

                    int comp = options.frontBaseFolder.compareTo(tokens[1]);
                    if (comp < 0) {
                        writted = true;
                        writeToFile(Paths.get(tmpFile), (newLine + ",\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    } else if (comp == 0) { // Se estiver repetindo (Mesmo nome do token)
                        writted = true;
                    } else {
                        if (line.endsWith(",") == false) {
                            printComma = true;
                        }
                    }

                } else if (line.startsWith("];")) { // FIM DO ARQUIVO
                    writted = true;
                    writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }

            writeToFile(Paths.get(tmpFile), (line + ((printComma) ? "," : "") + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

        Files.delete(readModule);
        new File(tmpFile).renameTo(new File(pathToFile));
    }

    protected void writeToFile(Path path, byte[] bytes, StandardOpenOption... append) throws IOException {
        Files.write(path, bytes, append);
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    protected void gerarModelo(GenOptions options) throws IOException {
        boolean pkClass = false; // options.generateEmpresaEntity

        String path = GerarFrontEnd.mainPath + "model\\";
        String classBody = "";

        if (options.getModelGenerator() != null) {
            classBody = getClassBodyFromModelGenerator(options);

        } else {
            classBody = "" +
                    "export class " + options.entityName + " {\r\n" +
                    "    id" + options.entityName + ": number;\r\n" +
                    "\r\n" +
                    "  // ################ JOINS ################\r\n" +
                    ((pkClass)
                            ? "  empresa: Empresa;\r\n"
                            : "") +
                    "\r\n" +
                    "  // ################ PROPERTIES ################\r\n" +
                    "  descricao: string;\r\n" +
                    "}"
            ;
        }

        Utils.writeContentTo(path + options.entityName + ".ts", classBody);
        System.out.println("Generated Model '" + options.entityName + ".ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    private String getClassBodyFromModelGenerator(GenOptions options) {
        ModelGenerator modelGen = options.getModelGenerator();

        String imports = "";
        String pks = "";
        String joins = "";
        String properties = "";
        String constructor = "";

        Set<Reference> joinsComposto = new LinkedHashSet<>();

        // Listando as propriedades do modelGen
        for (Property prop : modelGen.getProperties()) {
            if (prop.getType() == PropertyType.JOIN) { // joins and imports
                Reference ref = modelGen.getJoinReferenceFromProperty(prop);

                String strImport = "import { " + ref.getClassName() + " } from './" + ref.getClassName() + "';\r\n";
                if (imports.contains(strImport) == false)
                    imports += strImport;

                joins += "  " + ref.getFormatVariableName(prop.getName()) + ": " + ref.getClassName() + ";\r\n";

            } else if (prop.getType() == PropertyType.JOIN_COMPOSTO) { // joins compostos and imports
                Reference ref = modelGen.getReferenceFromProperty(prop);

                if (joinsComposto.contains(ref) == false) {
                    String strImport = "import { " + ref.getClassName() + " } from './" + ref.getClassName() + "';\r\n";
                    if (imports.contains(strImport) == false)
                        imports += strImport;

                    joins += "  " + ref.getFormatVariableName(prop.getName()) + ": " + ref.getClassName() + ";\r\n";

                    joinsComposto.add(ref);
                }

            } else if (prop.getType() == PropertyType.JOIN_COMPOSTO_CHAVE) { // joins compostos de chave
                // JOINS COMPOSTO DE CHAVE não são representados como variáveis ou chave, somente sendo representdo no próprio BANCO
            } else if (prop.isPrimaryKey()) { // pks
                pks += "  " + prop.getVariableName() + ": " + normalizaTipoModel(prop.getType()) + ";\r\n";
            } else { // other properties
                if(prop.getType().equals(PropertyType.AUDITORIA)) continue;

                properties += "  " + prop.getVariableName() + ": " + normalizaTipoModel(prop.getType()) + ";\r\n";
            }
        }

        // ACS DATE TIME
        String datas = "";
        List<Property> lsDateTime = modelGen.getProperties().stream()
                .filter(ele -> ele.getType().equals(PropertyType.ACS_DATE_TIME))
                .collect(Collectors.toList());

        if (lsDateTime.size() != 0) {
            imports += "import { AcsDateTime } from '../shared/utils/AcsDateTime';\r\n";

            String reduceDatas = lsDateTime.stream()
                    .map(ele -> {
                        return "      " + "this." + ele.getVariableName() + " = new AcsDateTime(object['" + ele.getVariableName() + "']);";
                    })
                    .reduce((dataA, dataB) -> {
                        return dataA + "\r\n" + dataB;
                    }).orElse("");
            if (reduceDatas.equals("") == false) {
                datas = "\r\n" + reduceDatas + "\r\n";
            }
        }

        constructor += "\r\n" +
                "  " + "constructor(object?) {\r\n" +
                "    " + "if (object) { \r\n" +
                "      " + "Object.assign(this, object);\r\n" +
                datas +
                "    " + "}\r\n" +
                "  " + "}\r\n"
        ;

        String classBody = imports +
                "\r\n" +
                "export class " + options.entityName + " {\r\n" +
                pks +
                "\r\n" +
                "  // ################ JOINS ################\r\n" +
                joins +
                "\r\n" +
                "  // ################ PROPERTIES ################\r\n" +
                properties +
                constructor +
                "}";

        return classBody;
    }

    // ----------------------------------------------------------------------------------------- //
    protected void gerarServico(GenOptions options) throws IOException {
        boolean pkClass = false;

        String path = GerarFrontEnd.mainPath + "services\\";

        String classBody = "import { Injectable } from '@angular/core';\r\n" +
                "import { AuthHttp } from 'angular2-jwt';\r\n" +
                "\r\n" +
                ((pkClass)
                        ? "import { AbstractServiceEmpresa } from './abstract-service-empresa';\r\n"
                        : "import { AbstractService } from './abstract-service';\r\n") +
                "import { " + options.entityName + " } from '../model/" + options.entityName + "';\r\n" +
                "\r\n" +
                "@Injectable({providedIn: 'root'})\r\n" +
                ((pkClass)
                        ? "export class " + options.entityName + "Service extends AbstractServiceEmpresa<" + options.entityName + "> {\r\n"
                        : "export class " + options.entityName + "Service extends AbstractService<" + options.entityName + "> {\r\n") +
                "  constructor( private http: AuthHttp ) {\r\n" +
                "    super(http, '" + options.defaultRoute + "', 20);\r\n" +
                "  }\r\n" +
                "\r\n" +
                "  getDefaultFilterParam(param: string): string {\r\n" +
                "    return ``;\r\n" +
                "  }\r\n" +
                "}";

        Utils.writeContentTo(path + options.defaultRoute + ".service.ts", classBody);
        System.out.println("Generated Service '" + options.defaultRoute + ".service.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    protected void gerarModule(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath;

        String classBody =
                "import { NgModule } from '@angular/core';\r\n" +
                "import { CommonModule } from '@angular/common';\r\n" +
                "\r\n" +
                "import { UtilsModule } from '../../../shared/utils/utils.module';\r\n" +
                "import { MaterialModule } from '../../../shared/utils/material-module.module';\r\n" +
                "import { SharedComponentsModule } from '../../../shared/components/shared-components.module';\r\n" +
                "import { SharedDirectivesModule } from '../../../shared/directive/shared-directives.module';\r\n" +
                "\r\n" +
                "import { " + options.frontBaseName + "RoutingModule } from './" + options.frontBaseFolder + "-routing.module';\r\n" +
                "\r\n" +
                "import { Listar" + options.frontBaseName + "Component } from './listar-" + options.frontBaseFolder + "/listar-" + options.frontBaseFolder + ".component';\r\n" +
                "import { Criar" + options.frontBaseName + "Component } from './criar-editar-" + options.frontBaseFolder + "/criar-" + options.frontBaseFolder + "/criar-" + options.frontBaseFolder + ".component';\r\n" +
                "import { Editar" + options.frontBaseName + "Component } from './criar-editar-" + options.frontBaseFolder + "/editar-" + options.frontBaseFolder + "/editar-" + options.frontBaseFolder + ".component';\r\n" +
                "\r\n" +
                "@NgModule({\r\n" +
                "  imports: [" + "\r\n" +
                "    CommonModule,\r\n" +
                "    MaterialModule,\r\n" +
                "    // MaterialDateModule,\r\n" +
                "    UtilsModule,\r\n" +
                "\r\n" +
                "    SharedComponentsModule,\r\n" +
                "    SharedDirectivesModule,\r\n" +
                "    // SharedPipesModule,\r\n" +
                "\r\n" +
                "    " + options.frontBaseName + "RoutingModule\r\n" +
                "  ],\r\n" +
                "  declarations: [" + "Listar" + options.frontBaseName + "Component, " + "Criar" + options.frontBaseName + "Component, " + "Editar" + options.frontBaseName + "Component]\r\n" +
                "})\r\n" +
                "export class " + options.frontBaseName + "Module { }";

        Utils.writeContentTo(path + options.frontBaseFolder + ".module.ts", classBody);
        System.out.println("Generated Module '" + options.frontBaseFolder + ".module.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    protected void gerarRoutingModule(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath;

        String classBody = "import { NgModule } from '@angular/core';\r\n" +
                "import { Routes, RouterModule } from '@angular/router';\r\n" +
                "import { AuthGuard } from '../../../general/general-interceptors/guard/auth-guard';\r\n" +
                "\r\n" +
                "import { Listar" + options.frontBaseName + "Component } from './listar-" + options.frontBaseFolder + "/listar-" + options.frontBaseFolder + ".component';\r\n" +
                "import { Criar" + options.frontBaseName + "Component } from './criar-editar-" + options.frontBaseFolder + "/criar-" + options.frontBaseFolder + "/criar-" + options.frontBaseFolder + ".component';\r\n" +
                "import { Editar" + options.frontBaseName + "Component } from './criar-editar-" + options.frontBaseFolder + "/editar-" + options.frontBaseFolder + "/editar-" + options.frontBaseFolder + ".component';\r\n" +
                "\r\n" +
                "// rota padrao = /" + options.frontModuleName + "/" + options.frontBaseFolder + "\r\n" +
                "const routes: Routes = [\r\n" +
                "  { path: '', component: Listar" + options.frontBaseName + "Component, canActivate: [AuthGuard], data: { roles: ['ACESSAR " + options.accessAlias + "'] }},\r\n" +
                "  { path: 'create', component: Criar" + options.frontBaseName + "Component, canActivate: [AuthGuard], data: { roles: ['INCLUIR " + options.accessAlias + "'] }},\r\n" +
                "  { path: ':id" + options.entityName + "', component: Editar" + options.frontBaseName + "Component, canActivate: [AuthGuard], data: { }},\r\n" +
                "];\r\n" +
                "\r\n" +
                "@NgModule({\r\n" +
                "  imports: [RouterModule.forChild(routes)],\r\n" +
                "  exports: [ ]\r\n" +
                "})\r\n" +
                "export class " + options.frontBaseName + "RoutingModule { }";

        Utils.writeContentTo(path + options.frontBaseFolder + "-routing.module.ts", classBody);
        System.out.println("Generated Routing Module '" + options.frontBaseFolder + "-routing.module.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    // ----------------------------- FAZENDO A LISTAGEM -----------------------------
    protected void gerarTelaListar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "listar-" + options.frontBaseFolder + "\\";
        String serviceName = options.entityName + "Service";
        serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1);

        String classBody = "" +
                "<form [formGroup]=\"formModel\" data-toggle=\"validator\" role=\"form\">\r\n" +
                "    <others_panel>" + "\r\n" +
                "      <div class=\"panel-header m-0 row\">" + "\r\n" +
                "        <h2 class=\"mr-auto mb-0\">{{ title }}</h2>" + "\r\n" +
                "        <crud_create [enabled]=\"auth.hasIncludePermission()\"></crud_create>" + "\r\n" +
                "      </div>" + "\r\n" +
                "\r\n" +
                "      <div class=\"panel-content mat-elevation-z8 pb-1\">" + "\r\n" +
                "        <table_data-simple #table class=\"listar-" + options.frontBaseFolder + "\" [data_service]=\"" + serviceName + "\" sortActive=\"id\" >" + "\r\n" +
                "          <td acsTableColumn=\"id\" *tableColumn=\"'id'; header:'Cod.'; sort_header: true; let row\">{{ row.id }}</td>" + "\r\n" +
                "\r\n" +
                "          <!-- EDIT AND DELETE ROW -->" + "\r\n" +
                "          <td acsTableColumn=\"edit_delete\" [sticky]=\"true\" tabindex=\"-1\" *tableColumn=\"'edit_delete'; let row\">" + "\r\n" +
                "            <crud_edit [entity_id]=\"row.id\"></crud_edit>" + "\r\n" +
                "            <crud_delete (click)=\"modelAtual = row\" [enabled]=\"auth.hasUpdatePermission()\"></crud_delete>" + "\r\n" +
                "          </td>" + "\r\n" +
                "        </table_data-simple>" + "\r\n" +
                "      </div>" + "\r\n" +
                "    </others_panel>" + "\r\n" +
                "</form>\r\n" +
                "\r\n" +
                "<!-- Modal -->" + "\r\n" +
                "<dialogs_delete bodyText=\"Deseja excluir o MODELO '{{ modelAtual?.id }}'?\" (delete_button_click)=\"deleteModel()\"></dialogs_delete>" + "\r\n" +
                "";

        Utils.writeContentTo(path + "listar-" + options.frontBaseFolder + ".component.html", classBody);
        System.out.println("Generated LIST COMPONENT HTML '" + "listar-" + options.frontBaseFolder + ".component.html' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    protected void gerarSassListar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "listar-" + options.frontBaseFolder + "\\";

        String classBody = "" +
                ".listar-" + options.frontBaseFolder + " {" + "\r\n" +
                "  .mat-column-id {" + "\r\n" +
                "    text-align: center;" + "\r\n" +
                "  }" + "\r\n" +
                "}" + "\r\n";

        Utils.writeContentTo(path + "listar-" + options.frontBaseFolder + ".component.scss", classBody);
        System.out.println("Generated LIST COMPONENT SASS '" + "listar-" + options.frontBaseFolder + ".component.scss' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    protected void gerarComponentListar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "listar-" + options.frontBaseFolder + "\\";

        String serviceName = options.entityName + "Service";
        serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1);

        String classBody = "import { Component, OnInit } from '@angular/core';" + "\r\n" +
                "\r\n" +
                "import { " + options.entityName + " } from '../../../../model/" + options.entityName + "';" + "\r\n" +
                "import { " + options.entityName + "Service } from '../../../../services/" + options.defaultRoute + ".service';" + "\r\n" +
                "\r\n" +
                "import { ListagemBaseComponent } from '../../../shared/base/listagem-base-component';\r\n" +
                "import { ListagemBaseService } from '../../../shared/base/listagem-base.service';\r\n" +
                "\r\n" +
                "@Component({" + "\r\n" +
                "  templateUrl: './listar-" + options.frontBaseFolder + ".component.html'," + "\r\n" +
                "  styleUrls: ['./listar-" + options.frontBaseFolder + ".component.scss']" + "\r\n" +
                "})\r\n" +
                "export class Listar" + options.frontBaseName + "Component extends ListagemBaseComponent implements OnInit {" + "\r\n" +
                "  modelAtual: " + options.entityName + ";" + "\r\n" +
                "\r\n" +
                "  constructor(\r\n" +
                "    public " + serviceName + ": " + options.entityName + "Service,\r\n" +
                "    \r\n" +
                "    public baseServices: ListagemBaseService" + "\r\n" +
                "  ) { super(baseServices); }\r\n" +
                "\r\n" +
                "  ngOnInit(): void {\r\n" +
                "     super.ngOnInit();\r\n" +
                "     this.title = '" + options.entityName + "';" + "\r\n" +
                "     this.auth.cadastroPermission = '" + options.accessAlias + "';" + "\r\n" +
                "  }\r\n" +
                "\r\n" +
                "  bindFormValidators(): void {\r\n" +
                "    super.bindFormValidatorsPadrao();\r\n" +
                "  }\r\n" +
                "\r\n" +
                "  getParametros(): string {\r\n" +
                "    throw new Error('Method not implemented.');\r\n" +
                "  }\r\n" +
                "\r\n" +
                "  deleteModel() {" + "\r\n" +
                "    this." + serviceName + "\r\n" +
                "        .delete(this.modelAtual.id)" + "\r\n" +
                "        .then(() => {" + "\r\n" +
                "          this.table.filterDataTable( (dataTable: " + options.entityName + "[]): " + options.entityName + "[] => {" + "\r\n" +
                "            return dataTable.filter( ele => ele.id !== this.modelAtual.id );" + "\r\n" +
                "          });" + "\r\n" +
                "\r\n" +
                "          this.toasty.success(`MODELO '${this.modelAtual.id}' excluído com sucesso!`);" + "\r\n" +
                "        }).catch( erro => this.errorHandler.handle(erro) );" + " \r\n" +
                "  }" + "\r\n" +
                "}" + "\r\n";

        Utils.writeContentTo(path + "listar-" + options.frontBaseFolder + ".component.ts", classBody);
        System.out.println("Generated LIST COMPONENT TS '" + "listar-" + options.frontBaseFolder + ".component.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    // --------------------------- FAZENDO CRIAR - EDITAR ---------------------------
    protected void gerarTelaCriarEditar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\";

        String classBody = "" +
                "<form [formGroup]=\"formModel\" data-toggle=\"validator\" role=\"form\">" + "\r\n" +
                "  <others_group-panel [titleGroup]=\"title\"> <!-- GROUP PANEL -->" + "\r\n" +
                "    <others_panel [panelTitle]=\"title\"> <!-- PANEL GERAL -->" + "\r\n" +
                "      <div class=\"panel-content row m-0\">" + "\r\n" +
                "        <!-- CODIGO -->" + "\r\n" +
                "        <div class=\"form-group col-4 col-sm-2 col-md-2\" >" + "\r\n" +
                "          <mat-form-field appearance=\"fill\" hideRequiredMarker=\"true\">" + "\r\n" +
                "            <mat-label>Código</mat-label>" + "\r\n" +
                "            <input matInput formControlName=\"id\" title=\"Código " + options.entityName + "\">" + "\r\n" +
                "          </mat-form-field>" + "\r\n" +
                "        </div>" + "\r\n" +
                "      </div>" + "\r\n" +
                "    </others_panel>" + "\r\n" +
                "  </others_group-panel>" + "\r\n" +
                "</form>" + "\r\n" +
                "\r\n" +
                "<!-- TOOL BOX -->" + "\r\n" +
                "<others_tool-box #toolbox [formModel]=\"formModel\" [route]=\"routerLink\" [salvarEditarText]=\"salvarEditarText\" (submitAction)=\"gravarFormModel();\"></others_tool-box>" + "\r\n" +
                "";

        Utils.writeContentTo(path + "criar-editar-" + options.frontBaseFolder + ".component.html", classBody);
        System.out.println("Generated CREATE/EDIT COMPONENT HTML '" + "criar-editar-" + options.frontBaseFolder + ".component.html' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    protected void gerarSassCriarEditar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\";

        String classBody = "";

        Utils.writeContentTo(path + "criar-editar-" + options.frontBaseFolder + ".component.scss", classBody);
        System.out.println("Generated CREATE/EDIT COMPONENT CSS '" + "criar-editar-" + options.frontBaseFolder + ".component.scss' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    protected void gerarComponentCriarEditar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\";

        String serviceName = options.entityName + "Service";
        serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1);

        ModelGenerator modelGen = options.getModelGenerator();
        String bindFormValidators = "";

        // Listando as propriedades do modelGen
        for (Property prop : modelGen.getProperties()) {
            if (bindFormValidators.equals("") == false) {
                bindFormValidators += "," + "\r\n";
            }

            if (prop.getType() == PropertyType.JOIN) { // joins
                Reference ref = modelGen.getJoinReferenceFromProperty(prop);

                bindFormValidators += "  " +
                        "    " + ref.getFormatVariableName(prop.getName()) + ": this.formBuilder.group({" + "\r\n" +
                        "        id: ['']" + "\r\n" +
                        "      })";

            } else if (prop.getType() == PropertyType.JOIN_COMPOSTO) { // joins compostos and imports
                // NAO HÁ MAIS JOIN COMPOSTOS !!
            } else if (prop.getType() == PropertyType.JOIN_COMPOSTO_CHAVE) { // joins compostos de chave
                // JOINS COMPOSTO DE CHAVE não são representados como variáveis ou chave, somente sendo representdo no próprio BANCO !
            } else if (prop.isPrimaryKey()) { // pks
                bindFormValidators += "      id: ['']";
            } else { // other properties
                if(prop.getType().equals(PropertyType.AUDITORIA)) continue;

                bindFormValidators += "      " + prop.getVariableName() + ": [" + normalizaInicioModel(prop.getType()) + ((prop.isNullable() == false) ? ", Validators.required" : "") + "]";
            }
        }

        String classBody = "" +
                "import { OnInit, ViewChild } from '@angular/core';\r\n" +
                "import { Validators } from '@angular/forms';\r\n" +
                "\r\n" +
                "import { CadastroBaseComponent } from '../../../../cadastros/cadastro-base.component';\r\n" +
                "import { CadastroBaseService } from '../../../../cadastros/cadastro-base.service';\r\n" +
                "\r\n" +
                "import { " + options.entityName + "Service } from '../../../../services/" + options.defaultRoute + ".service';\r\n" +
                "\r\n" +
                "export abstract class CriarEditar" + options.frontBaseName + "Component extends CadastroBaseComponent implements OnInit {\r\n" +
                "  id" + options.entityName + ": number;" + "\r\n" +
                "\r\n" +
                "  constructor(\r\n" +
                "    public " + serviceName + ": " + options.entityName + "Service,\r\n" +
                "\r\n" +
                "    public baseServices: CadastroBaseService\r\n" +
                "  ) { super('" + options.accessAlias + "', baseServices); }\r\n" +
                "\r\n" +
                "  ngOnInit() {\r\n" +
                "    super.ngOnInit();\r\n" +
                "\r\n" +
                "    this.title = 'Criar Novo " + options.entityName + "';\r\n" +
                "    this.routerLink = '/" + options.frontModuleName.replaceAll("[\\\\]", "/") + "/" + options.frontBaseFolder + "';\r\n" +
                "  }\r\n" +
                "\r\n" +
                "  bindFormValidators() {\r\n" +
                "    this.formModel = this.formBuilder.group({\r\n" +
                "      // ------------------- PANEL GERAL -------------------- //\r\n" +
                bindFormValidators + "\r\n" +
                "    });\r\n" +
                "  }\r\n" +
                "}";

        Utils.writeContentTo(path + "criar-editar-" + options.frontBaseFolder + ".component.ts", classBody);
        System.out.println("Generated CREATE/EDIT COMPONENT TS '" + "criar-editar-" + options.frontBaseFolder + ".component.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    // ------------------------------- FAZENDO CRIAR --------------------------------
    protected void gerarComponentCriar(GenOptions options) throws IOException {
        String serviceName = options.entityName + "Service";
        serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1);

        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\criar-" + options.frontBaseFolder + "\\";

        String classBody = "" +
                "import { Component, OnInit } from '@angular/core';" + "\r\n" +
                "import { FormGroup } from '@angular/forms';" + "\r\n" +
                "\r\n" +
                "import { CriarEditar" + options.frontBaseName + "Component } from '../criar-editar-" + options.frontBaseFolder + ".component';" + "\r\n" +
                "\r\n" +
                "import { CadastroBaseService } from '../../../../../cadastros/cadastro-base.service';" + "\r\n" +
                "import { " + options.entityName + "Service } from '../../../../../services/" + options.defaultRoute + ".service';" + "\r\n" +
                "import { " + options.entityName + " } from '../../../../../model/" + options.entityName + "';" + "\r\n" +
                "import { Observable } from 'rxjs';" + "\r\n" +
                "\r\n" +
                "@Component({" + "\r\n" +
                "  templateUrl: '../criar-editar-" + options.frontBaseFolder + ".component.html'," + "\r\n" +
                "  styleUrls: ['../criar-editar-" + options.frontBaseFolder + ".component.scss']" + "\r\n" +
                "})\r\n" +
                "export class Criar" + options.frontBaseName + "Component extends CriarEditar" + options.frontBaseName + "Component implements OnInit {" + "\r\n" +
                "  constructor(" + "\r\n" +
                "    public " + serviceName + ": " + options.entityName + "Service," + "\r\n" +
                "    public baseServices: CadastroBaseService" + "\r\n" +
                "  ) {" + "\r\n" +
                "    super(" + serviceName + ", baseServices);" + "\r\n" +
                "  }" + "\r\n" +
                "\r\n" +
                "  gravarModel(formModel: FormGroup): Observable<any> {" + "\r\n" +
                "    return this." + serviceName + "\r\n" +
                "        .observePersist( new " + options.entityName + "(formModel.getRawValue()) )" + "\r\n" +
                "        .map( () => {" + "\r\n" +
                "            this.toasty.success('" + options.entityName + " adicionado com sucesso!');" + "\r\n" +
                "            this.router.navigate([this.routerLink]);" + "\r\n" +
                "        });" + "\r\n" +
                "  }" + "\r\n" +
                "}" + "\r\n" +
                "";

        Utils.writeContentTo(path + "criar-" + options.frontBaseFolder + ".component.ts", classBody);
        System.out.println("Generated CREATE COMPONENT TS 'criar-" + options.frontBaseFolder + ".component.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    protected void gerarComponentEditar(GenOptions options) throws IOException {
        String serviceName = options.entityName + "Service";
        serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1);

        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\editar-" + options.frontBaseFolder + "\\";

        ModelGenerator modelGen = options.getModelGenerator();
        String setFormValues = "";

        // Listando as propriedades do modelGen
        for (Property prop : modelGen.getProperties()) {
            if (prop.getType() == PropertyType.JOIN) { // joins
                Reference ref = modelGen.getJoinReferenceFromProperty(prop);

                String propName = ref.getFormatVariableName(prop.getName());
                setFormValues += "    this.formModel.get('" + propName + ".id').setValue(model." + propName + ".id);" + "\r\n";

            } else if (prop.getType() == PropertyType.JOIN_COMPOSTO) { // joins compostos and imports
                // NAO HÁ MAIS JOIN COMPOSTOS !!
            } else if (prop.getType() == PropertyType.JOIN_COMPOSTO_CHAVE) { // joins compostos de chave
                // JOINS COMPOSTO DE CHAVE não são representados como variáveis ou chave, somente sendo representdo no próprio BANCO !
            } else if (prop.isPrimaryKey()) { // pks
                setFormValues += "    this.formModel.get('id').setValue( this.zerosLeft(model.id.toString(), 2) );" + "\r\n";
            } else { // other properties
                if(prop.getType().equals(PropertyType.AUDITORIA)) continue;

                setFormValues += "    this.formModel.get('" + prop.getVariableName() + "').setValue(" + normalizaSetFormValue(prop.getVariableName(), prop.getType()) + ");" + "\r\n";
            }
        }

        String classBody = "" +
                "import { Component, OnInit } from '@angular/core';" + "\r\n" +
                "import { ActivatedRoute } from '@angular/router';" + "\r\n" +
                "import { FormGroup } from '@angular/forms';" + "\r\n" +
                "\r\n" +
                "import { CriarEditar" + options.frontBaseName + "Component } from '../criar-editar-" + options.frontBaseFolder + ".component';" + "\r\n" +
                "\r\n" +
                "import { CadastroBaseService } from '../../../../../cadastros/cadastro-base.service';" + "\r\n" +
                "import { " + options.entityName + "Service } from '../../../../../services/" + options.defaultRoute + ".service';" + "\r\n" +
                "import { " + options.entityName + " } from '../../../../../model/" + options.entityName + "';" + "\r\n" +
                "\r\n" +
                "import { Observable } from 'rxjs';" + "\r\n" +
                "\r\n" +
                "@Component({" + "\r\n" +
                "  templateUrl: '../criar-editar-" + options.frontBaseFolder + ".component.html'," + "\r\n" +
                "  styleUrls: ['../criar-editar-" + options.frontBaseFolder + ".component.scss']" + "\r\n" +
                "})\r\n" +
                "export class Editar" + options.frontBaseName + "Component extends CriarEditar" + options.frontBaseName + "Component implements OnInit {\r\n" +
                "\r\n" +
                "  constructor(\r\n" +
                "    public " + serviceName + ": " + options.entityName + "Service,\r\n" +
                "\r\n" +
                "    public activatedRoute: ActivatedRoute,\r\n" +
                "    public baseServices: CadastroBaseService\r\n" +
                "  ) {\r\n" +
                "    super(" + serviceName + ", baseServices);\r\n" +
                "  }\r\n" +
                "\r\n" +
                "  ngOnInit() {\r\n" +
                "    this.editarView = true;\r\n" +
                "    this.activatedRoute.params\r\n" +
                "        .subscribe(params => {\r\n" +
                "          this.id" + options.entityName + " = params.id" + options.entityName + ";\r\n" +
                "        });\r\n" +
                "\r\n" +
                "    super.ngOnInit();\r\n" +
                "    this.title = 'Alterar Novo " + options.entityName + "';\r\n" +
                "\r\n" +
                "    // Preencher valores com o que foi buscado\r\n" +
                "    this." + serviceName + "\r\n" +
                "        .getById( this.id" + options.entityName + " )\r\n" +
                "        .then( model => {\r\n" +
                "          this.setFormValues( new " + options.entityName + "(model) );\r\n" +
                "        }).catch( (err) => this.errorHandler.handle(err) );\r\n" +
                "  }\r\n" +
                "\r\n" +
                "  setFormValues(model: " + options.entityName + ") {\r\n" +
                "    // ------------------- PANEL GERAL -------------------- //\r\n" +
                setFormValues +
                "  }\r\n" +
                "\r\n" +
                "  gravarModel(formModel: FormGroup): Observable<any> {\r\n" +
                "    return this." + serviceName + "\r\n" +
                "        .observeUpdate( new " + options.entityName + "(formModel.getRawValue()), this.id" + options.entityName + " )\r\n" +
                "        .map( () => {\r\n" +
                "          this.toasty.success('" + options.entityName + " Atualizado com Sucesso!');\r\n" +
                "          this.router.navigate([this.routerLink]);\r\n" +
                "        });\r\n" +
                "  }\r\n" +
                "}\r\n" +
                "";

        Utils.writeContentTo(path + "editar-" + options.frontBaseFolder + ".component.ts", classBody);
        System.out.println("Generated EDIT   COMPONENT TS '" + "editar-" + options.frontBaseFolder + ".component.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    // ------------------------------ NORMALIZAR TIPOS ------------------------------
    private String normalizaTipoModel(PropertyType type) {
        switch (type) {
            case STRING:
            case CHAR:
            case TEXT:
                return "string";

            case DATE:
            case TIMESTAMP:
                return "Date";

            case ACS_DATE_TIME:
                return "AcsDateTime";

            default:
            case NUMERO:
            case SHORT:
            case LONG:
            case DECIMAL:
                return "number";
        }
    }

    private String normalizaInicioModel(PropertyType type) {
        switch (type) {
            case STRING:
            case CHAR:
            case TEXT:
                return "''";

            case DATE:
            case TIMESTAMP:
            case ACS_DATE_TIME:
                return "null";

            default:
            case NUMERO:
            case SHORT:
            case LONG:
            case DECIMAL:
                return "0";
        }
    }

    private String normalizaSetFormValue(String nameVariable, PropertyType type) {
        switch (type) {
            case TIMESTAMP:
            case DATE:
                return "(model." + nameVariable + " != null)? new Date(model." + nameVariable + ") : null";

            case ACS_DATE_TIME:
                return "model." + nameVariable + ".toDate()";

            default:
            case STRING:
            case CHAR:
            case TEXT:
            case NUMERO:
            case SHORT:
            case LONG:
            case DECIMAL:
                return "model." + nameVariable;
        }
    }
}
