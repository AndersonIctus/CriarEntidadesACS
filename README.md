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
// Classe com os atributos necessário para criar a estrutura
class ReportFileModel {
    String nome;         // Nome do Relatorio (pode ser o nome do arquivo jasper sem a extenção separado por underline [ex.: listagem_produtos]
    String titulo;       // Titulo do Relatorio [Ex: Listagem de Produtos]
    String dominio;      // Domínio do Relatorio (aqui segue o nome das pastas do tipo de relatório) [Ex.: "cadastro" ou "operacional"]
    String permissao;    // Permissão de acesso do Relatorio [Ex.: "REL. OPERACIONAL RESUMO" ou "REL. LISTAGENS"]
    List<ReportProperty> properties; // Lista de Propriedades/atributos do relatorio (O Objetivo é colocar os campos obrigatorios, não obrigatórios e ocultos usados no relatorio)
}

// Classe com os valores padrão usados principalmente no back
class ReportProperty {
    String name;         // Nome do campo (Nome que será usado como variável para um campo. Use Camel case do Java) [Ex.: "idEmpresa" ou "observacao"]
    String type;         // Tipo do campo (O tipo do campo considerado para o Java no backend. Também pode usar o tipo Search para facilitar o Front) [Ex.: "AcsDateTime" ou "Integer"]
    String entity;       // Entidade considerada no Front (Só é usada quando o type for 'Search'. Use Entidades do model do back end java) [Ex.: "Empresa" ou "ProdutoEmpresa"]
    String value;        // Valor padrão que ele deve assumir (ele é nulo por padrão) [Ex.: "0" ou "CORPO" ou "S"]
    Boolean required;    // Informa se o campo é requerido ou não para a pesquisa (é falso por padrão) [Ex.: false ou true]
    PropertyFrontValue front; // Alguns dados a mais do do atributo usado no front
}

// Classe com os valores padrão usados exclusivamente no front
class PropertyFrontValue {
    String label;        // Label do campo no Front (se não informar pega o "name" do campo) [Ex.: "Empresa Cadastrada" ou "Ativo"]
    String type;         // Tipo que o campo terá no front (se não passado ele pega um valor padrão a partir do "type" do campo) [Ex.: "INPUT", "SELECT", "SEARCH"]
    String group;        // Usado para quando o tipo no front é um "SEARCH" ou "FILTER" (Se não for passado ele pega a partir do "entity" do campo. o nome deve ser camel case iniciado por minusculo) [Ex.: "empresa" ou "produtoEmpresa" ou "combustivel"]
    Integer inteiro;     // Usado para informar a parte inteira dos campos numericos ou decimais (O padrão é 1 para tipos numericos ou decimais type: 'number' || type: 'decimal') [Ex.: inteiro: 8]
    Integer decimal;     // Usado para informar as casas decimais dos campos decimais (O padrão é 2 para typos decimais - type: 'decimal') [Ex.: decimal: 2]
    Boolean zerosLeft;   // Usado para indicar aos campos numericos se deve ou não completar com zeros a esquerda (type: 'number') [Ex.: zerosLeft: true]
    Map<String, String> options; // Mapa de opções que é usado para um Select ou Radio Button (A formatação segue o padrão  "valor-chave": "label no front") [Ex.: options: { "R": "Relatório Resumido", "D": "Relatório Detalhado" }] 
} 

// **********************************************************
// ***** Aqui o exemplo de como o arquivo deve ficar ********
// ****** Mínimo de detalhes Possível
{
  "nome": "listagem_produtos",
  "titulo": "Listar Produtos",
  "dominio": "cadastro",
  "permissao": "REL. LISTAGENS",
  "propriedades": [
    { "name": "idEmpresa", "entity": "Empresa", "front": { "type": "search" } },
    { "name": "tipoRelatorio", "front": { "type": "radio" } },
    { "name": "serie", "front": { "type": "number" } },
    { "name": "valorInicial", "front": { "type": "decimal" } },
    { "name": "nome", "type": "String" },
    { "name": "idade" },
  ]
}

