package main.geradores.model.utils;

public class Reference {
    private String className;
    private String referenceName;
    private String[] keys;

    public Reference(String className, String[] keys) {
        this.referenceName = className;
        this.className = ClasseNormalizada.normalizeClassName(className);
        this.keys = keys;
        for (int i = 0; i < keys.length; i++) {
            keys[i] = keys[i].toLowerCase();
        }
    }

    public String getClassName() {
        return className;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getClassVariableName() {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    public String[] getKeys() {
        return keys;
    }

    public String getFormatVariableName(String varName) {
        String names[] = varName.replaceAll("id_", "").split("_");
        String nameRet = "";

        for (int i = 0; i < names.length; i++) {
            if (i == 0) {
                nameRet += names[i];
            } else {
                nameRet += names[i].substring(0, 1).toUpperCase() + names[i].substring(1);
            }
        }

        return nameRet;
    }

    @Override
    public String toString() {
        return className + "[" + String.join(", ", keys) + "] from (" + referenceName + ")";
    }
}
