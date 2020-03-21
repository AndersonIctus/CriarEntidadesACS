package main;

public class TesteModelGenerator {
	public static void main(String[] args) {
		Principal.main(
				"-mf", "./itens_venda.txt",
//				"-moduleestoques\\compra-diversos\\criar-editar-compra-diversos",
//				"-front-baseitens-compra",
				"-front",
				"-model",
				"-audit"
		);
		// Principal.main("-test");
	}
}
