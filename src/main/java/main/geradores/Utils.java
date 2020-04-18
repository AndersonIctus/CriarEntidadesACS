package main.geradores;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Utils {
    private static boolean auditionMode = false;

    public static void writeContentTo(String pathFile, String classBody) throws IOException {
        if (auditionMode == false) {
            // 1 - Abre o stream e Cria se já não existir !!
            PrintStream arquivo = new PrintStream(pathFile, "UTF-8");

            // 2 - escreve o body no arquivo
            arquivo.println(classBody);

            // 3 - fecha o stream
            arquivo.close();
        } else {
            System.out.println(classBody);
            System.out.println("Criado em: " + pathFile);
            System.out.println();
        }
    }

    public static void createDirectory(String pathDirectory) throws IOException {
        if (auditionMode == false) {
            new File(pathDirectory).mkdir();
        } else {
            System.out.println("Diretório gerado em: " + pathDirectory + "\r\n");
        }
    }

    public static boolean isDirectory(String pathDirectory) throws IOException {
        File file = new File(pathDirectory);
        return file.isDirectory();
    }

    public static void aditionModeOn() {
        auditionMode = true;
    }

    public static void aditionModeOff() {
        auditionMode = false;
    }

    public static boolean isAuditionMode() {
        return auditionMode;
    }
}
