package main;

public class TesteModelGenerator {
	public static void main(String[] args) {
		Principal.main(
				"-fp",
				"carteira-digital",
//				"-mount"
//				"./report-base.json",
//				"-modulemovimentos",
//				"-front-baseitens-compra",
//				"-back",
				"-front",
//				"-file",
//				"-model",
//				"-ffe",
//              "-eaCHEQUES",
				"-audit"
		);
		// Principal.main("-test");
	}
}
