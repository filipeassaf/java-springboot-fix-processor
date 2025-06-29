package br.com.fix.processor.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FixMessageGenerator {

    private static final String SOH = "\u0001";

    @Value("${generator.total-messages}")
    private int totalMessages;
    @Value("${generator.total-full}")
    private int totalFull;
    @Value("${generator.contas}")
    private String contasStr;
    @Value("${generator.instrumentos}")
    private String instrumentosStr;

    private String[] CONTAS;
    private String[] INSTRUMENTOS;

    private final Set<String> orderIds = new HashSet<>();
    private final Set<String> clientOrderIds = new HashSet<>();
    private final Random random = new Random();

    // Inicializa arrays no construtor
    public FixMessageGenerator() {}

    @org.springframework.beans.factory.annotation.Autowired
    private void initArrays() {
        CONTAS = contasStr.split(",");
        INSTRUMENTOS = instrumentosStr.split(",");
    }

    public void generateFixMessages(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < totalMessages; i++) {
                boolean isFull = i < totalFull;
                String fixMessage = generateSingleFixMessage(isFull);
                writer.write(fixMessage);
                writer.newLine();
            }
        }
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public void generateFixMessagesRange(String filePath, int start, int end, int threadId) throws IOException {
        List<String> allLines;
        synchronized (FixMessageGenerator.class) {
            if (start == 0) {
                // Limpa o arquivo apenas na primeira thread
                Files.write(Paths.get(filePath), new byte[0]);
            }
        }
        // Cada thread gera apenas seu bloco
        List<String> lines = new ArrayList<>();
        for (int i = start; i < end; i++) {
            boolean isFull = i < totalFull;
            String fixMessage = generateSingleFixMessage(isFull);
            lines.add(fixMessage);
        }
        synchronized (FixMessageGenerator.class) {
            Files.write(Paths.get(filePath), lines, java.nio.file.StandardOpenOption.APPEND, java.nio.file.StandardOpenOption.CREATE);
        }
    }

    private String generateSingleFixMessage(boolean isFull) {
        String orderId = generateUniqueId(orderIds);
        String clOrdId = generateUniqueId(clientOrderIds);
        String conta = getRandom(CONTAS);
        String instrumento = getRandom(INSTRUMENTOS);
        String lado = random.nextBoolean() ? "1" : "2"; // 1=Buy, 2=Sell
        int orderQty = random.nextInt(9000) + 1000;
        int lastQty = isFull ? orderQty : random.nextInt(orderQty);
        int cumQty = lastQty; // Para simplificar, igual ao lastQty
        String enteringTrader = "TRADER" + random.nextInt(1000);
        String avgPx = String.format(Locale.US, "%.2f", 10 + (random.nextDouble() * 20));
        String lastPx = avgPx;
        String timestamp = java.time.LocalDateTime.now().toString();
        String execType = "F"; // Trade
        String ordStatus = isFull ? "2" : "1"; // 2 = Filled, 1 = Partially Filled

        return String.join(SOH,
            "8=FIX.4.4",
            "35=8",
            "49=SISTEMA",
            "56=DROPCOPY",
            "52=" + timestamp,
            "1=" + conta,
            "11=" + clOrdId,
            "37=" + orderId,
            "55=" + instrumento,
            "54=" + lado,
            "38=" + orderQty,
            "32=" + lastQty,
            "14=" + cumQty,
            "6=" + avgPx,
            "31=" + lastPx,
            "150=" + execType,
            "39=" + ordStatus,
            "60=" + timestamp,
            "452=36" + SOH + "448=" + enteringTrader,  // Entering Trader
            "10=000" // Placeholder para checksum
        );
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
