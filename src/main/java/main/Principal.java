/**
 *
 */
package main;

import java.io.IOException;

/**
 * @author Anderson Dourado Cunha
 */
public class Principal {

    /**
     * @param args
     */
    public static void main(String... args) {
        System.out.println("#######################################################");
        System.out.println("####### Gerador de Entidades para o Gerente-web #######");

        try {
            if (args.length < 2) {
                if (args.length == 1 && args[0].equalsIgnoreCase("-test")) {
                    System.out.println("-> Fazendo TESTE de validacao:");
                    GeradorDeEntidades.gerarEntidadeFrom("-test", "test-script.txt", "-audit", "-front", "-ffe");
                    return;
                }

                System.out.println("-> Erro de solicitação, por favor entre com uma das opções: ");
                printOpcoes();
                return;
            }

            if (args[0].equalsIgnoreCase("-generate") || args[0].equalsIgnoreCase("-g") ||
                    args[0].equalsIgnoreCase("-modelFile") || args[0].equalsIgnoreCase("-mf") ||
                    args[0].equalsIgnoreCase("-report") || args[0].equalsIgnoreCase("-rt") ||
                    args[0].equalsIgnoreCase("-formaPagamento") || args[0].equalsIgnoreCase("-fp")
            ) {
                GeradorDeEntidades.gerarEntidadeFrom(args);
            } else {
                printOpcoes();
                return;
            }

            System.out.println("");
            System.out.println("#######################################################");

        } catch (IOException e) {
            System.out.println("Ocorreu um erro na geração dos arquivos ![CAUSE: " + e.getMessage() + "]");
            e.printStackTrace();
        }
    }

    private static void printOpcoes() {
        String opcoes = "";
        opcoes += "-> Utilize uma das diretivas \r\n"
                + "\t\t-generate [ou -g] <nomeEntidade> [flags ou options] \t para gerar as classes para essa entidade !\r\n"
                + "\t\t-modelFile [ou -mf] <path_to_file> [flags ou options] \t Arquivo SCRIPT que pode ser usado como base para a criação do Modelo.\r\n"
                + "\t\t-report [ou -rt] <path_to_file> [flags ou options] \t Arquivo para ser usado como base para a criação dos Arquivos de Relatório.\r\n"
                + "\t\t-formaPagamento [ou -fp] <grupo_pagamento> [flags ou options] \t Gera a Estrutura e os Arquivos para uma nova Forma de Pagamento.\r\n"
                + "\r\n"

                + "FLAGS: \r\n"
                + "-----------------------------------\r\n"
                + " -noGenerateModel [ou -noGM] \t\t\t\t Indica que a classe modelo não deve ser gerada.\r\n"
                + " -onlyBackEnd [ou -back] \t\t\t\t Gera somente os arquivos de BACK - END.\r\n"
                + " -onlyModel [ou -model] \t\t\t\t Gera somente os arquivos de Modelo.\r\n"
                + " -onlyFrontEnd [ou -front] \t\t\t\t Gera somente os arquivos de FRONT - END.\r\n"
                + " -fullFrontEnd [ou -ffe] \t\t\t\t Gera todos os arquivos no FRONT - END.\r\n"
                + " -parseScript [ou -ps] \t\t\t\t Faz um Novo Arquivo de Scripting para ser usado como base na geração dos modelos.\r\n"
                + " -mount [ou -mt] \t\t\t\t Usado para gerar um arquivo de base para ser usado nos relatórios.\r\n"
                + " -auditionMode [ou -audit] \t\t\t\t Nao faz geracoes, so imprime o resultado em tela (Modo Audição de Teste).\r\n"
                + "\r\n"

                + "OPTIONS: \r\n"
                + "-----------------------------------\r\n"
                + " -r  \t\t\t\t\t\t\t Indica a ROTA padrão para ser usada.\r\n"
                + " -ea \t\t\t\t\t\t\t Indica o ACCESS ALIAS da Entidade para ser usada.\r\n"
                + " -et \t\t\t\t\t\t\t Indica o Table Name da Entidade para ser usada.\r\n"
                + " -module \t\t\t\t\t\t Indica o Módulo usado no FrontEnd (padrão é cadastros).\r\n"
                + " -front-base \t\t\t\t\t\t Indica o Nome base que será criado no Front (por padrão pega o nome da tabela).\r\n\r\n"
                + "\r\n"

                + "TESTE DE VALIDADE: \r\n"
                + "-----------------------------------\r\n"
                + "* CriarEntidadesACS -test \r\n"

                + "Exemplo Valido: \r\n"
                + "* CriarEntidadesACS -generate Produto -rprodutos -eaPRODUTOS -etprodutos \r\n"
                + "* CriarEntidadesACS -mf \"./meu-script.txt\" -ffe \r\n"
                + "* CriarEntidadesACS -mf \"./meu-script.txt\" -modulemovimentos -front-baselivro-caixa -ffe -ea\"LIVRO CAIXA\" \r\n"
                + "* CriarEntidadesACS -report -mount \r\n"
                + "* CriarEntidadesACS -rt \"./report-base.json\" \r\n"
                + "\r\n";

        System.out.println(opcoes);
    }
}
