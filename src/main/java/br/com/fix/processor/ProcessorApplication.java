package br.com.fix.processor;

import java.io.IOException;
import java.util.Scanner;
import java.time.Duration;
import java.time.Instant;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import br.com.fix.processor.generator.FixMessageGenerator;
import br.com.fix.processor.generator.FixMessageGenerator2;
import br.com.fix.processor.generator.FixMessageGenerator3;
import br.com.fix.processor.handler.FixMessageHandler;
import br.com.fix.processor.handler.FixMessageHandler2;
import br.com.fix.processor.handler.FixMessageHandler3;

@SpringBootApplication
public class ProcessorApplication implements CommandLineRunner {

    @Value("${app.input-path}")
    private String inputPath;
    @Value("${app.csv-path}")
    private String csvPath;
    @Value("${app.fulfill-path}")
    private String fulfillPath;

    @Autowired
    private FixMessageGenerator generator;
    @Autowired
    private FixMessageGenerator2 generator2;
    @Autowired
    private FixMessageGenerator3 generator3;
    @Autowired
    private FixMessageHandler handler;
    @Autowired
    private FixMessageHandler2 handler2;
    @Autowired
    private FixMessageHandler3 handler3;

    public static void main(String[] args) {
        SpringApplication.run(ProcessorApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        // Só executa o menu se houver entrada padrão disponível
        if (System.console() != null || System.in.available() > 0) {
            runMenu();
        }
    }

    // Método para executar o menu (pode ser chamado após o contexto Spring estar pronto)
    public void runMenu() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nBem-vindo ao Fix Processor");
        System.out.println("Escolha uma opção:");
        System.out.println("1 - Gerar input_fix.txt (Parte 1 - Manual)");
        System.out.println("2 - Gerar AllMsgs.csv (Parte 2.1 - Manual)");
        System.out.println("3 - Gerar FulFill.txt (Parte 2.2 - Manual)");
        System.out.println("4 - Executar tudo (1 > 2 > 3)");
        System.out.println();
        System.out.println("5 - Gerar input_fix.txt (Parte 1 - QuickFIX/J)");
        System.out.println("6 - Gerar AllMsgs.csv (Parte 2.1 - QuickFIX/J)");
        System.out.println("7 - Gerar FulFill.txt (Parte 2.2 - QuickFIX/J)");
        System.out.println();
        System.out.println("8 - Gerar input_fix.txt (Parte 1 - MultiThread)");
        System.out.println("9 - Gerar AllMsgs.csv (Parte 2.1 - MultiThread)");
        System.out.println("10 - Gerar FulFill.txt (Parte 2.2 - MultiThread)");
        System.out.println();
        System.out.println("0 - Sair");
        while (true) {
            System.out.print("Digite a opção: ");
            int option = Integer.parseInt(scanner.nextLine());
            switch (option) {
                case 1:
                    Instant start1 = Instant.now();
                    generator.generateFixMessages(inputPath);
                    Instant end1 = Instant.now();
                    System.out.println("Arquivo input_fix.txt gerado com sucesso (" + Duration.between(start1, end1).toMillis() + " ms)");
                    break;
                case 2:
                    Instant start2 = Instant.now();
                    handler.processFixFile(inputPath, csvPath);
                    Instant end2 = Instant.now();
                    System.out.println("Arquivo AllMsgs.csv (Manual) gerado com sucesso (" + Duration.between(start2, end2).toMillis() + " ms)");
                    break;
                case 3:
                    Instant start3 = Instant.now();
                    handler.processFullFills(inputPath, fulfillPath);
                    Instant end3 = Instant.now();
                    System.out.println("Arquivo FulFill.txt gerado com sucesso (" + Duration.between(start3, end3).toMillis() + " ms)");
                    break;
                case 4:
                    Instant start4 = Instant.now();
                    generator.generateFixMessages(inputPath);
                    handler.processFixFile(inputPath, csvPath);
                    handler.processFullFills(inputPath, fulfillPath);
                    Instant end4 = Instant.now();
                    System.out.println("Todos arquivos gerados com sucesso. (" + Duration.between(start4, end4).toMillis() + " ms)");
                    break;
                case 5:
                    Instant start5 = Instant.now();
                    generator2.generateFixMessages(inputPath);
                    Instant end5 = Instant.now();
                    System.out.println("Arquivo input_fix.txt (QuickFIX/J) gerado com sucesso (" + Duration.between(start5, end5).toMillis() + " ms)");
                    break;
                case 6:
                    Instant start6 = Instant.now();
                    handler2.processFixFile(inputPath, csvPath);
                    Instant end6 = Instant.now();
                    System.out.println("Arquivo AllMsgs.csv (QuickFIX/J) gerado com sucesso (" + Duration.between(start6, end6).toMillis() + " ms)");
                    break;
                case 7:
                    Instant start7 = Instant.now();
                    handler2.processFullFills(inputPath, fulfillPath);
                    Instant end7 = Instant.now();
                    System.out.println("Arquivo FulFill.txt (QuickFIX/J) gerado com sucesso (" + Duration.between(start7, end7).toMillis() + " ms)");
                    break;
                case 8:
                    Instant start8 = Instant.now();
                    // MultiThread input_fix.txt
                    try {
                        generator3.generateFixMessagesMultiThread(inputPath, 4);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Instant end8 = Instant.now();
                    System.out.println("Arquivo input_fix.txt (MultiThread) gerado com sucesso (" + Duration.between(start8, end8).toMillis() + " ms)");
                    break;
                case 9:
                    Instant start9 = Instant.now();
                    try {
                        handler3.processFixFileMultiThread(inputPath, csvPath, 4);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Instant end9 = Instant.now();
                    System.out.println("Arquivo AllMsgs.csv (MultiThread) gerado com sucesso (" + Duration.between(start9, end9).toMillis() + " ms)");
                    break;
                case 10:
                    Instant start10 = Instant.now();
                    try {
                        handler3.processFullFillsMultiThread(inputPath, fulfillPath, 4);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Instant end10 = Instant.now();
                    System.out.println("Arquivo FulFill.txt (MultiThread) gerado com sucesso (" + Duration.between(start10, end10).toMillis() + " ms)");
                    break;
                case 0:
                    System.out.println("Encerrando aplicação.");
                    scanner.close();
                    System.exit(0); // Garante o encerramento da aplicação ao rodar via jar
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
}
