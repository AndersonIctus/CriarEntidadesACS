package main.geradores.model.utils;

import main.geradores.model.config.Configuracoes;

import java.util.Map;

public class ClasseNormalizada {

    private static ClasseNormalizada instance = new ClasseNormalizada();
    private Map<String, String> classes = Configuracoes.getInstance().get("classes_normalizadas");

    private ClasseNormalizada() {
    }

    private static String paraSingular(String className) {
        if (className.toLowerCase().endsWith("res")) {
            className = className.substring(0, className.length() - 2); // CONCENTRADORES -> CONCENTRADOR
        } else if (className.toLowerCase().endsWith("oes")) {
            className = className.substring(0, className.length() - 3) + "ao"; // PADROES -> PADRAO
        } else if (className.charAt(className.length() - 1) == 's') {
            className = className.substring(0, className.length() - 1);
        }

        return className;
    }

    public static String normalizeClassName(String className) {
        // Classes que não seguiram a normalização de nomes de classes !!
        String camelClass = "";
        if ((camelClass = instance.classes.get(className)) != null) {
            return camelClass;
        }

        // 1 - Quebra o className por '_' underline
        String classes[] = className.split("_");
        camelClass = "";
        for (String name : classes) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            camelClass += paraSingular(name);
        }

        return camelClass;
    }
}
