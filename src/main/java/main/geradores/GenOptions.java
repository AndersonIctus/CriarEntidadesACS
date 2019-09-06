package main.geradores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.geradores.model.ModelGenerator;
import main.geradores.model.utils.Property;

public class GenOptions {
	public String entityName;
	public String defaultRoute;
	public String accessAlias;
	public String entityTableName;
	public String modelFile;

	public String frontBaseName;
	public String frontBaseFolder;
	public String frontModuleName = "cadastros";

	public boolean generateModel = true; // Gera modelos por padrão
	public boolean generateEmpresaEntity   = false; // A empresa não é entidade por padrão
	public boolean generateResourceService = true; // A classe de service é sempre gerada !
	public boolean onlyBackEnd = false; // Flag para indicar que somente o back end será gerado !
	public boolean onlyFrontEnd = false; // Flag para indicar que somente o front end será gerado !
	public boolean onlyModel = false; // Flag para indicar que somente o MODEL será gerado !
	public boolean fullFrontEnd = false; // Flag para indicar que se deve gerar todo o front end será gerado !

	private ModelGenerator modelGenerator;
	private List<Property> lsOptionsKeys;

	public GenOptions(String entityName) {
		this.entityName = entityName;

		this.defaultRoute    = entityName+"-route";
		this.entityTableName = entityName+"_TABLE_NAME";
		this.frontBaseName   = this.defaultRoute;
		this.frontBaseFolder = this.defaultRoute;
	}

	public String getFrontNameFrom(String nameToFront) {
		String ret = "";
		for(int i = 0; i < nameToFront.length(); i++) {
			if(i == 0) {
				ret += nameToFront.substring(0, 1).toUpperCase();
			} else {
				if(nameToFront.charAt(i) == '-' || nameToFront.charAt(i) == '_') {
					i++;
					ret += nameToFront.substring(i, i+1).toUpperCase();
				} else {
					ret += nameToFront.charAt(i);
				}
			}
		}

		return ret;
	}

	@Override
	public String toString() {
		String out = "###############################\r\n"
				+ "###### Options: \r\n"
				+ "# entityName      -> '" + entityName + "' \r\n"
				+ "# entityTableName -> '" + entityTableName + "' \r\n"
				+ "# defaultRoute    -> '" + defaultRoute + "' \r\n"
				+ "# accessAlias     -> '" + accessAlias + "' \r\n"
				+ "# frontBaseName   -> '" + frontBaseName + "' \r\n"
				+ "# frontBaseFolder -> '" + frontBaseFolder + "' \r\n"
				+ "# frontModuleName -> '" + frontModuleName + "' \r\n"
				+ "# modelFile       -> '" + ((modelFile == null)? "<modelo padrão>" : modelFile) + "' \r\n"
				+ "###### Flags: \r\n"
				+ "# generateModel -> '" + generateModel + "' \r\n"
				+ "# genEmpEntity  -> '" + generateEmpresaEntity + "' \r\n"
				+ "# genResService -> '" + generateResourceService + "' \r\n"
				+ "# onlyBackEnd   -> '" + onlyBackEnd + "' \r\n"
				+ "# onlyFrontEnd  -> '" + onlyFrontEnd + "' \r\n"
				+ "# fullFrontEnd  -> '" + fullFrontEnd + "' \r\n"
				+ "###############################\r\n";
		return out;
	}

	public void generateModelScript() throws IOException {
		modelGenerator = new ModelGenerator(this.modelFile);
	}

	public ModelGenerator getModelGenerator() {
		return modelGenerator;
	}

	public List<Property> getOptionsKeys() {
		return lsOptionsKeys;
	}

	public void addKey(Property propertie) {
		if(this.lsOptionsKeys == null)
			this.lsOptionsKeys = new ArrayList<>();

		lsOptionsKeys.add(propertie);
	}

	public boolean isComposto() {
		return this.lsOptionsKeys.size() > 1;
	}
}