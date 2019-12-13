package main;

public class TesteModelGenerator {
	public static void main(String[] args) {
		Principal.main(
				"-mf", "./test-script.txt",
				"-eaCOMPRAS E DEVOLUCOES",
				"-front",
				"-ffe",
				"-moduleestoques\\compra-diversos\\criar-editar-compra-diversos",
				"-front-baseitens-compra",
				"-audit"
		);
		// Principal.main("-test");
	}
}
