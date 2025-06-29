package br.com.fix.processor.handler;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public class FixMessageHandler {

    private static final String SOH = "\u0001";

    public void processFixFile(String inputPath, String outputCsvPath) throws IOException {
        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputPath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputCsvPath))
        ) {
            // Cabeçalho CSV
            writer.write("Horario;Conta;Instrumento;Lado;Qtd Ordem;Qtd Execucao Atual;Qtd Executada Acumulada;Preco Executado;Notional Ordem;Notional Exec Atual;Notional Exec Acumulada;Entering Trader");
            writer.newLine();

            String line;
            while ((line = reader.readLine()) != null) {
                Map<String, String> tags = parseFixLine(line);

                // Campos essenciais
                String horario = tags.get("60");
                String conta = tags.get("1");
                String instrumento = tags.get("55");
                String lado = tags.get("54");
                BigDecimal orderQty = new BigDecimal(tags.getOrDefault("38", "0"));
                BigDecimal lastQty = new BigDecimal(tags.getOrDefault("32", "0"));
                BigDecimal cumQty = new BigDecimal(tags.getOrDefault("14", "0"));
                BigDecimal avgPx = new BigDecimal(tags.getOrDefault("6", "0"));
                String enteringTrader = tags.getOrDefault("448", "UNKNOWN");

                // Cálculos
                BigDecimal notionalOrdem = orderQty.multiply(avgPx);
                BigDecimal notionalExecAtual = lastQty.multiply(avgPx);
                BigDecimal notionalExecAcumulada = cumQty.multiply(avgPx);

                // Linha CSV
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

    private Map<String, String> parseFixLine(String line) {
        Map<String, String> map = new HashMap<>();
        String[] parts = line.split(SOH);
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
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
            Map<String, String> tags = parseFixLine(line);

            String ordStatus = tags.get("39"); // 2 = Filled
            String execType = tags.get("150"); // F = Trade

            if (!"2".equals(ordStatus) || !"F".equals(execType)) {
                continue; // só queremos execuções totais
            }

            // Pega campos para cálculo
            BigDecimal orderQty = new BigDecimal(tags.getOrDefault("38", "0"));
            BigDecimal avgPx = new BigDecimal(tags.getOrDefault("6", "0"));
            String enteringTrader = tags.getOrDefault("448", "UNKNOWN");

            BigDecimal notional = orderQty.multiply(avgPx);

            // Adiciona campos novos
            tags.put("1010", notional.toPlainString());
            tags.put("1011", enteringTrader);

            // Recria a linha FIX com os campos
            StringBuilder fixLine = new StringBuilder();
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                fixLine.append(entry.getKey()).append("=").append(entry.getValue()).append(SOH);
            }

            writer.write(fixLine.toString());
            writer.newLine();
            }
        }
    }

}
