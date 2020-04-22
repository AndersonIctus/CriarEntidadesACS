# CriarEntidadesACS #

Projeto feito para poder ajudar no parsing de SCRIPTS do sistema para as classes básicas JAVA/ANGULAR

* Esse projeto deve ser copiado para a mesma pasta onde se encontra os Projetos do Gerente Web

### DEPENDÊNCIAS ###
- Ele é um projeto baseado no MAVEN (pom.xml)
- **[GSON](https://mvnrepository.com/artifact/com.google.code.gson/gson/2.8.5)** 

_____________________________
# Geração do Artefato (CriarEntidadesACS.jar)
Após baixar o repositório, para gerar o Artefato, na pasta do Diretorio criado (CriarEntidaesACS) execute o código MAVEN:
```     
$ mvnw package
```     
O arquivo será gerado em ./target/RELEASE/CriarEntidadesACS.jar.

Então esse arquivo deve ser movido para dentro Pasta do Back do Gerente Web
_____________________________
# Como Utilizar #
Essas são as opções dentro do executável:

```
-> Utilize uma das diretivas
  -generate [ou -g] <nomeEntidade> [flags ou options]      Para gerar as classes para essa entidade !
  -modelFile [ou -mf] <path_to_file> [flags ou options]    Arquivo SCRIPT que pode ser usado como base para a criação do Modelo.
  -report [ou -rt] <path_to_file> [flags ou options] 	   Arquivo para ser usado como base para a criação dos Arquivos de Relatório.

FLAGS:
-----------------------------------
 -noGenerateModel [ou -noGM]               Indica que a classe modelo não deve ser gerada.
 -onlyBackEnd [ou -back]                   Gera somente os arquivos de BACK - END.
 -onlyModel [ou -model]                    Gera somente os arquivos de Modelo.
 -onlyFrontEnd [ou -front]                 Gera somente os arquivos de FRONT - END.
 -fullFrontEnd [ou -ffe]                   Gera todos os arquivos no FRONT - END.
 -parseScript [ou -ps]                     Faz um Novo Arquivo de Scripting para ser usado como base na geração dos modelos.
 -mount [ou -mt]                           Usado para gerar um arquivo de base para ser usado nos relatórios. [Use esse arquivo como base]
 -auditionMode [ou -audit]                 Nao faz geracoes, so imprime o resultado em tela (Modo Audição de Teste).


OPTIONS:
-----------------------------------
 -r                 Indica a ROTA padrão para ser usada.
 -ea                Indica o ACCESS ALIAS da Entidade para ser usada.
 -et                Indica o Table Name da Entidade para ser usada.
 -module            Indica o Módulo usado no FrontEnd (padrão é cadastros).
 -front-base        Indica o Nome base que será criado no Front (por padrão pega o nome da tabela).


TESTE DE VALIDADE:
-----------------------------------
* CriarEntidadesACS -test
```

### Estrutura do Report-Base (script .json) ###
Foi criada essa estrutura para facilitar a montagem das classes e arquivos de 'back' e 'front' do projeto.
```
// **********************************************************
// ************ Aqui o modelo que cria o Json ***************
class ReportFileModel {
    String name;         // Nome do report (pode ser o nome do arquivo jasper sem a extenção separado por underline [ex.: listagem_produtos]
    String title;        // Titulo do Report [Ex: Listagem de Produtos]
    String reportType;   // Tipo do Report (aqui segue o nome das pastas do tipo de relatório) [Ex.: "cadastro" ou "operacional"]
    String role;         // Regra de acesso do relatorio [Ex.: "REL. OPERACIONAL RESUMO" ou "REL. LISTAGENS"]
    List<ReportProperty> properties; // Lista de Propriedades/atributos do relatorio (O Objetivo é colocar os campos obrigatorios, não obrigatórios e ocultos usados no relatorio)
}

class ReportProperty {
	String name;         // Nome do campo (Nome que será usado como variável para um campo. Use Camel case do Java) [Ex.: "idEmpresa" ou "observacao"]
	String type;         // Tipo do campo (O tipo do campo considerado para o Java no backend. Também pode usar o tipo Search para facilitar o Front) [Ex.: "AcsDateTime" ou "Integer"]
	String entity;       // Entidade considerada no Front (Só é usada quando o type for 'Search'. Use Entidades do model do back end java) [Ex.: "Empresa" ou "ProdutoEmpresa"]
	String value;        // Valor padrão que ele deve assumir (ele é nulo por padrão) [Ex.: "0" ou "CORPO" ou "S"]
	Boolean required;    // Informa se o campo é requerido ou não para a pesquisa (é falso por padrão) [Ex.: false ou true]
	PropertyFrontValue front; // Alguns dados a mais do do atributo usado no front
}

class PropertyFrontValue {
	String label;        // Label do campo no Front (se não informar pega o "name" do campo) [Ex.: "Empresa Cadastrada" ou "Ativo"]
	String type;         // Tipo que o campo terá no front (se não passado ele pega um valor padrão a partir do "type" do campo) [Ex.: "INPUT", "SELECT", "SEARCH"]
	String group;        // Usado para quando o tipo no front é um "SEARCH" (Se não for passado ele pega a partir do "entity" do campo. o nome deve ser camel case iniciado por minusculo) [Ex.: "empresa" ou "produtoEmpresa" ou "combustivel"]
} 

// **********************************************************
// ***** Aqui o exemplo de como o arquivo deve ficar ********
// ****** Mínimo de detalhes Possível
{
  "name": "listagem_produtos",
  "title": "Listar Produtos",
  "reportType": "cadastro",
  "role": "REL. LISTAGENS",
  "properties": [
    { "name": "idEmpresa" },
    { "name": "ativo", "type": "String" }
  ]
}

// ****** Máximo de detalhes Possível (E necessário)
{
  "name": "listagem_produtos",
  "title": "Listar Produtos",
  "reportType": "cadastro",
  "role": "REL. LISTAGENS",
  "properties": [
    { "name": "idEmpresa", "type": "Search", "entity": "Empresa", "value": "0", "required": true, "front": { "label": "Empresa", "type": "SEARCH", "group": "empresa" } },
    { "name": "ativo", "type": "String", "value": "S", "front": { "label": "Ativo", "type": "select" } }
  ]
}

// Para saber mais, crie o arquivo base com o comando '$ CriarEntidadesACS -rt "./report-base.json"' 
// Nele vão estar as mais variadas maneiras de se criar as propriedades do relatório.
```

### Exemplos Válidos: ###
```
$ CriarEntidadesACS -generate Produto -rprodutos -eaPRODUTOS -etprodutos
$ CriarEntidadesACS -mf "./meu-script.txt" -ffe
$ CriarEntidadesACS -mf "./meu-script.txt" -modulemovimentos -front-baselivro-caixa -ffe -ea"LIVRO CAIXA"
$ CriarEntidadesACS -report -mount
$ CriarEntidadesACS -rt "./report-base.json"
```
