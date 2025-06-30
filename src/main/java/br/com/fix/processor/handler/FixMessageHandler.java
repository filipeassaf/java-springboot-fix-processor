package br.com.fix.processor.handler;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Paths;
import quickfix.Message;
import quickfix.InvalidMessage;

@Component
public class FixMessageHandler {

    private static final String SOH = "\u0001";

    public void processFixFile(String inputPath, String outputCsvPath) throws IOException {
        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputPath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsvPath))
        ) {
            writer.write("Horario;Conta;Instrumento;Lado;Qtd Ordem;Qtd Execucao Atual;Qtd Executada Acumulada;Preco Executado;Notional Ordem;Notional Exec Atual;Notional Exec Acumulada;Entering Trader");
            writer.newLine();
            String line;
            while ((line = reader.readLine()) != null) {
                Map<String, String> tags = parseFixLineQuickFix(line);
                String horario = tags.get("60");
                String conta = tags.get("1");
                String instrumento = tags.get("55");
                String lado = tags.get("54");
                BigDecimal orderQty = new BigDecimal(tags.getOrDefault("38", "0"));
                BigDecimal lastQty = new BigDecimal(tags.getOrDefault("32", "0"));
                BigDecimal cumQty = new BigDecimal(tags.getOrDefault("14", "0"));
                BigDecimal avgPx = new BigDecimal(tags.getOrDefault("6", "0"));
                String enteringTrader = tags.getOrDefault("448", "UNKNOWN");
                BigDecimal notionalOrdem = orderQty.multiply(avgPx);
                BigDecimal notionalExecAtual = lastQty.multiply(avgPx);
                BigDecimal notionalExecAcumulada = cumQty.multiply(avgPx);
                String csvLine = String.join(";",
                        horario, conta, instrumento, lado,
                        orderQty.toPlainString(),
                        lastQty.toPlainString(),
                        cumQty.toPlainString(),
                        avgPx.toPlainString(),
                        notionalOrdem.toPlainString(),
                        notionalExecAtual.toPlainString(),
                        notionalExecAcumulada.toPlainString(),
                        enteringTrader
                );
                writer.write(csvLine);
                writer.newLine();
            }
        }
    }

    private Map<String, String> parseFixLineQuickFix(String line) {
        Map<String, String> map = new HashMap<>();
        try {
            Message msg = new Message(line, null, true);
            for (Iterator<?> it = msg.iterator(); it.hasNext(); ) {
                quickfix.Field<?> field = (quickfix.Field<?>) it.next();
                map.put(Integer.toString(field.getTag()), field.getObject().toString());
            }
        } catch (InvalidMessage e) {
            String[] parts = line.split(SOH);
            for (String part : parts) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2) {
                    map.put(kv[0], kv[1]);
                }
            }
        }
        return map;
    }

    public void processFullFills(String inputPath, String outputPath) throws IOException {
        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputPath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                Map<String, String> tags = parseFixLineQuickFix(line);
                String ordStatus = tags.get("39");
                String execType = tags.get("150");
                if (!"2".equals(ordStatus) || !"F".equals(execType)) {
                    continue;
                }
                BigDecimal orderQty = new BigDecimal(tags.getOrDefault("38", "0"));
                BigDecimal avgPx = new BigDecimal(tags.getOrDefault("6", "0"));
                String enteringTrader = tags.getOrDefault("448", "UNKNOWN");
                BigDecimal notional = orderQty.multiply(avgPx);
                List<String> originalFields = new ArrayList<>();
                String[] parts = line.split(SOH);
                Set<String> alreadyAdded = new HashSet<>();
                for (String part : parts) {
                    if (part.isEmpty()) continue;
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2) {
                        originalFields.add(part);
                        alreadyAdded.add(kv[0]);
                    }
                }
                originalFields.add("1010=" + notional.toPlainString());
                originalFields.add("1011=" + enteringTrader);
                StringBuilder fixLine = new StringBuilder();
                for (String field : originalFields) {
                    fixLine.append(field).append(SOH);
                }
                writer.write(fixLine.toString());
                writer.newLine();
            }
        }
    }

    public void processFixFileMultiThread(String inputPath, String outputCsvPath, int numThreads) throws IOException, InterruptedException {
        List<String> allLines = Files.readAllLines(Paths.get(inputPath));
        int total = allLines.size();
        int chunk = total / numThreads;
        int remainder = total % numThreads;
        Thread[] threads = new Thread[numThreads];
        String[] tempFiles = new String[numThreads];
        for (int t = 0; t < numThreads; t++) {
            int start = t * chunk + Math.min(t, remainder);
            int end = start + chunk + (t < remainder ? 1 : 0);
            String tempFile = outputCsvPath + ".tmp." + t;
            tempFiles[t] = tempFile;
            final int threadStart = start;
            final int threadEnd = end;
            final int threadId = t;
            threads[t] = new Thread(() -> {
                try {
                    processFixFileRangeLines(allLines, tempFile, threadStart, threadEnd, threadId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            threads[t].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsvPath))) {
            // Escreve o cabeçalho antes de adicionar os dados das threads
            writer.write("Horario;Conta;Instrumento;Lado;Qtd Ordem;Qtd Execucao Atual;Qtd Executada Acumulada;Preco Executado;Notional Ordem;Notional Exec Atual;Notional Exec Acumulada;Entering Trader");
            writer.newLine();
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

    private void processFixFileRangeLines(List<String> allLines, String outputCsvPath, int start, int end, int threadId) throws IOException {
        List<String> linesToWrite = new ArrayList<>();
        for (int i = start; i < end && i < allLines.size(); i++) {
            String line = allLines.get(i);
            Map<String, String> tags = parseFixLineQuickFix(line);
            String horario = tags.get("60");
            String conta = tags.get("1");
            String instrumento = tags.get("55");
            String lado = tags.get("54");
            BigDecimal orderQty = new BigDecimal(tags.getOrDefault("38", "0"));
            BigDecimal lastQty = new BigDecimal(tags.getOrDefault("32", "0"));
            BigDecimal cumQty = new BigDecimal(tags.getOrDefault("14", "0"));
            BigDecimal avgPx = new BigDecimal(tags.getOrDefault("6", "0"));
            String enteringTrader = tags.getOrDefault("448", "UNKNOWN");
            BigDecimal notionalOrdem = orderQty.multiply(avgPx);
            BigDecimal notionalExecAtual = lastQty.multiply(avgPx);
            BigDecimal notionalExecAcumulada = cumQty.multiply(avgPx);
            String csvLine = String.join(";",
                    horario, conta, instrumento, lado,
                    orderQty.toPlainString(),
                    lastQty.toPlainString(),
                    cumQty.toPlainString(),
                    avgPx.toPlainString(),
                    notionalOrdem.toPlainString(),
                    notionalExecAtual.toPlainString(),
                    notionalExecAcumulada.toPlainString(),
                    enteringTrader
            );
            linesToWrite.add(csvLine);
        }
        System.out.println("[" + Thread.currentThread().getName() + "] Gerando AllMsgs.csv: " + linesToWrite.size() + " linhas processadas.");
        synchronized (FixMessageHandler.class) {
            if (start == 0) {
                Files.write(Paths.get(outputCsvPath), new byte[0]);
            }
            Files.write(Paths.get(outputCsvPath), linesToWrite, java.nio.file.StandardOpenOption.APPEND, java.nio.file.StandardOpenOption.CREATE);
        }
    }

    public void processFullFillsMultiThread(String inputPath, String outputPath, int numThreads) throws IOException, InterruptedException {
        List<String> allLines = Files.readAllLines(Paths.get(inputPath));
        // Filtra apenas as linhas relevantes antes de dividir entre as threads
        List<String> filteredLines = new ArrayList<>();
        for (String line : allLines) {
            Map<String, String> tags = parseFixLineQuickFix(line);
            String ordStatus = tags.get("39");
            String execType = tags.get("150");
            if ("2".equals(ordStatus) && "F".equals(execType)) {
                filteredLines.add(line);
            }
        }
        int total = filteredLines.size();
        int threadsToUse = Math.min(numThreads, total);
        if (threadsToUse == 0) threadsToUse = 1;
        int chunk = total / threadsToUse;
        int remainder = total % threadsToUse;
        Thread[] threads = new Thread[threadsToUse];
        String[] tempFiles = new String[threadsToUse];
        for (int t = 0; t < threadsToUse; t++) {
            int start = t * chunk + Math.min(t, remainder);
            int end = start + chunk + (t < remainder ? 1 : 0);
            String tempFile = outputPath + ".tmp." + t;
            tempFiles[t] = tempFile;
            final int threadStart = start;
            final int threadEnd = end;
            final int threadId = t;
            threads[t] = new Thread(() -> {
                try {
                    processFullFillsRangeLines(filteredLines, tempFile, threadStart, threadEnd, threadId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            threads[t].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
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

    private void processFullFillsRangeLines(List<String> allLines, String outputPath, int start, int end, int threadId) throws IOException {
        List<String> linesToWrite = new ArrayList<>();
        for (int i = start; i < end && i < allLines.size(); i++) {
            String line = allLines.get(i);
            Map<String, String> tags = parseFixLineQuickFix(line);
            String ordStatus = tags.get("39");
            String execType = tags.get("150");
            if (!"2".equals(ordStatus) || !"F".equals(execType)) {
                continue;
            }
            BigDecimal orderQty = new BigDecimal(tags.getOrDefault("38", "0"));
            BigDecimal avgPx = new BigDecimal(tags.getOrDefault("6", "0"));
            String enteringTrader = tags.getOrDefault("448", "UNKNOWN");
            BigDecimal notional = orderQty.multiply(avgPx);
            List<String> originalFields = new ArrayList<>();
            String[] parts = line.split(SOH);
            Set<String> alreadyAdded = new HashSet<>();
            for (String part : parts) {
                if (part.isEmpty()) continue;
                String[] kv = part.split("=", 2);
                if (kv.length == 2) {
                    originalFields.add(part);
                    alreadyAdded.add(kv[0]);
                }
            }
            originalFields.add("1010=" + notional.toPlainString());
            originalFields.add("1011=" + enteringTrader);
            StringBuilder fixLine = new StringBuilder();
            for (String field : originalFields) {
                fixLine.append(field).append(SOH);
            }
            linesToWrite.add(fixLine.toString());
        }
        System.out.println("[" + Thread.currentThread().getName() + "] Gerando FulFill.txt: " + linesToWrite.size() + " linhas processadas.");
        synchronized (FixMessageHandler.class) {
            if (start == 0) {
                Files.write(Paths.get(outputPath), new byte[0]);
            }
            Files.write(Paths.get(outputPath), linesToWrite, java.nio.file.StandardOpenOption.APPEND, java.nio.file.StandardOpenOption.CREATE);
        }
    }
}
