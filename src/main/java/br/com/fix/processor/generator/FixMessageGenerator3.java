package br.com.fix.processor.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import quickfix.Message;

@Component
public class FixMessageGenerator3 {
    @Value("${generator.total-messages}")
    private int totalMessages;
    @Value("${generator.total-full}")
    private int totalFull;
    @Value("${generator.contas}")
    private String contasStr;
    @Value("${generator.instrumentos}")
    private String instrumentosStr;

    private String[] contas;
    private String[] instrumentos;
    private final Set<String> orderIds = new HashSet<>();
    private final Set<String> clientOrderIds = new HashSet<>();
    private final Random random = new Random();

    public FixMessageGenerator3() {}

    private void ensureArraysInitialized() {
        if (contas == null || instrumentos == null) {
            synchronized (this) {
                if (contas == null || instrumentos == null) {
                    contas = contasStr.split(",");
                    instrumentos = instrumentosStr.split(",");
                }
            }
        }
    }

    public void generateFixMessagesRange(String filePath, int start, int end) throws IOException {
        ensureArraysInitialized();
        synchronized (FixMessageGenerator3.class) {
            if (start == 0) {
                Files.write(Paths.get(filePath), new byte[0]);
            }
        }
        List<String> lines = new ArrayList<>();
        for (int i = start; i < end; i++) {
            boolean isFull = i < totalFull;
            String fixMessage = generateSingleFixMessage(isFull);
            lines.add(fixMessage);
        }
        // Loga no terminal o nome da thread, ação e quantidade de linhas processadas
        System.out.println("[" + Thread.currentThread().getName() + "] Gerando input_fix.txt: " + lines.size() + " linhas processadas.");
        synchronized (FixMessageGenerator3.class) {
            Files.write(Paths.get(filePath), lines, java.nio.file.StandardOpenOption.APPEND, java.nio.file.StandardOpenOption.CREATE);
        }
    }

    public void generateFixMessagesMultiThread(String filePath, int numThreads) throws IOException, InterruptedException {
        int total = totalMessages;
        int chunk = total / numThreads;
        int remainder = total % numThreads;
        Thread[] threads = new Thread[numThreads];
        // Cada thread escreve em um arquivo temporário
        String[] tempFiles = new String[numThreads];
        for (int t = 0; t < numThreads; t++) {
            int start = t * chunk + Math.min(t, remainder);
            int end = start + chunk + (t < remainder ? 1 : 0);
            String tempFile = filePath + ".tmp." + t;
            tempFiles[t] = tempFile;
            threads[t] = new Thread(() -> {
                try {
                    generateFixMessagesRange(tempFile, start, end);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            threads[t].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        // Junta os arquivos temporários no arquivo final
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String temp : tempFiles) {
                List<String> lines = Files.readAllLines(Paths.get(temp));
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
                Files.delete(Paths.get(temp));
            }
        }
    }

    private String generateSingleFixMessage(boolean isFull) {
        ensureArraysInitialized();
        String orderId = generateUniqueId(orderIds);
        String clOrdId = generateUniqueId(clientOrderIds);
        String conta = getRandom(contas);
        String instrumento = getRandom(instrumentos);
        String lado = random.nextBoolean() ? "1" : "2"; // 1=Buy, 2=Sell
        int orderQty = random.nextInt(9000) + 1000;
        int lastQty = isFull ? orderQty : random.nextInt(orderQty);
        int cumQty = lastQty; // Para simplificar, igual ao lastQty
        String enteringTrader = "TRADER" + random.nextInt(1000);
        String avgPx = String.format(Locale.US, "%.2f", 10 + (random.nextDouble() * 20));
        String lastPx = avgPx;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String execType = "F"; // Trade
        String ordStatus = isFull ? "2" : "1"; // 2 = Filled, 1 = Partially Filled

        // Monta a mensagem manualmente usando QuickFIX/J para garantir ordem e campos
        Message report = new Message();
        report.getHeader().setField(new quickfix.field.BeginString("FIX.4.4")); // 8
        report.getHeader().setField(new quickfix.field.MsgType("8")); // 35
        report.getHeader().setField(new quickfix.field.SenderCompID("SISTEMA")); // 49
        report.getHeader().setField(new quickfix.field.TargetCompID("DROPCOPY")); // 56
        report.getHeader().setField(new quickfix.field.SendingTime(now)); // 52
        report.setField(new quickfix.field.Account(conta)); // 1
        report.setField(new quickfix.field.ClOrdID(clOrdId)); // 11
        report.setField(new quickfix.field.OrderID(orderId)); // 37
        report.setField(new quickfix.field.Symbol(instrumento)); // 55
        report.setField(new quickfix.field.Side(lado.charAt(0))); // 54
        report.setField(new quickfix.field.OrderQty(orderQty)); // 38
        report.setField(new quickfix.field.LastQty(lastQty)); // 32
        report.setField(new quickfix.field.CumQty(cumQty)); // 14
        report.setField(new quickfix.field.AvgPx(Double.parseDouble(avgPx))); // 6
        report.setField(new quickfix.field.LastPx(Double.parseDouble(lastPx))); // 31
        report.setField(new quickfix.field.ExecType(execType.charAt(0))); // 150
        report.setField(new quickfix.field.OrdStatus(ordStatus.charAt(0))); // 39
        report.setField(new quickfix.field.TransactTime(now)); // 60
        // Parties (452=36, 448=TRADERxxx)
        quickfix.Group partyGroup = new quickfix.Group(453, 448); // 453=NoPartyIDs, 448=PartyID
        partyGroup.setInt(452, 36); // PartyRole
        partyGroup.setString(448, enteringTrader); // PartyID
        report.addGroup(partyGroup);
        // O QuickFIX/J calcula o checksum automaticamente ao serializar
        return report.toString();
    }

    private String getRandom(String[] array) {
        return array[random.nextInt(array.length)];
    }

    private String generateUniqueId(Set<String> existingIds) {
        String id;
        do {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        } while (!existingIds.add(id));
        return id;
    }

}
