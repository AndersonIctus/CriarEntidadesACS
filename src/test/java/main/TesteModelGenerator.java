package main;

public class TesteModelGenerator {
	public static void main(String[] args) {
		Principal.main(
				"-report",
//				"-mount",
				"./report-base_fatura_avulso.json",
//				"-modulemovimentos",
//				"-front-baseitens-compra",
				"-back",
//				"-front",
//				"-model",
//				"-ffe",
//              "-eaCHEQUES",
				"-audit"
		);
		// Principal.main("-test");
	}
}
