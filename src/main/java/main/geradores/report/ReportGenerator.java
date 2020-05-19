package main.geradores.report;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.geradores.model.utils.ClasseNormalizada;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReportGenerator {
    private ReportFileModel reportModel;

    private String reportName;
    private String className;
    private String defaultRoute;

    public ReportGenerator(String pathToFile) throws IOException {
        Path path = Paths.get(pathToFile);

        if (!path.toFile().exists()) {
            throw new RuntimeException("Arquivo passado não existe[" + path.getFileName() + "]");
        }

        String linhas = Files.lines(path)
                .reduce((strA, strB) -> strA + "\r\n" + strB)
                .orElse(null);

        Type type = new TypeToken<ReportFileModel>() {}.getType();

        Gson g = new Gson();
        reportModel = g.fromJson(linhas, type);
        if(reportModel == null) throw new RuntimeException("O Arquivo passado está configurado errado. Favor use as diretivas '-report -mount' para gerar um arquivo padrão");
        reportModel.normalizeProperties();

        this.reportName = reportModel.getNome().toLowerCase();
        this.className = ClasseNormalizada.normalizeClassName(reportModel.getNome());
        this.defaultRoute = normalizaRotaPadrao(this.reportName);

        System.out.println(this);
    }

    private String normalizaRotaPadrao(String reportName) {
        return reportName.replaceAll("_", "-");
    }

    public String getClassName() {
        return this.className;
    }

    public String getReportName() {
        return this.reportName;
    }

    public String getDefaultRoute() {
        return this.defaultRoute;
    }

    public ReportFileModel getReportModel() {
        return reportModel;
    }

    @Override
    public String toString() {
        String out = "*****************************\r\n" +
                "** reportName => " + reportName + "\r\n" +
                "** className  => " + className + "\r\n" +
                "** route      => " + defaultRoute + "\r\n";
        out += "********* ReportModel ********\r\n";
        out += reportModel + "\r\n";
        out += "*****************************";
        return out;
    }
}
