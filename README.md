# Fix Processor

Aplicação Java para geração e processamento de mensagens FIX, focada em análise de ordens, integração de sistemas e simulação de cenários do mercado financeiro. Permite criar massas de dados, processar arquivos e gerar relatórios para uso em ambientes de testes, homologação ou integração.

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

## Tecnologias utilizadas
- Java 17+
- Spring Boot (CLI e injeção de dependências)
- JUnit 5 (testes automatizados)
- Gradle (build, dependências e execução)

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
5. **Rodar apenas os testes**
   ```sh
   ./gradlew test
   ```
6. **Ver o relatório dos testes**
   Abra o arquivo abaixo no seu navegador:
   
   `build/reports/tests/test/index.html`

## Configuração
Edite o arquivo `src/main/resources/application.properties` para ajustar:

- Caminhos dos arquivos de entrada/saída
- Parâmetros de geração de massa (quantidade de mensagens, contas, instrumentos, etc)

## Estrutura do Projeto
- `src/main/java` — Código fonte principal
- `src/test/java` — Testes automatizados
- `build/reports/tests/test/index.html` — Relatório dos testes

## Dúvidas?
Abra uma issue ou entre em contato!
