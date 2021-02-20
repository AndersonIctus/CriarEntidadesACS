package main.geradores.formaPgto.impl;

import main.geradores.GenOptions;
import main.geradores.Utils;
import main.geradores.model.impl.GerarFrontEnd;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class GerarFrontEndPagamento extends GerarFrontEnd {
    @Override
    public void gerarArquivos(GenOptions options) throws IOException {
        System.out.println("===============================================");
        System.out.println("=========== GERANDO FRONT END =================");
        if (options.mainFront != null) {
            mainPath = options.mainFront;
        }
        else {
            options.mainFront = mainPath;
        }

        if (options.onlyBackEnd) {
            System.out.println("Pulando a geração dos arquivos para o FrontEnd ...");
        }
        else {
            // Verifica se deve gerar todos arquivos mais do front end ou não
            if (options.frontModuleName.equalsIgnoreCase("cadastros")) {
                GerarFrontEnd.mainPath += "cadastros\\" + options.frontBaseFolder + "\\";
            }
            else {
                GerarFrontEnd.mainPath += "modulos\\" + options.frontModuleName + "\\" + options.frontBaseFolder + "\\";
            }

            // Criar pasta do novo módulo
            Utils.createDirectory(GerarFrontEnd.mainPath);
            if (!options.frontModuleName.contains("\\")) { // So gera se não tiver sub-modulos
                gerarModule(options);
                gerarRoutingModule(options);
            }
            else {
                System.out.println("Pulando geracao do Module e RoutingModule, pois trata-se de um sub-modulo");
                System.out.println("------------------------------------------------------------------------------\r\n");
            }

            // ----------- Listagem
            Utils.createDirectory(GerarFrontEnd.mainPath + "listar-" + options.frontBaseFolder + "\\");
            gerarTelaListar(options);
            gerarSassListar(options);
            gerarComponentListar(options);

            // ----------- Criar / Editar
            Utils.createDirectory(GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\");
            gerarTelaCriarEditar(options);
            gerarSassCriarEditar(options);
            gerarComponentCriarEditar(options);

            // Criar
            Utils.createDirectory(GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\criar-" + options.frontBaseFolder);
            gerarComponentCriar(options);

            // Editar
            Utils.createDirectory(GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\editar-" + options.frontBaseFolder);
            gerarComponentEditar(options);

            // Incluir o novo modulo no Routing Module Escolhido
            if (!options.frontModuleName.contains("\\")) { // So gera se não tiver sub-modulos
                incluirCadastroModulo(options);
                incluirPrestacaoModulo(options);
                incluirGerenciaModulo(options);
            } else {
                System.out.println("Pulando inclusão nas rotas PADRAO, pois trata-se de um sub-modulo");
                System.out.println("------------------------------------------------------------------------------\r\n");
            }
        }

        System.out.println();
        System.out.println("===============================================");
        System.out.println("===============================================");
    }

    // region // ------------------- GERACAO DOS MODULOS ------------------- //
    // ----------------------------------------------------------------------------------------- //
    @Override
    protected void gerarModule(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath;

        String classBody =
                "import { NgModule } from '@angular/core';\r\n" +
                "import { CommonModule } from '@angular/common';\r\n" +
                "import { UtilsModule } from '../../../shared/utils/utils.module';\r\n" +
                "import { MaterialModule, MaterialDateModule } from '../../../shared/utils/material-module.module';\r\n" +
                "\r\n" +
                "import { SharedComponentsModule } from '../../../shared/components/shared-components.module';\r\n" +
                "import { SharedDirectivesModule } from '../../../shared/directive/shared-directives.module';\r\n" +
                "import { SharedPipesModule } from '../../../shared/pipes/shared-pipes.module';\r\n" +
                "\r\n" +
                "import { " + options.frontBaseName + "RoutingModule } from './" + options.frontBaseFolder + "-routing.module';\r\n" +
                "\r\n" +
                "import { AcsRecebimentoModule } from '../shared/acs-recebimento/acs-recebimento.module';\r\n" +
                "\r\n" +
                "import { Listar" + options.frontBaseName + "Component } from './listar-" + options.frontBaseFolder + "/listar-" + options.frontBaseFolder + ".component';\r\n" +
                "import { Criar" + options.frontBaseName + "Component } from './criar-editar-" + options.frontBaseFolder + "/criar-" + options.frontBaseFolder + "/criar-" + options.frontBaseFolder + ".component';\r\n" +
                "import { Editar" + options.frontBaseName + "Component } from './criar-editar-" + options.frontBaseFolder + "/editar-" + options.frontBaseFolder + "/editar-" + options.frontBaseFolder + ".component';\r\n" +
                "\r\n" +
                "@NgModule({\r\n" +
                "  imports: [" + "\r\n" +
                "    CommonModule,\r\n" +
                "    MaterialModule,\r\n" +
                "    MaterialDateModule,\r\n" +
                "    UtilsModule,\r\n" +
                "\r\n" +
                "    SharedComponentsModule,\r\n" +
                "    SharedDirectivesModule,\r\n" +
                "    SharedPipesModule,\r\n" +
                "\r\n" +
                "    " + options.frontBaseName + "RoutingModule,\r\n" +
                "    AcsRecebimentoModule\r\n" +
                "  ],\r\n" +
                "  declarations: [\r\n" +
                "        Listar" + options.frontBaseName + "Component, " + "Criar" + options.frontBaseName + "Component, " + "Editar" + options.frontBaseName + "Component\r\n" +
                "  ]\r\n" +
                "})\r\n" +
                "export class " + options.frontBaseName + "Module { }";

        Utils.writeContentTo(path + options.frontBaseFolder + ".module.ts", classBody);
        System.out.println("Generated Module '" + options.frontBaseFolder + ".module.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    // ----------------------------------------------------------------------------------------- //
    @Override
    protected void gerarRoutingModule(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath;

        String classBody =
            "import { NgModule } from '@angular/core';\r\n" +
            "import { Routes, RouterModule } from '@angular/router';\r\n" +
            "import { ParametrosSistemaResolver } from '../../../shared/resolvers/parametros-sistema-resolver';\r\n" +
            "import { AuthGuard } from '../../../general/general-interceptors/guard/auth-guard';\r\n" +
            "\r\n" +
            "// Componentes para Prestação \r\n" +
            "import { Listar" + options.frontBaseName + "Component } from './listar-" + options.frontBaseFolder + "/listar-" + options.frontBaseFolder + ".component';\r\n" +
            "import { Criar" + options.frontBaseName + "Component } from './criar-editar-" + options.frontBaseFolder + "/criar-" + options.frontBaseFolder + "/criar-" + options.frontBaseFolder + ".component';\r\n" +
            "import { Editar" + options.frontBaseName + "Component } from './criar-editar-" + options.frontBaseFolder + "/editar-" + options.frontBaseFolder + "/editar-" + options.frontBaseFolder + ".component';\r\n" +
            "\r\n" +
            "const preferenceFields = 'clientePadrao.id,permTrocoVc,reqAutorizacaoCt,reqNsuCt,opcaoValorVendaTrocoVc';\r\n" +
            "\r\n" +
            "// rota padrao = /" + options.frontModuleName + "/" + options.frontBaseFolder + "\r\n" +
            "const routes: Routes = [\r\n" +
            "  { path: '', redirectTo: '/" + options.frontModuleName + "/" + options.frontBaseFolder + "/venda', pathMatch: 'full' },\r\n" +
            getRoutingPath(options, "venda") + ",\r\n"+
            getRoutingPath(options, "gerencia") + "\r\n"+
            "];\r\n" +
            "@NgModule({\r\n" +
            "  imports: [RouterModule.forChild(routes)],\r\n" +
            "  exports: [ ]\r\n" +
            "})\r\n" +
            "export class " + options.frontBaseName + "RoutingModule { }\r\n";

        Utils.writeContentTo(path + options.frontBaseFolder + "-routing.module.ts", classBody);
        System.out.println("Generated Routing Module '" + options.frontBaseFolder + "-routing.module.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    private String getRoutingPath(GenOptions options, String pathBase) {
        return  "  " + "{\r\n" +
                "      " + "path: '" + pathBase + "',\r\n" +
                "      " + "children: [\r\n" +
                "          " + "{ path: '', component: Listar" + options.frontBaseName + "Component, canActivate: [AuthGuard],\r\n" +
                "          " + "    data: { roles: ['SUPER', 'ACESSAR " + options.accessAlias + "'] }},\r\n" +
                "          " + "{ path: 'create', component: Criar" + options.frontBaseName + "Component, canActivate: [AuthGuard],\r\n" +
                "          " + "    data: { roles: ['SUPER', 'INCLUIR " + options.accessAlias + "'], prefFields: preferenceFields }, resolve: { prefSistema: ParametrosSistemaResolver }},\r\n" +
                "          " + "{ path: ':idRecebimento', component: Editar" + options.frontBaseName + "Component, canActivate: [AuthGuard],\r\n" +
                "          " + "    data: { prefFields: preferenceFields }, resolve: { prefSistema: ParametrosSistemaResolver }}\r\n" +
                "      " + "]\r\n" +
                "  " + "}";
    }
    //endregion

    // region // ------------------ GERACAO DAS LISTAGENS ------------------ //
    @Override
    protected void gerarTelaListar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "listar-" + options.frontBaseFolder + "\\";
        String serviceName = "recebimentoService";

        String classBody = "" +
                "<form [formGroup]=\"formModel\" data-toggle=\"validator\" role=\"form\">\r\n" +
                "    <others_panel>" + "\r\n" +
                "        <div class=\"panel-header m-0 row\">" + "\r\n" +
                "            <h2 class=\"mr-auto mb-0\">{{ title }}</h2>" + "\r\n" +
                "\r\n" +
                "            <div *ngIf=\"isListagemOrigemCaixa()\" class=\"mr-2\">" + "\r\n" +
                "                <button type=\"button\" mat-raised-button type=\"button\" class=\"btn btn--rounded text-bold pl-2\" (click)=\"voltarParaPrestacoesGerencia()\">" + "\r\n" +
                "                    <mat-icon>account_balance</mat-icon> {{ listagemCaixaText }}" + "\r\n" +
                "                </button>" + "\r\n" +
                "            </div>" + "\r\n" +
                "\r\n" +
                "            <crud_create [enabled]=\"false\" [nomessage]=\"true\" (click)=\"tratamentoDeInclusao()\"></crud_create>" + "\r\n" +
                "        </div>" + "\r\n" +
                "\r\n" +
                "        <div class=\"panel-content mat-elevation-z8 pb-1\">" + "\r\n" +
                "            <table_filter-custom #customFilter class=\"filter filter--edges\">" + "\r\n" +
                "                <!-- CABEÇALHO DA LISTAGEM -->" + "\r\n" +
                "                <acs-listagem-recebimento #acs_listagem_recebimento" + "\r\n" +
                "                       [filterDialogComponent]=\"filter_dialog\"" + "\r\n" +
                "                       [submitted]=\"submitted\"" + "\r\n" +
                "                       (mudancaData)=\"mudancaData()\">" + "\r\n" +
                "\r\n" +
                "                    <!-- FINALIZADORA -->" + "\r\n" +
                "                    <div class=\"form-group col-4 col-sm-6 col-md-3 col-lg-2\">" + "\r\n" +
                "                        <mat-form-field appearance=\"fill\">" + "\r\n" +
                "                            <mat-label><b>Tipo do Cliente</b></mat-label>" + "\r\n" +
                "                            <mat-select formControlName=\"finalizadora\" title=\"Escolher Tipo do Cliente\">" + "\r\n" +
                "                                <mat-option *ngFor=\"let opcao of opcoesFinalizadora\" [value]=\"opcao.value\"> {{opcao.name}} </mat-option>" + "\r\n" +
                "                            </mat-select>" + "\r\n" +
                "                        </mat-form-field>" + "\r\n" +
                "                    </div>" + "\r\n" +
                "\r\n" +
                "                    <!-- VALOR MINIMO -->" + "\r\n" +
                "                    <div class=\"form-group col-4 col-sm-6 col-md-3 col-lg-2\">" + "\r\n" +
                "                        <mat-form-field appearance=\"fill\" hideRequiredMarker=\"true\" floatLabel=\"always\">" + "\r\n" +
                "                            <mat-label><b>Valor Mínimo (R$)</b></mat-label>" + "\r\n" +
                "                            <input matInput formControlName=\"valorMinimo\" maxLength=\"14\" acsCurrencyMask" + "\r\n" +
                "                                [options]=\"{ prefix: ' ', precision: {min: 2, max: 2} }\" placeholder=\"0,00\"" + "\r\n" +
                "                                (keyup.enter)=\"filter.doFilter()\">" + "\r\n" +
                "                        </mat-form-field>" + "\r\n" +
                "                        <others_alert-errors [submitted]=\"submitted\" [control]=\"formModel.get('valorMinimo')\"" + "\r\n" +
                "                            [errors]=\"[errorValIniMaiorValFin]\"" + "\r\n" +
                "                        ></others_alert-errors>" + "\r\n" +
                "                    </div>" + "\r\n" +
                "\r\n" +
                "                    <!-- VALOR MAXIMO -->" + "\r\n" +
                "                    <div class=\"form-group col-4 col-sm-6 col-md-3 col-lg-2\">" + "\r\n" +
                "                        <mat-form-field appearance=\"fill\" hideRequiredMarker=\"true\" floatLabel=\"always\">" + "\r\n" +
                "                            <mat-label><b>Valor Máximo (R$)</b></mat-label>" + "\r\n" +
                "                            <input matInput formControlName=\"valorMaximo\" maxLength=\"14\" acsCurrencyMask" + "\r\n" +
                "                                [options]=\"{ prefix: ' ', precision: {min: 2, max: 2} }\" placeholder=\"0,00\"" + "\r\n" +
                "                                (keyup.enter)=\"filter.doFilter()\">" + "\r\n" +
                "                        </mat-form-field>" + "\r\n" +
                "                        <others_alert-errors [submitted]=\"submitted\" [control]=\"formModel.get('valorMaximo')\"" + "\r\n" +
                "                            [errors]=\"[errorValIniMaiorValFin]\"" + "\r\n" +
                "                        ></others_alert-errors>" + "\r\n" +
                "                    </div>" + "\r\n" +
                "                </acs-listagem-recebimento>" + "\r\n" +
                "            </table_filter-custom>" + "\r\n" +
                "\r\n" +
                "            <div class=\"row m-0 p-0 text-center\">" + "\r\n" +
                "                <label class=\"col-12 text-bold text-color-primary text-size-16\">" + "\r\n" +
                "                    {{ qtdFinalizadoras }} Finalizadoras Totalizadas, Valor Total:" + "\r\n" +
                "                    {{valorTotalListado | decimal: { prefix: 'R$ '} }}" + "\r\n" +
                "                </label>" + "\r\n" +
                "            </div>" + "\r\n" +
                "\r\n" +
                "            <table_data-simple #table class=\"listar-" + options.frontBaseFolder + "\" [data_service]=\"" + serviceName + "\" [customFilterComponent]=\"customFilter\" sortActive=\"dataEmissao\" sortDirection=\"desc\">" + "\r\n" +
                "                <td acsTableColumn=\"dataEmissao\" *tableColumn=\"'dataEmissao'; header:'Emissão'; sort_header: true; let row\">{{ row.dataEmissao | acsDate }}</td>" + "\r\n" +
                "                <td acsTableColumn=\"valor\" *tableColumn=\"'valor'; header:'Valor (R$)'; let row\">{{ row.valor | decimal }}</td>" + "\r\n" +
                "\r\n" +
                "                <!-- EDIT AND DELETE ROW -->" + "\r\n" +
                "                <td acsTableColumn=\"edit_delete\" [sticky]=\"true\" tabindex=\"-1\" *tableColumn=\"'edit_delete'; let row\">" + "\r\n" +
                "                    <crud_edit [entity_id]=\"row.id\"></crud_edit>" + "\r\n" +
                "                    <crud_delete (click)=\"modelAtual = row\" [enabled]=\"auth.hasUpdatePermission()\"></crud_delete>" + "\r\n" +
                "                </td>" + "\r\n" +
                "            </table_data-simple>" + "\r\n" +
                "        </div>" + "\r\n" +
                "    </others_panel>" + "\r\n" +
                "</form>\r\n" +
                "\r\n" +
                "<!-- Modal -->" + "\r\n" +
                "<dialogs_delete bodyText=\"Deseja excluir o MODELO '{{ modelAtual?.id }}'?\" (delete_button_click)=\"deleteModel()\"></dialogs_delete>" + "\r\n" +
                "\r\n" +
                "<!-- MODAL FILTER -->" + "\r\n" +
                "<dialogs_filter class=\"filter-listagem-recebimento filter-" + options.frontBaseFolder + "\" #filter_dialog></dialogs_filter>" + "\r\n" +
                "\r\n";

        Utils.writeContentTo(path + "listar-" + options.frontBaseFolder + ".component.html", classBody);
        System.out.println("Generated LIST COMPONENT HTML '" + "listar-" + options.frontBaseFolder + ".component.html' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    @Override
    protected void gerarSassListar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "listar-" + options.frontBaseFolder + "\\";

        String classBody = "" +
                ":host ::ng-deep {" + "\r\n" +
                "    " + ".filter-" +options.frontBaseFolder + " {" + "\r\n" +
                "    " + "}" + "\r\n" +
                "\r\n" +
                "    " + ".listar-" +options.frontBaseFolder + " {" + "\r\n" +
                "    " + "}" + "\r\n" +
                "}\r\n";

        Utils.writeContentTo(path + "listar-" + options.frontBaseFolder + ".component.scss", classBody);
        System.out.println("Generated LIST COMPONENT SASS '" + "listar-" + options.frontBaseFolder + ".component.scss' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    @Override
    protected void gerarComponentListar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "listar-" + options.frontBaseFolder + "\\";

        String serviceName = options.entityName + "Service";
        serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1);

        String classBody =
            "import { OnInit, ViewChild, AfterContentInit, Component } from '@angular/core';" + "\r\n" +
            "import { Validators } from '@angular/forms';" + "\r\n" +
            "import { ActivatedRoute } from '@angular/router';" + "\r\n" +
            "\r\n" +
            "import { Util } from '../../../../shared/utils/Util';" + "\r\n" +
            "import { AcsDateTime } from '../../../../shared/utils/AcsDateTime';" + "\r\n" +
            "import { ConstTipoFinalizadora } from '../../../../shared/models/constants/ConstTipoFinalizadora';" + "\r\n" +
            "\r\n" +
            "import { " + options.entityName + " } from '../../../../model/" + options.entityName + "';" + "\r\n" +
            "import { " + options.entityName + "Service } from '../../../../services/" + options.defaultRoute + ".service';" + "\r\n" +
            "\r\n" +
            "import { ListagemBaseComponent } from '../../../../shared/base/listagem-base-component';" + "\r\n" +
            "import { ListagemBaseService } from '../../../../shared/base/listagem-base.service';" + "\r\n" +
            "import { ListagemRecebimentoComponent } from '../../shared/acs-recebimento/listagem-recebimento/listagem-recebimento.component';" + "\r\n" +
            "\r\n" +
            "import { ListagemContexto } from '../../shared/acs-recebimento/contexto-recebimento/listar/ListagemContexto';" + "\r\n" +
            "import { ContextoRecebimentoFactory } from '../../shared/acs-recebimento/contexto-recebimento/ContextoRecebimentoFactory';" + "\r\n" +
            "\r\n" +
            "import { debounceTime } from 'rxjs/operators';" + "\r\n" +
            "\r\n" +
            "@Component({" + "\r\n" +
            "    templateUrl: './listar-" + options.frontBaseFolder + ".component.html'," + "\r\n" +
            "    styleUrls: ['./listar-" + options.frontBaseFolder + ".component.scss']" + "\r\n" +
            "})" + "\r\n" +
            "export class Listar" + options.frontBaseName + "Component extends ListagemBaseComponent implements OnInit, AfterContentInit {" + "\r\n" +
            "    modelAtual: " + options.entityName + ";" + "\r\n" +
            "    contexto: ListagemContexto;" + "\r\n" +
            "\r\n" +
            "    qtdFinalizadoras: number = 0;" + "\r\n" +
            "    valorTotalListado: number = 0;" + "\r\n" +
            "    listagemCaixaText: string;" + "\r\n" +
            "\r\n" +
            "    opcoesFinalizadora = [" + "\r\n" +
            "        { name: 'Todos', value: 'T' }," + "\r\n" +
            "        { name: 'Finalizadora', value: ConstTipoFinalizadora.finalizadora }" + "\r\n" +
            "    ];" + "\r\n" +
            "\r\n" +
            "    // ------ FILTERS ------ //" + "\r\n" +
            "    @ViewChild('acs_listagem_recebimento', {static: true}) acsListagemRecebimento: ListagemRecebimentoComponent;" + "\r\n" +
            "\r\n" +
            "    constructor(\r\n" +
            "      public " + serviceName + ": " + options.entityName + "Service,\r\n" +
            "\r\n" +
            "      public activatedRoute: ActivatedRoute," + "\r\n" +
            "      public baseServices: ListagemBaseService" + "\r\n" +
            "    ) { super(baseServices); }\r\n" +
            "\r\n" +
            "    ngOnInit(): void {" + "\r\n" +
            "       // Resolvendo o Contexto !" + "\r\n" +
            "       this.contexto = ContextoRecebimentoFactory.createListagemContexto(this.router, this.bag, this.activatedRoute.snapshot.params);" + "\r\n" +
            "       this.contexto.setTitle('" + options.frontBaseName + "');" + "\r\n" +
            "\r\n" +
            "       super.ngOnInit();" + "\r\n" +
            "       this.title = this.contexto.getTitle();" + "\r\n" +
            "       this.listagemCaixaText = this.contexto.getCaixaText();" + "\r\n" +
            "\r\n" +
            "       this.auth.cadastroPermission = '" + options.accessAlias + "';" + "\r\n" +
            "\r\n" +
            "       // Definições dos filtros" + "\r\n" +
            "       this.iniciarFiltros();" + "\r\n" +
            "       this.acsListagemRecebimento.setValidacaoCallDialog( this.validarUsoFiltro.bind(this) );" + "\r\n" +
            "\r\n" +
            "       // Verificando o contexto a de algum partir da CAIXA" + "\r\n" +
            "       if(this.contexto.isOrigemCaixa()) {" + "\r\n" +
            "           super.setLimpaDadosNoCarregamento(false);" + "\r\n" +
            "           this.acsListagemRecebimento.setValuesPrestacaoGerencia(this.contexto.getValuePrestacaoGerencia());" + "\r\n" +
            "\r\n" +
            "       } else {" + "\r\n" +
            "           this.contexto.navegarAPartirDe('" + options.frontBaseFolder + "'); // Se cair aqui e a URL for a mesma, a navegação não ocorre !" + "\r\n" +
            "       }" + "\r\n" +
            "\r\n" +
            "       this.table.urlAction = '/list-" + options.frontBaseFolder + "';" + "\r\n" +
            "       this.table.dataParameter = this.getFilterList();" + "\r\n" +
            "    }\r\n" +
            "\r\n" +
            "    ngAfterContentInit(): void {" + "\r\n" +
            "        this.totalizadorListagem();" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    bindFormValidators(): void {\r\n" +
            "        const dataAtual = AcsDateTime.now().toDate();" + "\r\n" +
            "        this.formModel = this.formBuilder.group({" + "\r\n" +
            "            dtInicial: [dataAtual, Validators.required]," + "\r\n" +
            "            dtFinal: [dataAtual, Validators.required]," + "\r\n" +
            "            turno: ['']," + "\r\n" +
            "            finalizadora: ['T', Validators.required], // Tipo do Cliente" + "\r\n" +
            "            valorMinimo: ['']," + "\r\n" +
            "            valorMaximo: ['']," + "\r\n" +
            "\r\n" +
            "            pdv: [[]]," + "\r\n" +
            "            caixaVenda: [[]]," + "\r\n" +
            "            operador: [[]]" + "\r\n" +
            "        });" + "\r\n" +
            "\r\n" +
            "        // Definindo o fomModel da Listagem de Recebimento" + "\r\n" +
            "        this.acsListagemRecebimento.formModel = this.formModel;" + "\r\n" +
            "\r\n" +
            "        // VALUE CHANGES //" + "\r\n" +
            "        this.formModel.get('valorMinimo').valueChanges.pipe(debounceTime(300)).subscribe( value => {" + "\r\n" +
            "            this.validarValorIniFin();" + "\r\n" +
            "        });" + "\r\n" +
            "\r\n" +
            "        this.formModel.get('valorMaximo').valueChanges.pipe(debounceTime(300)).subscribe( value => {" + "\r\n" +
            "            this.validarValorIniFin();" + "\r\n" +
            "        });" + "\r\n" +
            "    }\r\n" +
            "\r\n" +
            "    iniciarFiltros(): void {" + "\r\n" +
            "        ////////////////////////////////////////////////////////" + "\r\n" +
            "        // CABEÇALHO DO ACS LISTAGEM" + "\r\n" +
            "        this.acsListagemRecebimento.empresa = this.empresa;" + "\r\n" +
            "        this.acsListagemRecebimento.contexto = this.contexto.getContextoCaixaDeOrigem();" + "\r\n" +
            "        this.acsListagemRecebimento.finalizadoras = [ConstTipoFinalizadora.finalizadora];" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    getParametros(): string {\r\n" +
            "        const filter = this.getFilterList();" + "\r\n" +
            "        this.totalizadorListagem();" + "\r\n" +
            "\r\n" +
            "        return filter;" + "\r\n" +
            "    }\r\n" +
            "\r\n" +
            "    getFilterList(pegaCobranca = true): string {" + "\r\n" +
            "        const filterList: string[] = [];" + "\r\n" +
            "        const filtro = this.formModel.getRawValue();" + "\r\n" +
            "\r\n" +
            "        filterList.push(`idEmpresa=${this.empresa.id}`);" + "\r\n" +
            "        if(filtro['dtInicial'] !== null) {" + "\r\n" +
            "            filterList.push(`dtInicial=${new AcsDateTime(filtro['dtInicial']).toMinAcsDateTime()}`);" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        if(filtro['dtFinal'] !== null) {" + "\r\n" +
            "            filterList.push(`dtFinal=${new AcsDateTime(filtro['dtFinal']).toMaxAcsDateTime()}`);" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        if(filtro['turno'] !== '') {" + "\r\n" +
            "            filterList.push(`turno=${filtro['turno']}`);" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        if(filtro['valorMinimo'] !== 0 && filtro['valorMinimo'] !== '') {" + "\r\n" +
            "            filterList.push(`valorMinimo=${filtro['valorMinimo']}`);" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        if(filtro['valorMaximo'] !== 0 && filtro['valorMaximo'] !== '') {" + "\r\n" +
            "            filterList.push(`valorMaximo=${filtro['valorMaximo']}`);" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        filtro['pdv'].forEach( element => {" + "\r\n" +
            "            filterList.push(`idPdv=${element.id}`);" + "\r\n" +
            "        });" + "\r\n" +
            "\r\n" +
            "        filtro['caixaVenda'].forEach( element => {" + "\r\n" +
            "            filterList.push(`idCaixaVenda=${element.id}`);" + "\r\n" +
            "        });" + "\r\n" +
            "\r\n" +
            "        filtro['operador'].forEach( element => {" + "\r\n" +
            "            filterList.push(`idOperador=${element.id}`);" + "\r\n" +
            "        });" + "\r\n" +
            "\r\n" +
            "       // ------------------------------------------------" + "\r\n" +
            "       if(filtro['finalizadora'] === 'T') {" + "\r\n" +
            "           filterList.push(`finalizadora=${ConstTipoFinalizadora.finalizadoras}`);" + "\r\n" +
            "       } else {" + "\r\n" +
            "           filterList.push(`finalizadora=${filtro['finalizadora']}`);" + "\r\n" +
            "       }" + "\r\n" +
            "\r\n" +
            "       filterList.push(`contextoCaixaDeOrigem=${this.contexto.getContextoCaixaDeOrigem()}`);" + "\r\n" +
            "       return filterList.join('&');" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    // Override " + "\r\n" +
            "    limparCampos() {" + "\r\n" +
            "        if(this.contexto.isOrigemCaixa()) {" + "\r\n" +
            "            this.bindFormValidators();" + "\r\n" +
            "\r\n" +
            "            this.acsListagemRecebimento.setValuesPrestacaoGerencia(this.contexto.getValuePrestacaoGerencia());" + "\r\n" +
            "            super.filtrarEOrdenar();" + "\r\n" +
            "\r\n" +
            "            this.submitted = false;" + "\r\n" +
            "\r\n" +
            "        } else {" + "\r\n" +
            "            super.limparCampos();" + "\r\n" +
            "        }" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    // ---------------- FUNCOES DE DATA PARAMETER ---------------- \\" + "\r\n" +
            "    mudancaData() {" + "\r\n" +
            "        this.acsListagemRecebimento.limparFiltros();" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    totalizadorListagem() {" + "\r\n" +
            "        const filters = this.getFilterList();" + "\r\n" +
            "        this.recebimentoService" + "\r\n" +
            "            .getCustom(`/totalizadoresRecebimentos/?${filters}`)" + "\r\n" +
            "            .toPromise()" + "\r\n" +
            "            .then(resp => {" + "\r\n" +
            "                const response = resp.json();" + "\r\n" +
            "                const total: number = 0;" + "\r\n" +
            "                this.qtdFinalizadoras = response.length;" + "\r\n" +
            "                this.valorTotalListado = response.reduce((acumulador, atual) => acumulador + atual.valor, total);" + "\r\n" +
            "        });" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    // ------------------------- VALIDAÇÕES ------------------------- //" + "\r\n" +
            "    validarValorIniFin() {" + "\r\n" +
            "        const valorInicial = this.formModel.get('valorMinimo');" + "\r\n" +
            "        const valorFinal   = this.formModel.get('valorMaximo');" + "\r\n" +
            "\r\n" +
            "        super.validarValorInicialMaiorQueValorFinal(valorInicial, valorFinal);" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    // Esse metodo é reutilizado nos filters para só poderem ser usados se houver Data selecionada" + "\r\n" +
            "    validarUsoFiltro(): boolean {" + "\r\n" +
            "        const filtros = this.formModel.getRawValue();" + "\r\n" +
            "        if(filtros['dtInicial'] == null || filtros['dtFinal'] == null) {" + "\r\n" +
            "            this.toasty.warning('Preencha as Datas antes de fazer a busca.');" + "\r\n" +
            "            return false;" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        return true;" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    deleteModel() {" + "\r\n" +
            "        this.table.isLoadingResults = true;" + "\r\n" +
            "        this.recebimentoService" + "\r\n" +
            "            .customDelete(`/${this.modelAtual.id}/${this.contexto.getUrlContexto()}`)" + "\r\n" +
            "            .then(() => {" + "\r\n" +
            "                this.table.isLoadingResults = false;" + "\r\n" +
            "                this.table.filterDataTable((dataTable: Recebimento[]): Recebimento[] => {" + "\r\n" +
            "                    return dataTable.filter(ele => ele.id !== this.modelAtual.id);" + "\r\n" +
            "                 });" + "\r\n" +
            "\r\n" +
            "                this.toasty.success(`Recebimento excluída com sucesso!`);" + "\r\n" +
            "                this.totalizadorListagem();" + "\r\n" +
            "            }).catch(erro => {" + "\r\n" +
            "                this.table.isLoadingResults = false;" + "\r\n" +
            "                this.errorHandler.handle(erro);" + "\r\n" +
            "            });" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    // ------------------------- METODOS PARA A PRESTAÇÃO E GERÊNCIA ------------------------- //" + "\r\n" +
            "    // Metodo para voltar apra as prestações (Sem implementação, pois o botão só serve para o contexto de prestações)" + "\r\n" +
            "    voltarParaPrestacoesGerencia() {" + "\r\n" +
            "        this.contexto.voltarParaPrestacoesGerencia();" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    tratamentoDeInclusao() {" + "\r\n" +
            "        // 1 - Verifica se tem permissão para incluir !" + "\r\n" +
            "        if(!this.auth.hasIncludePermission()) {" + "\r\n" +
            "            this.toasty.error('O Usuário não tem permissão para Incluir!');" + "\r\n" +
            "            return;" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        // Verifica a inclusao no Contexto !" + "\r\n" +
            "        try {" + "\r\n" +
            "            this.contexto.validacaoDeInclusao();" + "\r\n" +
            "        } catch (error) {" + "\r\n" +
            "            this.toasty.error(error.message);" + "\r\n" +
            "            return;" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        this.router.navigate(['create'], {relativeTo: this.activatedRoute});" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    isListagemOrigemCaixa() {" + "\r\n" +
            "        return this.contexto.isOrigemCaixa();" + "\r\n" +
            "    }" + "\r\n" +
            "}" + "\r\n";

        Utils.writeContentTo(path + "listar-" + options.frontBaseFolder + ".component.ts", classBody);
        System.out.println("Generated LIST COMPONENT TS '" + "listar-" + options.frontBaseFolder + ".component.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }
    //endregion

    // region // ----------------- GERACAO DO CRIAR/EDITAR ----------------- //
    @Override
    protected void gerarTelaCriarEditar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\";

        String classBody = "" +
                "<form [formGroup]=\"formModel\" data-toggle=\"validator\" role=\"form\">" + "\r\n" +
                "    <others_group-panel [titleGroup]=\"title\"> <!-- GROUP PANEL -->" + "\r\n" +
                "        <others_panel [panelTitle]=\"title\"> <!-- PANEL GERAL -->" + "\r\n" +
                "            <div class=\"panel-content row m-0\">" + "\r\n" +
                "                <!-- CABEÇALHO DO CRIAR/EDITAR -->" + "\r\n" +
                "                <acs-criar-editar-recebimento #acs_criar_editar_recebimento" + "\r\n" +
                "                    [searchDialogComponent]=\"search_dialog\"" + "\r\n" +
                "                    [showDataVencimento]=\"editarView\"" + "\r\n" +
                "                    [submitted]=\"submitted\"" + "\r\n" +
                "                    (recebimentoEvent)=\"recebimentoEvent($event);\"" + "\r\n" +
                "                ></acs-criar-editar-recebimento>" + "\r\n" +
                "\r\n" +
                "                <!-- OUTROS CAMPOS -->" + "\r\n" +
                "            </div>" + "\r\n" +
                "          </others_panel>" + "\r\n" +
                "      </others_group-panel>" + "\r\n" +
                "</form>" + "\r\n" +
                "\r\n" +
                "<!-- TOOL BOX -->" + "\r\n" +
                "<others_tool-box #toolbox [formModel]=\"formModel\" [route]=\"routerLink\" [salvarEditarText]=\"salvarEditarText\" (submitAction)=\"gravarFormModel();\"></others_tool-box>" + "\r\n" +
                "\r\n" +
                "<!-- SEARCH DIALOGS -->" + "\r\n" +
                "<dialogs_search class=\"search-dialog-" + options.frontBaseFolder + "\" #search_dialog></dialogs_search>" + "\r\n" +
                "\r\n";

        Utils.writeContentTo(path + "criar-editar-" + options.frontBaseFolder + ".component.html", classBody);
        System.out.println("Generated CREATE/EDIT COMPONENT HTML '" + "criar-editar-" + options.frontBaseFolder + ".component.html' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    @Override
    protected void gerarSassCriarEditar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\";

        String classBody =
                ":host ::ng-deep {" + "\r\n" +
                "    .search-dialog-" + options.frontBaseFolder + " {" + "\r\n" +
                "        .mat-header-id {" + "\r\n" +
                "            min-width: 80px;" + "\r\n" +
                "        }" + "\r\n" +
                "\r\n" +
                "        .mat-column-id {" + "\r\n" +
                "            text-align: center;" + "\r\n" +
                "            width: 80px;" + "\r\n" +
                "        }" + "\r\n" +
                "    }" + "\r\n" +
                "}" + "\r\n";

        Utils.writeContentTo(path + "criar-editar-" + options.frontBaseFolder + ".component.scss", classBody);
        System.out.println("Generated CREATE/EDIT COMPONENT CSS '" + "criar-editar-" + options.frontBaseFolder + ".component.scss' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    @Override
    protected void gerarComponentCriarEditar(GenOptions options) throws IOException {
        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\";

        String serviceName = options.entityName + "Service";
        serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1);

        String classBody = "" +
            "import { Directive, OnInit, ViewChild } from '@angular/core';\r\n" +
            "import { Validators } from '@angular/forms';\r\n" +
            "import { ActivatedRoute } from '@angular/router';" + "\r\n" +
            "\r\n" +
            "import { Util } from '../../../../shared/utils/Util';" + "\r\n" +
            "import { AcsDateTime } from '../../../../shared/utils/AcsDateTime';" + "\r\n" +
            "import { DialogDesignModel } from '../../../../shared/components/dialogs/custom-dialog/DialogDesignModel';" + "\r\n" +
            "import { FormComponentValidator } from '../../../../shared/components/validator/form-components-validator';" + "\r\n" +
            "\r\n" +
            "import { CadastroBaseComponent } from '../../../../cadastros/cadastro-base.component';\r\n" +
            "import { CadastroBaseService } from '../../../../cadastros/cadastro-base.service';\r\n" +
            "import { SearchDialogComponent, SearchResponseModel } from '../../../../shared/components/dialogs/search-dialog/search-dialog.component';" + "\r\n" +
            "import { CriarEditarRecebimentoComponent, TipoRecebimentoEvent, RecebimentoEvent } from '../../shared/acs-recebimento/criar-editar-recebimento/criar-editar-recebimento.component';" + "\r\n" +
            "\r\n" +
            "import { CriarEditarContexto } from '../../shared/acs-recebimento/contexto-recebimento/criar-editar/CriarEditarContexto';" + "\r\n" +
            "import { ContextoRecebimentoFactory } from '../../shared/acs-recebimento/contexto-recebimento/ContextoRecebimentoFactory';" + "\r\n" +
            "import { ConstContextoCaixaDeOrigem } from '../../shared/base/ConstContextoCaixaDeOrigem';" + "\r\n" +
            "import { ConstTipoFinalizadora } from '../../../../shared/models/constants/ConstTipoFinalizadora';" + "\r\n" +
            "\r\n" +
            "import { " + options.entityName + "Service } from '../../../../services/" + options.defaultRoute + ".service';\r\n" +
            "import { ClienteEmpresaService } from '../../../../services/cliente-empresa.service';" + "\r\n" +
            "\r\n" +
            "import { Sistema } from '../../../../model/Sistema';" + "\r\n" +
            "import { ClienteEmpresa } from '../../../../model/ClienteEmpresa';" + "\r\n" +
            "\r\n" +
            "@Directive()\r\n" +
            "export abstract class CriarEditar" + options.frontBaseName + "Component extends CadastroBaseComponent implements OnInit {\r\n" +
            "    id" + options.entityName + ": number;" + "\r\n" +
            "    preferencia: Sistema;" + "\r\n" +
            "    contexto: CriarEditarContexto;" + "\r\n" +
            "\r\n" +
            "    @ViewChild('acs_criar_editar_recebimento', {static: true}) acsCriarEditarRecebimento: CriarEditarRecebimentoComponent;" + "\r\n" +
            "    @ViewChild('search_dialog') search_dialog: SearchDialogComponent;" + "\r\n" +
            "\r\n" +
            "    opcoesFinalizadora = [" + "\r\n" +
            "        { name: 'Finalizadora', value: ConstTipoFinalizadora.finalizadora }" + "\r\n" +
            "    ];" + "\r\n" +
            "\r\n" +
            "    constructor(\r\n" +
            "        public " + serviceName + ": " + options.entityName + "Service,\r\n" +
            "        public clienteEmpresaService: ClienteEmpresaService," + "\r\n" +
            "\r\n" +
            "        public activatedRoute: ActivatedRoute," + "\r\n" +
            "        public baseServices: CadastroBaseService" + "\r\n" +
            "    ) { super('" + options.accessAlias + "', baseServices); }\r\n" +
            "\r\n" +
            "    ngOnInit(): void {\r\n" +
            "        // Resolvendo o Contexto !" + "\r\n" +
            "        this.contexto = ContextoRecebimentoFactory.createCriarEditarContexto(this.router, this.bag, this.activatedRoute.snapshot.params);" + "\r\n" +
            "        this.preferencia = this.activatedRoute.snapshot.data.prefSistema;" + "\r\n" +
            "\r\n" +
            "        super.ngOnInit();\r\n" +
            "\r\n" +
            "        this.acsCriarEditarRecebimento.empresa = this.empresa;" + "\r\n" +
            "        this.acsCriarEditarRecebimento.contexto = this.contexto.getContextoCaixaDeOrigem();" + "\r\n" +
            "        this.acsCriarEditarRecebimento.finalizadoras = [ConstTipoFinalizadora.finalizadora];" + "\r\n" +
            "    }\r\n" +
            "\r\n" +
            "    ////////////////////////////////////////////////////////////////////////////////////" + "\r\n" +
            "    bindFormValidators() {\r\n" +
            "        const dataAtual = AcsDateTime.now().toMinDate();" + "\r\n" +
            "        this.formModel = this.formBuilder.group({" + "\r\n" +
            "            id: ['']," + "\r\n" +
            "            idEmpresa: [this.empresa.id]," + "\r\n" +
            "\r\n" +
            "            // ------------------- GERAL -------------------- //" + "\r\n" +
            "            dataCaixa: [dataAtual, Validators.required]," + "\r\n" +
            "            turno: ['', [Validators.required, FormComponentValidator.nonzero]]," + "\r\n" +
            "            pdv: this.formBuilder.group({" + "\r\n" +
            "                id: [null, Validators.required]," + "\r\n" +
            "                descricao: ['']" + "\r\n" +
            "            })," + "\r\n" +
            "            idCaixa: [null, Validators.required]," + "\r\n" +
            "            idCaixaGerencia: [null]," + "\r\n" +
            "            operador: this.formBuilder.group({" + "\r\n" +
            "                id: [null, Validators.required]," + "\r\n" +
            "                nome: ['']" + "\r\n" +
            "            })," + "\r\n" +
            "            idVenda: [null]," + "\r\n" +
            "            dataEmissao: [dataAtual, Validators.required]," + "\r\n" +
            "            dataVencimento: [null]," + "\r\n" +
            "            observacao: ['']," + "\r\n" +
            "\r\n" +
            "            // --------------------- DADOS DO CLIENTE ----------------------- //" + "\r\n" +
            "            clienteEmpresa: this.formBuilder.group({" + "\r\n" +
            "                id: [null, Validators.required]," + "\r\n" +
            "                cliente: this.formBuilder.group({" + "\r\n" +
            "                    id: [null]," + "\r\n" +
            "                    nome: ['']" + "\r\n" +
            "                })" + "\r\n" +
            "            })," + "\r\n" +
            "            dependente: [null]," + "\r\n" +
            "            placa: ['']," + "\r\n" +
            "            odometroAnterior: [0]," + "\r\n" +
            "            odometroAtual: [0]," + "\r\n" +
            "\r\n" +
            "           // ------------------- DADOS FISCAIS CONVENIO -------------------- //" + "\r\n" +
            "           documento: ['']," + "\r\n" +
            "           finalizadora: [ConstTipoFinalizadora.finalizadora, Validators.required]," + "\r\n" +
            "           chequeVinculado: ['N']," + "\r\n" +
            "           idFaturaConvenio: [null]," + "\r\n" +
            "\r\n" +
            "           // --------------------- VALORES DA VENDA ---------------------- //" + "\r\n" +
            "           valor: [0], // Validator com ValorContrato" + "\r\n" +
            "           valorCadastro: [0]," + "\r\n" +
            "           valorContrato: [0]," + "\r\n" +
            "           desconto: [0]," + "\r\n" +
            "           credito: [0]," + "\r\n" +
            "\r\n" +
            "           // ------------------- CAMPOS NÃO MOSTRADOS -------------------- //" + "\r\n" +
            "           prazo: [0]," + "\r\n" +
            "           finalidade: [1]," + "\r\n" +
            "           alteraLimite: ['N']," + "\r\n" +
            "           troco: [0], // Valor troco concedido" + "\r\n" +
            "           statusRecebimento: [0]," + "\r\n" +
            "           cpfCnpj: [null]," + "\r\n" +
            "           idQuitacaoFaturaConvenio: [null]," + "\r\n" +
            "\r\n" +
            "           // ********* Campos de FORA ********* //" + "\r\n" +
            "           // a) campos SOMENTE PARA CARTÕES" + "\r\n" +
            "           percTaxa: [0]," + "\r\n" +
            "           percTaxaCadastro: [0]," + "\r\n" +
            "           valorTaxa: [0]," + "\r\n" +
            "           valorTaxaCadastro: [0]," + "\r\n" +
            "           vpeMensagem: ['']," + "\r\n" +
            "           vpeDataEnvio: [null]," + "\r\n" +
            "           tefOperadora: ['']," + "\r\n" +
            "           tefBandeira: ['']," + "\r\n" +
            "           tefIdTransacao: ['']," + "\r\n" +
            "           tefTipoCartao: ['']," + "\r\n" +
            "           nsuTef: ['']," + "\r\n" +
            "           nsuOperacao: ['']," + "\r\n" +
            "           autorizacao: ['']," + "\r\n" +
            "           idClienteMotorista: [null]," + "\r\n" +
            "           idConciliacao: [null]," + "\r\n" +
            "           statusConciliacao: [0]," + "\r\n" +
            "           statusIntegrador: [0]," + "\r\n" +
            "           conectividade: [1]," + "\r\n" +
            "           numParcela: [1]," + "\r\n" +
            "           qtdParcelas: [1]," + "\r\n" +
            "           fiscalIdPagamento: [null]," + "\r\n" +
            "           fiscalIdResposta: [null]," + "\r\n" +
            "           lote: ['']," + "\r\n" +
            "           idRecebimentoRef: [null]," + "\r\n" +
            "           numCartao: ['']," + "\r\n" +
            "           numVenda: ['']," + "\r\n" +
            "           administradoraEmpresa: [null]," + "\r\n" +
            "           idFaturaCartao: [null]," + "\r\n" +
            "\r\n" +
            "          // b) Campos que se aplicam apenas a Cheques:" + "\r\n" +
            "          numAgencia: ['']," + "\r\n" +
            "          numBanco: ['']," + "\r\n" +
            "          numCheque: ['']," + "\r\n" +
            "          numCmc7: ['']," + "\r\n" +
            "          numComp: ['']," + "\r\n" +
            "          numConta: ['']," + "\r\n" +
            "          dataDeposito: [null]," + "\r\n" +
            "          cidade: ['']," + "\r\n" +
            "          uf: ['']," + "\r\n" +
            "          emitente: ['']," + "\r\n" +
            "\r\n" +
            "          // c) Campos que se aplicam apenas a Tickets:" + "\r\n" +
            "          quantidade: [1]," + "\r\n" +
            "\r\n" +
            "          // d) Campos que são preenchidos apenas pelo PDV:" + "\r\n" +
            "          tarifaServico: [0]," + "\r\n" +
            "\r\n" +
            "          // e) Campos de Vales de Funcionario:" + "\r\n" +
            "          funcionario: [null]" + "\r\n" +
            "        });" + "\r\n" +
            "\r\n" +
            "       // Definindo o fomModel da Listagem de Recebimento" + "\r\n" +
            "       this.acsCriarEditarRecebimento.bindFormModel(this.formModel);" + "\r\n" +
            "\r\n" +
            "       // ***************** CHANGE VALIDATORS ***************** //" + "\r\n" +
            "       this.defineChangeValidators();" + "\r\n" +
            "\r\n" +
            "       //" + "\r\n" +
            "       if(this.contexto.getContextoCaixaDeOrigem() === ConstContextoCaixaDeOrigem.CAIXA_GERENCIA) {" + "\r\n" +
            "           // Removendo o obrigatorio do PDV" + "\r\n" +
            "           this.formModel.get('turno').setValidators(null);" + "\r\n" +
            "           this.formModel.get('pdv.id').setValidators(null);" + "\r\n" +
            "           this.formModel.get('idCaixa').setValidators(null);" + "\r\n" +
            "\r\n" +
            "           // Incluindo obrigatoriedade do Caixa Gerencia" + "\r\n" +
            "           this.formModel.get('idCaixaGerencia').setValidators(Validators.required);" + "\r\n" +
            "       }" + "\r\n" +
            "    }\r\n" +
            "\r\n" +
            "    defineChangeValidators() {" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    // ------------------------- OPEN SEARCH ------------------------- //" + "\r\n" +
            "    openSearchDialog(controlName: string) {" + "\r\n" +
            "        if (this.formModel.disabled) return;" + "\r\n" +
            "        this.search_dialog.canCleanField = false;" + "\r\n" +
            "        this.search_dialog.modal_lg = false;" + "\r\n" +
            "\r\n" +
            "        if (controlName === 'cliente') {" + "\r\n" +
            "            if (this.formModel.get('clienteEmpresa.cliente.nome').disabled) return;" + "\r\n" +
            "\r\n" +
            "            const cliente_design = [" + "\r\n" +
            "                 new DialogDesignModel('Nome', 'cliente.nome')," + "\r\n" +
            "                 new DialogDesignModel('CPF/CNPJ', 'cliente.cpfCnpj', false, 'cliente.cpfCnpj', Util.formatCpfCnpj)," + "\r\n" +
            "                 new DialogDesignModel('UF', 'cliente.uf')" + "\r\n" +
            "            ];" + "\r\n" +
            "\r\n" +
            "            this.search_dialog.modal_lg = true;" + "\r\n" +
            "            this.search_dialog.changeSearchValues( 'Pesquisar Cliente', controlName, cliente_design, this.clienteEmpresaService," + "\r\n" +
            "                    '/search', `idEmpresa=${this.empresa.id}&ativo=S&notId=${this.preferencia.clientePadrao.id}` );" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        this.search_dialog.showDialog().subscribe((resp: SearchResponseModel) => { " + "\r\n" +
            "            if (resp.sourceControl === 'cliente') {" + "\r\n" +
            "                const result = resp.result as ClienteEmpresa;" + "\r\n" +
            "                this.defineDadosClienteEmpresa(result);" + "\r\n" +
            "            }" + "\r\n" +
            "        });" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    defineDadosClienteEmpresa(model: ClienteEmpresa) {" + "\r\n" +
            "        this.formModel.get('clienteEmpresa.id').setValue(model.id);" + "\r\n" +
            "        this.formModel.get('clienteEmpresa.cliente.id').setValue(model.cliente.id);" + "\r\n" +
            "        this.formModel.get('clienteEmpresa.cliente.nome').setValue(model.cliente.nome);" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    // ------------------------- OUTROS METODOS ------------------------- //" + "\r\n" +
            "    recebimentoEvent(evento: RecebimentoEvent) {" + "\r\n" +
            "        const model = evento.modelo;" + "\r\n" +
            "         switch(evento.tipo) {" + "\r\n" +
            "            case TipoRecebimentoEvent.VENDA:" + "\r\n" +
            "                // Mudanças para Recebimento e Venda !" + "\r\n" +
            "                break;" + "\r\n" +
            "        }" + "\r\n" +
            "    }" + "\r\n" +
            "}";

        Utils.writeContentTo(path + "criar-editar-" + options.frontBaseFolder + ".component.ts", classBody);
        System.out.println("Generated CREATE/EDIT COMPONENT TS '" + "criar-editar-" + options.frontBaseFolder + ".component.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    // CRIAR
    @Override
    protected void gerarComponentCriar(GenOptions options) throws IOException {
        String serviceName = options.entityName + "Service";
        serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1);

        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\criar-" + options.frontBaseFolder + "\\";

        String classBody = "" +
                "import { OnInit, Component } from '@angular/core';" + "\r\n" +
                "import { ActivatedRoute } from '@angular/router';" + "\r\n" +
                "\r\n" +
                "import { CriarEditar" + options.frontBaseName + "Component } from '../criar-editar-" + options.frontBaseFolder + ".component';" + "\r\n" +
                "\r\n" +
                "import { CadastroBaseService } from '../../../../../cadastros/cadastro-base.service';" + "\r\n" +
                "import { " + options.entityName + "Service } from '../../../../../services/" + options.defaultRoute + ".service';" + "\r\n" +
                "import { ClienteEmpresaService } from '../../../../../services/cliente-empresa.service';" + "\r\n" +
                "\r\n" +
                "import { " + options.entityName + " } from '../../../../../model/" + options.entityName + "';" + "\r\n" +
                "import { Observable } from 'rxjs';" + "\r\n" +
                "import { map } from 'rxjs/operators';" + "\r\n" +
                "\r\n" +
                "@Component({" + "\r\n" +
                "    templateUrl: '../criar-editar-" + options.frontBaseFolder + ".component.html'," + "\r\n" +
                "    styleUrls: [ '../criar-editar-" + options.frontBaseFolder + ".component.scss' ]" + "\r\n" +
                "})" + "\r\n" +
                "export class Criar" + options.frontBaseName + "Component extends CriarEditar" + options.frontBaseName + "Component implements OnInit {" + "\r\n" +
                "    constructor(" + "\r\n" +
                "        public " + serviceName + ": " + options.entityName + "Service," + "\r\n" +
                "        public clienteEmpresaService: ClienteEmpresaService," + "\r\n" +
                "\r\n" +
                "        public activatedRoute: ActivatedRoute," + "\r\n" +
                "        public baseServices: CadastroBaseService" + "\r\n" +
                "    ) {" + "\r\n" +
                "        super(" + serviceName + ", clienteEmpresaService, activatedRoute, baseServices);" + "\r\n" +
                "    }" + "\r\n" +
                "\r\n" +
                "    ngOnInit() {" + "\r\n" +
                "        super.ngOnInit();" + "\r\n" +
                "        this.contexto.setTitle('Novo " + options.frontBaseName + "');" + "\r\n" +
                "\r\n" +
                "        this.title = this.contexto.getTitle();" + "\r\n" +
                "        this.routerLink = this.contexto.getRouterLinkAPartirDe('" + options.frontBaseFolder + "');" + "\r\n" +
                "\r\n" +
                "        // Verificando o contexto a partir de algum CAIXA" + "\r\n" +
                "        if(this.contexto.isOrigemCaixa()) {" + "\r\n" +
                "            this.acsCriarEditarRecebimento.setValuesPrestacaoGerencia(this.contexto.getValuePrestacaoGerencia());" + "\r\n" +
                "        } else {" + "\r\n" +
                "            if(this.contexto.isUrlCaixa())" + "\r\n" +
                "                this.contexto.navegarAPartirDe('" + options.frontBaseFolder + "'); // Se cair aqui e a URL for a mesma, a navegação não ocorre !" + "\r\n" +
                "        }" + "\r\n" +
                "    }" + "\r\n" +
                "\r\n" +
                "    gravarModel(rawValue: any): Observable<any> {" + "\r\n" +
                "        return this.recebimentoService" + "\r\n" +
                "            .observePersist( new Recebimento(rawValue), `/${this.contexto.getUrlContexto()}` )" + "\r\n" +
                "            .pipe(" + "\r\n" +
                "                map( () => {" + "\r\n" +
                "                    this.toasty.success('" + options.frontBaseName + " adicionado com sucesso!');" + "\r\n" +
                "                    this.router.navigate([this.routerLink]);" + "\r\n" +
                "                })" + "\r\n" +
                "            );" + "\r\n" +
                "    }" + "\r\n" +
                "}" + "\r\n" +
                "";

        Utils.writeContentTo(path + "criar-" + options.frontBaseFolder + ".component.ts", classBody);
        System.out.println("Generated CREATE COMPONENT TS 'criar-" + options.frontBaseFolder + ".component.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }

    // EDITAR
    @Override
    protected void gerarComponentEditar(GenOptions options) throws IOException {
        String serviceName = options.entityName + "Service";
        serviceName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1);

        String path = GerarFrontEnd.mainPath + "criar-editar-" + options.frontBaseFolder + "\\editar-" + options.frontBaseFolder + "\\";

        String classBody = "" +
                "import { OnInit, Component } from '@angular/core';" + "\r\n" +
                "import { Validators } from '@angular/forms';" + "\r\n" +
                "import { ActivatedRoute } from '@angular/router';" + "\r\n" +
                "\r\n" +
                "import { CriarEditar" + options.frontBaseName + "Component } from '../criar-editar-" + options.frontBaseFolder + ".component';" + "\r\n" +
                "\r\n" +
                "import { CadastroBaseService } from '../../../../../cadastros/cadastro-base.service';" + "\r\n" +
                "import { " + options.entityName + "Service } from '../../../../../services/" + options.defaultRoute + ".service';" + "\r\n" +
                "import { ClienteEmpresaService } from '../../../../../services/cliente-empresa.service';" + "\r\n" +
                "\r\n" +
                "import { " + options.entityName + " } from '../../../../../model/" + options.entityName + "';" + "\r\n" +
                "\r\n" +
                "import { Observable } from 'rxjs';" + "\r\n" +
                "import { map } from 'rxjs/operators';" + "\r\n" +
                "\r\n" +
                "@Component({" + "\r\n" +
                "    templateUrl: '../criar-editar-" + options.frontBaseFolder + ".component.html'," + "\r\n" +
                "    styleUrls: ['../criar-editar-" + options.frontBaseFolder + ".component.scss']" + "\r\n" +
                "})" + "\r\n" +
                "export class Editar" + options.frontBaseName + "Component extends CriarEditar" + options.frontBaseName + "Component implements OnInit {\r\n" +
                "    constructor(\r\n" +
                "        public " + serviceName + ": " + options.entityName + "Service," + "\r\n" +
                "        public clienteEmpresaService: ClienteEmpresaService," + "\r\n" +
                "\r\n" +
                "        public activatedRoute: ActivatedRoute," + "\r\n" +
                "        public baseServices: CadastroBaseService" + "\r\n" +
                "    ) {" + "\r\n" +
                "        super(" + serviceName + ", clienteEmpresaService, activatedRoute, baseServices);" + "\r\n" +
                "    }" + "\r\n" +
                "\r\n" +
                "    ngOnInit() {" + "\r\n" +
                "        this.editarView = true;" + "\r\n" +
                "        this.idRecebimento = this.activatedRoute.snapshot.params.idRecebimento;" + "\r\n" +
                "\r\n" +
                "        super.ngOnInit();" + "\r\n" +
                "        this.contexto.setTitle('Alterar " + options.frontBaseName + "');" + "\r\n" +
                "\r\n" +
                "        this.title = this.contexto.getTitle();" + "\r\n" +
                "        this.routerLink = this.contexto.getRouterLinkAPartirDe('" + options.frontBaseFolder + "');" + "\r\n" +
                "\r\n" +
                "        // Verificando o contexto a partir de algum CAIXA" + "\r\n" +
                "        if(!this.contexto.isOrigemCaixa()) {" + "\r\n" +
                "            if(this.contexto.isUrlCaixa())" + "\r\n" +
                "                this.contexto.navegarAPartirDe('" + options.frontBaseFolder + "'); // Se cair aqui e a URL for a mesma, a navegação não ocorre !" + "\r\n" +
                "        }" + "\r\n" +
                "\r\n" +
                "        // Preencher valores com o que foi buscado" + "\r\n" +
                "        this.recebimentoService" + "\r\n" +
                "            .getById( this.idRecebimento )" + "\r\n" +
                "            .then( model => {" + "\r\n" +
                "                this.setFormValues( new Recebimento(model) );" + "\r\n" +
                "            }).catch( (err) => this.errorHandler.handle(err) );" + "\r\n" +
                "    }\r\n" +
                "\r\n" +
                "    setFormValues(model: " + options.entityName + ") {\r\n" +
                "        this.formModel.get('id').setValue( model.id );" + "\r\n" +
                "        this.formModel.get('idEmpresa').setValue(model.idEmpresa);" + "\r\n" +
                "\r\n" +
                "        // ------------------- PANEL GERAL -------------------- //" + "\r\n" +
                "        this.formModel.get('dataCaixa').setValue(model.dataCaixa.toDate());" + "\r\n" +
                "        this.formModel.get('turno').setValue(model.turno);" + "\r\n" +
                "\r\n" +
                "        if(this.acsCriarEditarRecebimento.isContextoVenda()) {" + "\r\n" +
                "            this.formModel.get('pdv.id').setValue(model.pdv.id);" + "\r\n" +
                "            this.formModel.get('pdv.descricao').setValue(model.pdv.descricao);" + "\r\n" +
                "        }" + "\r\n" +
                "\r\n" +
                "        this.formModel.get('idCaixa').setValue(model.idCaixa);" + "\r\n" +
                "        this.formModel.get('idCaixaGerencia').setValue(model.idCaixaGerencia);" + "\r\n" +
                "        this.formModel.get('operador.id').setValue(model.operador.id);" + "\r\n" +
                "        this.formModel.get('operador.nome').setValue(model.operador.nome);" + "\r\n" +
                "        this.formModel.get('idVenda').setValue(model.idVenda);" + "\r\n" +
                "\r\n" +
                "        this.formModel.get('dataEmissao').setValue(model.dataEmissao.toDate());" + "\r\n" +
                "        this.formModel.get('observacao').setValue((model.observacao)? model.observacao : '');" + "\r\n" +
                "\r\n" +
                "        // --------------------- DADOS DO CLIENTE ----------------------- //" + "\r\n" +
                "        this.defineDadosClienteEmpresa(model.clienteEmpresa);" + "\r\n" +
                "\r\n" +
                "        if(model.dependente !== null && model.dependente.id !== null) {" + "\r\n" +
                "            this.formModel.get('dependente.id').setValue(model.dependente.id);" + "\r\n" +
                "            this.formModel.get('dependente.nome').setValue(model.dependente.nome);" + "\r\n" +
                "            this.formModel.get('dependente.limCreditoVc').setValue(model.dependente.limCreditoVc);" + "\r\n" +
                "            this.formModel.get('dependente.limUsadoVc').setValue(model.dependente.limUsadoVc);" + "\r\n" +
                "        }" + "\r\n" +
                "\r\n" +
                "        this.formModel.get('placa').setValue(model.placa);" + "\r\n" +
                "        this.formModel.get('odometroAnterior').setValue(model.odometroAnterior);" + "\r\n" +
                "        this.formModel.get('odometroAtual').setValue(model.odometroAtual);" + "\r\n" +
                "\r\n" +
                "        // ------------------- DADOS FISCAIS CONVENIO -------------------- //" + "\r\n" +
                "        this.formModel.get('documento').setValue(model.documento); // Num Vale" + "\r\n" +
                "        this.formModel.get('finalizadora').setValue(model.finalizadora);" + "\r\n" +
                "        this.formModel.get('chequeVinculado').setValue(model.chequeVinculado);" + "\r\n" +
                "        this.formModel.get('idFaturaConvenio').setValue(model.idFaturaConvenio);" + "\r\n" +
                "\r\n" +
                "        // --------------------- VALORES DA VENDA ---------------------- //" + "\r\n" +
                "        this.formModel.get('valor').setValue(model.valor);" + "\r\n" +
                "        this.formModel.get('valorCadastro').setValue(model.valorCadastro);" + "\r\n" +
                "        this.formModel.get('valorContrato').setValue(model.valorContrato);" + "\r\n" +
                "        this.formModel.get('desconto').setValue(model.desconto);" + "\r\n" +
                "        this.formModel.get('credito').setValue(model.credito);" + "\r\n" +
                "\r\n" +
                "        // ------------------- CAMPOS NÃO MOSTRADOS -------------------- //" + "\r\n" +
                "        this.formModel.get('prazo').setValue(model.prazo);" + "\r\n" +
                "        this.formModel.get('finalidade').setValue(model.finalidade);" + "\r\n" +
                "        this.formModel.get('alteraLimite').setValue(model.alteraLimite);" + "\r\n" +
                "        this.formModel.get('troco').setValue(model.troco);" + "\r\n" +
                "        this.formModel.get('statusRecebimento').setValue(model.statusRecebimento);" + "\r\n" +
                "        this.formModel.get('cpfCnpj').setValue(model.cpfCnpj);" + "\r\n" +
                "        this.formModel.get('idQuitacaoFaturaConvenio').setValue(model.idQuitacaoFaturaConvenio);" + "\r\n" +
                "\r\n" +
                "        // ********* Campos de FORA ********* //" + "\r\n" +
                "        // a) campos SOMENTE PARA CARTÕES" + "\r\n" +
                "        this.formModel.get('dataVencimento').setValue(model.dataVencimento.toDate());" + "\r\n" +
                "        this.formModel.get('percTaxa').setValue(model.percTaxa);" + "\r\n" +
                "        this.formModel.get('percTaxaCadastro').setValue(model.percTaxaCadastro);" + "\r\n" +
                "        this.formModel.get('valorTaxa').setValue(model.valorTaxa);" + "\r\n" +
                "        this.formModel.get('valorTaxaCadastro').setValue(model.valorTaxaCadastro);" + "\r\n" +
                "        this.formModel.get('vpeMensagem').setValue(model.vpeMensagem);" + "\r\n" +
                "        this.formModel.get('vpeDataEnvio').setValue(model.vpeDataEnvio.toDate());" + "\r\n" +
                "        this.formModel.get('tefBandeira').setValue(model.tefBandeira);" + "\r\n" +
                "        this.formModel.get('tefIdTransacao').setValue(model.tefIdTransacao);" + "\r\n" +
                "        this.formModel.get('tefOperadora').setValue(model.tefOperadora);" + "\r\n" +
                "        this.formModel.get('tefTipoCartao').setValue(model.tefTipoCartao);" + "\r\n" +
                "        this.formModel.get('nsuTef').setValue(model.nsuTef);" + "\r\n" +
                "        this.formModel.get('nsuOperacao').setValue(model.nsuOperacao);" + "\r\n" +
                "        this.formModel.get('autorizacao').setValue(model.autorizacao);" + "\r\n" +
                "        this.formModel.get('idClienteMotorista').setValue(model.idClienteMotorista);" + "\r\n" +
                "        this.formModel.get('idConciliacao').setValue(model.idConciliacao);" + "\r\n" +
                "        this.formModel.get('statusConciliacao').setValue(model.statusConciliacao);" + "\r\n" +
                "        this.formModel.get('statusIntegrador').setValue(model.statusIntegrador);" + "\r\n" +
                "        this.formModel.get('conectividade').setValue(model.conectividade);" + "\r\n" +
                "        this.formModel.get('numParcela').setValue(model.numParcela);" + "\r\n" +
                "        this.formModel.get('qtdParcelas').setValue(model.qtdParcelas);" + "\r\n" +
                "        this.formModel.get('fiscalIdPagamento').setValue(model.fiscalIdPagamento);" + "\r\n" +
                "        this.formModel.get('fiscalIdResposta').setValue(model.fiscalIdResposta);" + "\r\n" +
                "        this.formModel.get('lote').setValue(model.lote);" + "\r\n" +
                "        this.formModel.get('idRecebimentoRef').setValue(model.idRecebimentoRef);" + "\r\n" +
                "        this.formModel.get('numCartao').setValue(model.numCartao);" + "\r\n" +
                "        this.formModel.get('numVenda').setValue(model.numVenda);" + "\r\n" +
                "        if(model.administradoraEmpresa !== null) {" + "\r\n" +
                "            this.formModel.get('administradoraEmpresa.id').setValue(model.administradoraEmpresa.id);" + "\r\n" +
                "        }" + "\r\n" +
                "        this.formModel.get('idFaturaCartao').setValue(model.idFaturaCartao);" + "\r\n" +
                "\r\n" +
                "        // b) Campos que se aplicam apenas a Cheques:" + "\r\n" +
                "        this.formModel.get('numAgencia').setValue(model.numAgencia);" + "\r\n" +
                "        this.formModel.get('numBanco').setValue(model.numBanco);" + "\r\n" +
                "        this.formModel.get('numCheque').setValue(model.numCheque);" + "\r\n" +
                "        this.formModel.get('numCmc7').setValue(model.numCmc7);" + "\r\n" +
                "        this.formModel.get('numComp').setValue(model.numComp);" + "\r\n" +
                "        this.formModel.get('numConta').setValue(model.numConta);" + "\r\n" +
                "        this.formModel.get('dataDeposito').setValue(model.dataDeposito.toDate());" + "\r\n" +
                "        this.formModel.get('cidade').setValue(model.cidade);" + "\r\n" +
                "        this.formModel.get('uf').setValue(model.uf);" + "\r\n" +
                "        this.formModel.get('emitente').setValue(model.emitente);" + "\r\n" +
                "\r\n" +
                "        // c) Campos que se aplicam apenas a Tickets:" + "\r\n" +
                "        this.formModel.get('quantidade').setValue(model.quantidade);" + "\r\n" +
                "\r\n" +
                "        // d) Campos que são preenchidos apenas pelo PDV:" + "\r\n" +
                "        this.formModel.get('tarifaServico').setValue(model.tarifaServico);" + "\r\n" +
                "\r\n" +
                "        // e) Campos de Vales de Funcionario:" + "\r\n" +
                "        if(model.funcionario !== null) {" + "\r\n" +
                "            this.formModel.get('funcionario.id').setValue(model.funcionario.id);" + "\r\n" +
                "        }" + "\r\n" +
                "    }\r\n" +
                "\r\n" +
                "    gravarModel(rawValue: any): Observable<any> {" + "\r\n" +
                "        return this.recebimentoService" + "\r\n" +
                "            .observeUpdate( new Recebimento(rawValue), this.idRecebimento, `/${this.contexto.getUrlContexto()}` )" + "\r\n" +
                "            .pipe(" + "\r\n" +
                "                map( () => {" + "\r\n" +
                "                    this.toasty.success('" + options.frontBaseName + " Atualizado com Sucesso!');" + "\r\n" +
                "                    this.router.navigate([this.routerLink]);" + "\r\n" +
                "                })" + "\r\n" +
                "            );" + "\r\n" +
                "    }" + "\r\n" +
                "}\r\n" +
                "";

        Utils.writeContentTo(path + "editar-" + options.frontBaseFolder + ".component.ts", classBody);
        System.out.println("Generated EDIT   COMPONENT TS '" + "editar-" + options.frontBaseFolder + ".component.ts' into '" + path + "'");
        System.out.println("------------------------------------------------------------------------------\r\n");
    }
    //endregion

    // region // -------------------- GERACAO DOS MODULOS -------------------- //
    private void incluirPrestacaoModulo(GenOptions options) throws IOException {
        String pathToFile = options.mainFront;

        pathToFile += "modulos\\movimentos\\caixas-de-venda\\caixas-de-venda-routing.module.ts";
        String newLine = "            { path: '" + options.frontBaseFolder + "', loadChildren: () => import('../" + options.frontBaseFolder + "/" + options.frontBaseFolder + ".module').then(m => m." + options.frontBaseName + "Module) }";

        writeLineModule(options.frontBaseFolder, pathToFile, newLine);
    }

    private void incluirGerenciaModulo(GenOptions options) throws IOException {
        String pathToFile = options.mainFront;

        pathToFile += "modulos\\movimentos\\caixas-da-gerencia\\caixas-da-gerencia-routing.module.ts";
        String newLine = "                  { path: '" + options.frontBaseFolder + "', loadChildren: () => import('../" + options.frontBaseFolder + "/" + options.frontBaseFolder + ".module').then(m => m." + options.frontBaseName + "Module) }";

        writeLineModule(options.frontBaseFolder, pathToFile, newLine);
    }

    private void writeLineModule(String frontBaseFolder, String pathToFile, String newLine) throws IOException {
        if (Utils.isAuditionMode()) {
            System.out.println("PATH     => " + pathToFile);
            System.out.println("New LINE => " + newLine);
            return;
        }

        String tmpFile = "./tmp.txt";
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
                if (line.contains("loadChildren:")) {
                    // 2.2 -> Verifica a Ordem alfabética
                    String[] tokens = line.split("'");

                    int comp = frontBaseFolder.compareTo(tokens[1]);
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

                } else if (line.startsWith("]")) { // FIM DO ARQUIVO
                    writted = true;
                    writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }

            writeToFile(Paths.get(tmpFile), (line + ((printComma) ? "," : "") + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

        Files.delete(readModule);
        new File(tmpFile).renameTo(new File(pathToFile));
    }
    //endregion
}
