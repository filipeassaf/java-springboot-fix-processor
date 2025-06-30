# Fix Processor

Aplicação Java para geração e processamento de mensagens FIX, focada em análise de ordens, integração de sistemas e simulação de cenários do mercado financeiro. Permite criar massas de dados, processar arquivos e gerar relatórios para uso em ambientes de testes, homologação ou integração.

## Abrindo o projeto em uma IDE

Você pode abrir este projeto no [Visual Studio Code](https://code.visualstudio.com/) (VS Code) ou em qualquer IDE que suporte Spring Boot e Gradle, como IntelliJ IDEA ou Eclipse.

**Recomendações para VS Code:**
- Instale as extensões "Extension Pack for Java" e "Spring Boot Extension Pack" para melhor experiência.
- Abra a pasta do projeto pelo menu `Arquivo > Abrir Pasta...`.
- O VS Code irá detectar o projeto Gradle e sugerir tarefas de build, execução e depuração.

**Outras IDEs:**
- Importe o projeto como um projeto Gradle.
- Certifique-se de que a IDE está configurada para usar Java 17+.
- Utilize os recursos nativos da IDE para rodar, depurar e testar aplicações Spring Boot.

## Como rodar o projeto
1. **Clonar o repositório**
   ```sh
   git clone https://github.com/filipeassaf/java-springboot-fix-processor
   ```
2. **Entrar na pasta do projeto**
   ```sh
   cd java-springboot-fix-processor
   ```
3. **Compilar, rodar todos os testes e gerar os artefatos**
   ```sh
   ./gradlew build
   ```
4. **Executar a aplicação (menu interativo)**
   ```sh
   java -jar build/libs/processor-0.0.1-SNAPSHOT.jar
   ``` 
   > Obs. 1: Você também pode executar a aplicação diretamente pela sua IDE (VS Code, IntelliJ, Eclipse) usando o botão "Run" ou "Play" na classe principal `ProcessorApplication`.

   > Obs. 2: Não utilizar `./gradlew run` pois o menu interativo da aplicação depende de entrada de dados pelo terminal.

## Menu principal da aplicação
Ao executar a aplicação, você verá o seguinte menu no terminal:

```
Bem-vindo ao Fix Processor
Escolha uma opção:
1 - Gerar input_fix.txt (Parte 1)
2 - Gerar AllMsgs.csv (Parte 2.1)
3 - Gerar FulFill.txt (Parte 2.2)
4 - Executar tudo (1 > 2 > 3)
0 - Sair
```

### O que faz cada opção?
- **1 - Gerar input_fix.txt (Parte 1):** Gera o arquivo `data/input_fix.txt` com mensagens FIX simuladas.
- **2 - Gerar AllMsgs.csv (Parte 2.1):** Processa o arquivo `data/input_fix.txt` e gera o arquivo `data/AllMsgs.csv` com os dados estruturados em formato CSV.
- **3 - Gerar FulFill.txt (Parte 2.2):** Processa o arquivo `data/input_fix.txt` e gera o arquivo `data/FulFill.txt` com execuções completas e cálculos automáticos.
- **4 - Executar tudo:** Executa as três etapas acima em sequência, gerando todos os arquivos de uma vez.
- **0 - Sair:** Encerra a aplicação.

## Configuração
Edite o arquivo `src/main/resources/application.properties` para ajustar:

- Caminhos dos arquivos de entrada/saída
- Parâmetros de geração de massa (quantidade de mensagens, contas, instrumentos, etc)

## Tecnologias utilizadas
- Java 17+
- Spring Boot (CLI e injeção de dependências)
- JUnit 5 (testes automatizados)
- Gradle (build, dependências e execução)
- QuickFIX/J (protocolo FIX)
- Multithreading (processamento paralelo)

## Recursos adicionais:
- Utiliza multithreading para processamento eficiente das mensagens.
- Integração com a biblioteca [QuickFIX/J](https://www.quickfixj.org/) para manipulação do protocolo FIX.

## Melhorias Futuras

- Implementar testes automatizados, incluindo cenários multi-thread e de integração.
- Adicionar validação e tratamento de exceções mais detalhados.
- Otimizar o desempenho das rotinas multi-thread.
- Incluir métricas e logs para monitoramento do processamento.

## Dúvidas?
Abra uma issue ou entre em contato!
