package main.geradores;

import java.io.IOException;

public interface IGerador {
    void gerarArquivos(GenOptions options) throws IOException;
}
