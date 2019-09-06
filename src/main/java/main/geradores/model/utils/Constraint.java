package main.geradores.model.utils;

public class Constraint implements Comparable<Constraint> {
	private String name;
	private ConstraintType type;
	private String[] keys;
	private Reference reference;

	private Constraint(String name, String[] keys) {
		this.name = name;
		this.keys = keys;
	}

	public Constraint(MyMatching mat) {
		// Nome
		this.name = mat.get(1);

		// Type
		setType(mat.get(2));
		if( this.type == ConstraintType.FOREIGN_KEY ) {
			setReference(mat);
		}

		// KEYS !!
		keys = mat.get(3).replaceAll("[\\(\\) ]+", "").split(","); // Retira os parentesis e espaços !
		for (int i = 0; i < keys.length; i++) {
			keys[i] = keys[i].toLowerCase();
		}
	}

	public static Constraint createIndex(String name, String reference, String keys) {
		String Otherkeys[] = keys.replaceAll("[\\(\\) ]+", "").split(","); // Retira os parentesis e espaços !
		for (int i = 0; i < Otherkeys.length; i++) {
			Otherkeys[i] = Otherkeys[i].toLowerCase();
		}

		Constraint c = new Constraint(name, Otherkeys);
		c.type = ConstraintType.INDEX;
		c.reference = new Reference(reference, Otherkeys);

		return c;
	}

	public void setType(String type) {
		if( type.equals("UNIQUE") ) {
			this.type = ConstraintType.UNIQUE;
		} else if( type.equals("PRIMARY KEY") ) {
			this.type = ConstraintType.PRIMARY_KEY;
		} else if( type.equals("FOREIGN KEY") ) {
			this.type = ConstraintType.FOREIGN_KEY;
		} else if( type.equals("INDEX") ) {
			this.type = ConstraintType.INDEX;
		} else {
			throw new RuntimeException("Ocorreu um erro ao tentar mapear a CONSTRAINT do tipo '" + type + "'. Ele não é um tipo valido!");
		}
	}

	private void setReference(MyMatching mat) {
		String[] keys = mat.get(6).replaceAll("[\\(\\) ]+", "").split(","); // Retira os parentesis e espaços !
		this.reference = new Reference(mat.get(5), keys);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getName() {
		return name;
	}

	public ConstraintType getType() {
		return type;
	}

	public String[] getKeys() {
		return keys;
	}

	public Reference getReference() {
		return reference;
	}

	// Verifica se a chave está na constraint
	public boolean hasKey(String key) {
		for(String k : keys) {
			if(k.equalsIgnoreCase(key)) {
				return true;
			}
		}

		return false;
	}

	// Verifica se todas as chaves passadas estão na Constraint
	public boolean hasKeys(String... keys) {
		if(keys.length > this.keys.length) return false;
		for(int i = 0; i < keys.length; i++) {
			if( this.hasKey(keys[i]) == false )
				return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return name + ": " + type + "(" + String.join(" - ", keys) + ")" + ((reference != null)? " Ref -> " + reference : "");
	}

	@Override
	public int compareTo(Constraint o) {
		if(this.type == o.type) {
			return this.name.compareTo(o.name);
		} else {
			return this.type.compareTo(o.type);
		}
	}
}
