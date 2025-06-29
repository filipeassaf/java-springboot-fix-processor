package br.com.fix.processor;

import java.io.IOException;
import java.util.Scanner;
import java.time.Duration;
import java.time.Instant;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import br.com.fix.processor.generator.FixMessageGenerator;
import br.com.fix.processor.handler.FixMessageHandler;

@SpringBootApplication
public class ProcessorApplication {

	private static final String INPUT_PATH = "data/input_fix.txt";
    private static final String CSV_PATH = "data/AllMsgs.csv";
    private static final String FULFILL_PATH = "data/FulFill.txt";

	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
        FixMessageGenerator generator = new FixMessageGenerator();
        FixMessageHandler handler = new FixMessageHandler();

		System.out.println("\nBem-vindo ao Fix Processor");
        System.out.println("Escolha uma opção:");
        System.out.println("1 - Gerar massa de dados FIX (Parte 1)");
        System.out.println("2 - Gerar AllMsgs.csv (Parte 2.1)");
        System.out.println("3 - Gerar FulFill.txt (Parte 2.2)");
        System.out.println("4 - Executar tudo (1 > 2.1 > 2.2)");
        System.out.println("0 - Sair");

        while (true) {

            System.out.print("Digite a opção: ");

            int option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1:
                    Instant start1 = Instant.now();
                    generator.generateFixMessages(INPUT_PATH);
                    Instant end1 = Instant.now();
                    System.out.println("Arquivo input_fix.txt gerado com sucesso (" + Duration.between(start1, end1).toMillis() + " ms)");
                    break;
                case 2:
                    Instant start2 = Instant.now();
                    handler.processFixFile(INPUT_PATH, CSV_PATH);
                    Instant end2 = Instant.now();
                    System.out.println("Arquivo AllMsgs.csv gerado com sucesso (" + Duration.between(start2, end2).toMillis() + " ms)");
                    break;
                case 3:
                    Instant start3 = Instant.now();
                    handler.processFullFills(INPUT_PATH, FULFILL_PATH);
                    Instant end3 = Instant.now();
                    System.out.println("Arquivo FulFill.txt gerado com sucesso (" + Duration.between(start3, end3).toMillis() + " ms)");
                    break;
                case 4:
                    Instant start4 = Instant.now();
                    generator.generateFixMessages(INPUT_PATH);
                    handler.processFixFile(INPUT_PATH, CSV_PATH);
                    handler.processFullFills(INPUT_PATH, FULFILL_PATH);
                    Instant end4 = Instant.now();

                    System.out.println("Todos arquivos gerados com sucesso. (" + Duration.between(start4, end4).toMillis() + " ms)");
                    break;
                case 0:
                    System.out.println("Encerrando aplicação.");
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

}
