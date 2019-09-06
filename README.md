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
  -generate [ou -g] <nomeEntidade> [flags ou options]   para gerar as classes para essa entidade !
  -modelFile [ou -mf] <path_to_file> [flags ou options]    Arquivo SCRIPT que pode ser usado como base para a criação do Modelo.


FLAGS:
-----------------------------------
 -noGenerateModel [ou -noGM]               Indica que a classe modelo não deve ser gerada.
 -generateEmpresaEntity [ou -genEmp]       Gera as classes considerando a empresa como Chave da entidade.
 -onlyBackEnd [ou -back]                   Gera somente os arquivos de BACK - END.
 -onlyModel [ou -model]                    Gera somente os arquivos de Modelo.
 -onlyFrontEnd [ou -front]                 Gera somente os arquivos de FRONT - END.
 -fullFrontEnd [ou -ffe]                   Gera todos os arquivos no FRONT - END.
 -parseScript [ou -ps]                     Faz um Novo Arquivo de Scripting para ser usado como base na geração dos modelos.
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

### Exemplo Valido: ###
```
$ CriarEntidadesACS -generate Produto -rprodutos -eaPRODUTOS -etprodutos
$ CriarEntidadesACS -mf "./meu-script.txt" -ffe
$ CriarEntidadesACS -mf "./meu-script.txt" -modulemovimentos -front-baselivro-caixa -ffe -ea"LIVRO CAIXA"
```

