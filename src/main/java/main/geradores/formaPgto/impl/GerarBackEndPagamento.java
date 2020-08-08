package main.geradores.formaPgto.impl;

import main.geradores.GenOptions;
import main.geradores.Utils;
import main.geradores.model.impl.GerarBackEnd;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class GerarBackEndPagamento extends GerarBackEnd {
    @Override
    public void gerarArquivos(GenOptions options) throws IOException {
        System.out.println("===============================================");
        System.out.println("============ GERANDO BACK END =================");
        if (options.mainBack != null) {
            mainPath = options.mainBack;
        }
        else {
            options.mainBack = mainPath;
        }

        if (options.onlyFrontEnd) {
            System.out.println("Pulando a geração dos arquivos para o BackEnd ...");
        }
        else {
            gerarValidadorFinalizadora(options);
            gerarLinhaFactory(options);
        }

        System.out.println();
        System.out.println("===============================================");
        System.out.println("===============================================");
    }

    private void gerarValidadorFinalizadora(GenOptions options) throws IOException {
        String path = GerarBackEnd.mainPath + "service\\finalizadoraRecebimento\\validador\\impl\\";

        String classBody = "" +
            "package com.innovaro.acs.service.finalizadoraRecebimento.validador.impl;" + "\r\n" +
            "\r\n" +
            "import com.innovaro.acs.model.Recebimento;" + "\r\n" +
            "import com.innovaro.acs.security.AcsAuthConsts;" + "\r\n" +
            "import com.innovaro.acs.service.finalizadoraRecebimento.validador.ConstTipoPermissaoFinalizadora;" + "\r\n" +
            "import com.innovaro.acs.service.finalizadoraRecebimento.validador.ValidadorFinalizadora;" + "\r\n" +
            "" + "\r\n" +
            "public class " + options.frontBaseName + "ValidadorFinalizadora extends ValidadorFinalizadora {" + "\r\n" +
            "\r\n" +
            "    @Override" + "\r\n" +
            "    public String getDescricaoPermissao() {" + "\r\n" +
            "        return \"" + options.frontBaseFolder + "\";" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    @Override" + "\r\n" +
            "    public String getPermissaoFinalizadora(short tipoPermissao) {" + "\r\n" +
            "        switch (tipoPermissao) {" + "\r\n" +
            "            case ConstTipoPermissaoFinalizadora.INCLUIR: return AcsAuthConsts.PERMISSION_INCLUIR_" + options.accessAlias + ";" + "\r\n" +
            "            case ConstTipoPermissaoFinalizadora.ALTERAR: return AcsAuthConsts.PERMISSION_ALTERAR_" + options.accessAlias + ";" + "\r\n" +
            "            case ConstTipoPermissaoFinalizadora.EXCLUIR: return AcsAuthConsts.PERMISSION_EXCLUIR_" + options.accessAlias + ";" + "\r\n" +
            "        }" + "\r\n" +
            "\r\n" +
            "        return null;" + "\r\n" +
            "    }" + "\r\n" +
            "\r\n" +
            "    @Override" + "\r\n" +
            "    public void validarCampos(Recebimento model) {" + "\r\n" +
            "        // Incluir as validacoes dos campos" + "\r\n" +
            "    }" + "\r\n" +
            "}" + "\r\n" +
            "";

        Utils.writeContentTo(path + options.frontBaseName + "ValidadorFinalizadora.java", classBody);
        System.out.println("Generated Entity '" + options.frontBaseName + "ValidadorFinalizadora' into '" + path + "'");
        System.out.println("-----------------------------------------------");
    }

    private void gerarLinhaFactory(GenOptions options) throws IOException {
        String pathToFile = options.mainBack;
        pathToFile += "service/finalizadoraRecebimento/validador/ValidadorFinalizadoraFactory.java";

        String newLine = "" +
                "            // Grupo para " + options.frontBaseName + "\r\n" +
                "            case " + options.accessAlias + ":" + "\r\n" +
                "                validador = appCxt.getBean(" + options.frontBaseName + "ValidadorFinalizadora.class);" + "\r\n" +
                "                break;" + "\r\n";


        incluirImport(pathToFile, options.frontBaseName + "ValidadorFinalizadora");
        incluirNewLine(pathToFile, newLine, options.frontBaseName);
    }

    private void incluirImport(String pathToFile, String classFinalizadoraName) throws IOException {
        String baseImportLine = "import com.innovaro.acs.service.finalizadoraRecebimento.validador.impl.";
        String newLine = baseImportLine + classFinalizadoraName + ";";
        writeInfoToFile(
                pathToFile, newLine,
                baseImportLine,
                baseImportLine.length(),
                classFinalizadoraName,
                "import org.springframework.beans.factory.annotation.Autowired;"
        );
    }

    private void incluirNewLine(String pathToFile, String newLine, String compareToken) throws IOException {
        writeInfoToFile(
                pathToFile, newLine,
                "// Grupo",
                "            // Grupo para ".length(),
                compareToken,
                "default:"
        );
    }

    private void writeInfoToFile(String pathToFile, String newLine,
                                 String containsSearchLine, int lenInicioBusca,
                                 String compareToken, String tokenFinalizacao) throws IOException {
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
        boolean iniciaBusca = false;

        for (String line : lines) {
            if (!writted) {
                if( line.contains(containsSearchLine) ) {
                    iniciaBusca = true;

                    // Compara o que está no arquivo
                    String token = line.substring(lenInicioBusca);
                    int comp = compareToken.compareTo(token);
                    if (comp < 0) {
                        writted = true;
                        writeToFile(Paths.get(tmpFile), (newLine + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    } else if (comp == 0) { // Se estiver repetindo (Mesmo nome do token)
                        writted = true;
                    }
                }
                else if(iniciaBusca && line.contains(tokenFinalizacao)) {
                    writted = true;
                    writeToFile(Paths.get(tmpFile), ("\r\n" + newLine).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }

            writeToFile(Paths.get(tmpFile), (line + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

        Files.delete(readModule);
        new File(tmpFile).renameTo(new File(pathToFile));
    }
}
