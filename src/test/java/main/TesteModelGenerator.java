package main;

public class TesteModelGenerator {
	public static void main(String[] args) {
		Principal.main(
				"-mf", "./sync_notas.txt",
//				"-moduleestoques\\compra-diversos\\criar-editar-compra-diversos",
//				"-front-baseitens-compra",
				"-back",
//				"-model",
				"-audit"
		);
		// Principal.main("-test");
	}
}
