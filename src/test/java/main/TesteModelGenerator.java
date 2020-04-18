package main;

public class TesteModelGenerator {
	public static void main(String[] args) {
		Principal.main(
				"-report",
//				"-mount",
				"./report-base-outro.json",
//				"-moduleestoques\\compra-diversos\\criar-editar-compra-diversos",
//				"-front-baseitens-compra",
				"-front",
//				"-model",
				"-audit"
		);
		// Principal.main("-test");
	}
}