// ****** Máximo de detalhes Possível (E necessário)
{
  "nome": "listagem_produtos",
  "titulo": "Listar Produtos",
  "dominio": "cadastro",
  "permissao": "REL. LISTAGENS",
  "propriedades": [
    { "name": "idEmpresa", "type": "Search", "entity": "Empresa", "value": "0", "required": true, "front": { "label": "Empresa", "type": "search", "group": "empresa" } },
    { "name": "tipoRelatorio", "type": "String", "value": "D", "required": true, "front": { "label": "Apresentação", "type": "radio", "options": {"R": "Resumido", "D": "Detalhado"} } },
    { "name": "serie", "type": "String", "value": "0", "required": false, "front": { "label": "Série", "type": "number", "inteiro": 3, "zerosLeft": true } },
    { "name": "valorInicial", "type": "BigDecimal", "value": "0,00", "required": false, "front": { "label": "Valor Final", "type": "decimal", "inteiro": 11, "decimal": 2 } },
    { "name": "ativo", "type": "String", "value": "S", "front": { "label": "Ativo", "type": "select" } }
  ]
}

// Para saber mais, crie o arquivo base com o comando '$ CriarEntidadesACS -rt "./report-base.json"' 
// Nele vão estar as mais variadas maneiras de se criar as propriedades do relatório.
```
### Tipos que podem ser utilizados no Report-Base (script .json) ###
Aqui são os tipos que podem ser utilizados no arquivo.json.
 
    Se nenhum type for passado nem no back e nem no front.
    Por padrão os valores ficam:
    Back => Interger
    Front => input 

Tipos do filtro no back:
```
/** **********
   - No back, os tipos estão localizados no ReportProperty.type
   - Cada type, também representa um valor padrão para o front, caso um não seja especificado. 
     Isso quer dizer que types do back podem gerar automáticamente types no front e vice-versa. 
Então aqui estará o Tipo para o Back, e o padrão usado no Front 
************** */
TYPE             BACK             FRONT
----------       ----------       ----------
Integer          Integer          input
Long             Long             input
Double           Double           input
Character        Character        input
String           String           input
Boolean          Boolean          checkbox
BigDecimal       BigDecimal       decimal
AcsDateTime      AcsDateTime      date
Search           Integer          search
Filter           List<Interger>   filter

/* 
- Muitas vezes somente o type principal não é o suficiente. 
- E outras vezes, o type no back, pode ser diferente do type padrão no front
- Também, para tipos como Search e Filter, o front necessita de um group, que por vezes, 
pode não ser o mesmo gerado pela entity passada no back.
*/
```

Tipos do filtro no front:
```
/** **********
   - No front, os tipos estão localizados no PropertyFrontValue.type
   - Os types aqui dizem respeito ao COMPONENTE HTML que será criado no front.
   - Cada type, também representa um valor padrão para o back, INDEPENDENTE se um já foi especificado. 
     Isso quer dizer que types do front são, em maioria, mandatórios ao back. 
Então aqui estará o Tipo para o Front, e o padrão usado no Back 
************** */
TYPE             FRONT            BACK
----------       ----------       ----------
Select           select           String
Radio            radio            String
Checkbox         checkbox         String
TextArea         textarea         String
Decimal          decimal          BigDecimal
Search           search           Interger
Filter           filter           List<Interger>
Date             date             AcsDateTime

// Mas também existe alguns Types que NÃO OBRIGAM um tipo no back.
// Para esse tipos, o valor padrão usado no back é o Integer
TYPE      
----------
Input
Number

/* 
- Da mesma maneira, os types do front nem sempre serão o suficiente
- Para Search e Inputs, pode ser que o atributo 'group' seja necessário para especificar 
o grupo que deve usado.
- E outras vezes, o type no back, pode ser dirente do type padrão no front
*/ 
```
### Explicação mais Detalhada dos Tipos (script .json) ###
<b>Tipos Search e Filter:</b>
```
- Esse são os tipo para entidades.
- A propriedade 'entity' OBRIGATORIAMENTE deve ser passada.
- Para o front, ele usa como padrão para o grupo o nome da entidade, porém se o grupo que essa 
entidade estiver for diferente do nome da entidade, eEntão ele deve ser informado.

Ex.:
=> Simples 
   { "name": "idEmpresa", "type": "Search", "entity": "Empresa", "front": { "label": "Empresa" } }
