package main.geradores.report.impl;

import main.geradores.GenOptions;
import main.geradores.IGerador;

import java.io.IOException;

public class GerarReportFrontEnd implements IGerador {
    private static String mainPath = "..\\front\\src\\app\\";

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
            System.out.println("Vai gerar o Front end ...");
        }

        System.out.println();
        System.out.println("===============================================");
        System.out.println("===============================================");
    }
}
