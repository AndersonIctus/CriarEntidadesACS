package main.geradores.model.config;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 	Essa classe ficara responsável por manter o arquivo de configuração do em formato JSON do Gerador de Entidades.
 * ELE É UM SINGLETON !!
 * @author Anderson Dourado
 */
public class Configuracoes {
	private static Configuracoes instance;
	private static String ARQ_CONF_PATH = "./CriarEntidadesConfiguracoes.txt";

	private Map<String, Object> configuracoes;
	private Gson g = new Gson();

	private String confPadrao = "{\r\n" +
			"	\"configuracoes\" : {\r\n" +
			"		\"classes_normalizadas\" : {\r\n" +
			"			\"PLANO_CONTAS\": \"PlanoContas\",\r\n" +
			"			\"POS\": \"POS\",\r\n" +
			"			\"MFES\": \"MFE\",\r\n" +
			"			\"CONCENTRADORES\": \"Concentrador\",\r\n" +
			"			\"FORNECEDORES\": \"Fornecedor\",\r\n" +
			"			\"ITENS_COMBO\": \"ItemCombo\",\r\n" +
			"			\"ITENS_COMPRA\": \"ItemCompra\"\r\n" +
			"		}\r\n" +
			"	}	\r\n" +
			"}";

	@SuppressWarnings("unchecked")
	private Configuracoes() {
		try {
			// 1 - Verifica a existência do arquivo de configurações
			Path path = Paths.get(ARQ_CONF_PATH);

			//  1.1 - Se não exitir então cria passando as configurações padrão !!
			if( path.toFile().exists() == false ) {
				PrintStream arquivo = new PrintStream(ARQ_CONF_PATH, "UTF-8");
				arquivo.println(confPadrao);
				arquivo.close();
			}

			String linhas = Files.lines( path )
					.reduce( (strA, strB) -> strA + "\r\n" + strB)
					.orElse(null);

			Type type = new TypeToken<Map<String, Object>>(){}.getType();
			configuracoes = g.fromJson(linhas, type);
			configuracoes = (Map<String, Object>) configuracoes.get("configuracoes");

			//TODO:  1.2 - Se exitir, então deve verificar se todas as configurações foram colocadas, se alguma não tiver sido, precisa ser colocada !!

		} catch (IOException e) {
			System.out.println("Configuracoes não foram carregadas !!");
			e.printStackTrace();
			throw new RuntimeException("As configurações não poderam ser carregadas, por favro cheque o arquivo '" + ARQ_CONF_PATH + "'");
		}
	}

	public static Configuracoes getInstance() {
		if(instance == null)
			instance = new Configuracoes();

		return instance;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String atribute) {
		Object o = this.configuracoes.get(atribute);
		return (T) o;
	}
}