* Nesse caso, ele vai criar uma propriedade Search no Front do grupo 'empresa' e Integer no back.
* Essa propriedade gera o mesmo da seguinte:
   { "name": "idEmpresa", "type": "Search", "entity": "Empresa", "front": { "label": "Empresa", "type": "search", "group": "empresa" } }

=> Com grupo
Se tivermos a propriedade Funcionário "Operador" e uma outra Funcionário "Vendedor". Devemos separar o grupo 
dessas duas propriedades:
  { "name": "idOperador", "type": "Search", "entity": "Funcionario", "front": { "label": "Operador do Caixa", "group": "operador" } }
e 
  { "name": "idVendedor", "type": "Search", "entity": "Funcionario", "front": { "label": "Vendido por:", "group": "vendedor" } }

Nesse caso, se não fizessemos isso, ele incluiria o grupo a 'funcionario' que é a entidade base dessa propriedade.
-> No front, o service chamado será o mesmo para os dois, 'funcionarioService')
-> No Back, teremos duas propriedades:
   Integer idOperador;
   Integer idVendedor;

** Todas as regras se aplicam ao type "filter". O que muda é que no back cria-se uma Lista de Ids,
e no front, o componente usado é um Filter para escolher multiplas entidades.
```

<b>Tipos de Select e RadioButton:</b>
```
- Esse são os tipo para escolhas e normalmente eles são required.
- Aqui há a possibilidade de customizarmos os valores usados no front (PropertyFrontValue.options).
Caso não seja feito, valores padrão serão usados.

Ex.:
=> Simples
  { "name": "tipoRelatorio", "front": { "label": "Modo Relatorio", "type": "radio", "options": {"C": "Completo", "R": "Resumido"} } }
* Nesse caso, ele vai criar: 
  - back: private String tipoRelatorio = "C";
  - front: Radio button com as opções "Completo" e "Resumido", com o valor "C" já selecionado;

* Essa propriedade gera o mesmo da seguinte:
  { "name": "tipoRelatorio", "type": "String", "value": "C", "required": true, "front": { "label": "Modo Relatorio", "type": "radio", "options": {"C": "Completo", "R": "Resumido"} } },

** Todas as regras se aplicam ao type "select". Nada muda no back, e no front, o componente usado é um Select.
** Sempre utilize o "options" do front para criar já todos os valores necessários.
** Se você não utilizar um "value", então a primeira opção será usada.
```

<b>Tipos Numéricos</b>
```
- Esse são os tipo para usar campos Númericos ou Decimais
- Aqui deve-se utilizar as propriedades 'inteiro', 'decimal' e 'zerosLeft' para determianr como o input vai se comportar

Ex.:
=> Somente Número
  { "name": "idade", "front": { "label": "Idade", "type": "number", "inteiro": 2 } }

- back: private Integer idade; // Pode-se usar o Type do back para forçar o tipo no back ("type": "String", gera o tipo String no back)
- front: gera um elmento Input que só permite numéricos com o máximo de dois dígitos inteiros
  * o zerosLeft, é usado para quando o campo tem q ser preenchido com zeros a esquerda, por padrão ele não preenche.
* Essa propriedade gera o mesmo da seguinte:
  { "name": "idade", "type": "Interger", "front": { "label": "Idade", "type": "number", "inteiro": 2 } }

=> Decimais
  { "name": "valor", "front": { "label": "Valor da Nota", "type": "decimal", "inteiro": 7, "decimal": 3 } }

- back: private BigDecimal valor;
- front: gera um elemnto Input de Decimais que tem o tamanho 11 (sendo 3 podendo ser decimais e uma virgula)
  * zerosLeft não é usado para decimais    
* Essa propriedade gera o mesmo da seguinte:
   { "name": "valor", "type": "BigDecimal", "front": { "label": "Valor Final", "type": "decimal", "inteiro": 7, "decimal": 3 } }

** Se não informar os decimais, ele usa '2' como padrão
```

### Exemplos Válidos: ###
```
$ CriarEntidadesACS -generate Produto -rprodutos -eaPRODUTOS -etprodutos
$ CriarEntidadesACS -mf "./meu-script.txt" -ffe
$ CriarEntidadesACS -mf "./meu-script.txt" -modulemovimentos -front-baselivro-caixa -ffe -ea"LIVRO CAIXA"
$ CriarEntidadesACS -report -mount
$ CriarEntidadesACS -rt "./report-base.json"
```
