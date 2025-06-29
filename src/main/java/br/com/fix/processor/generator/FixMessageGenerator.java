package br.com.fix.processor.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FixMessageGenerator {

    private static final String SOH = "\u0001";
    private static final int TOTAL_MESSAGES = 5000;
    private static final int TOTAL_FULL = 2500;

    private static final String[] CONTAS = {"ACC001", "ACC002", "ACC003", "ACC004", "ACC005",
                                            "ACC006", "ACC007", "ACC008", "ACC009", "ACC010"};

    private static final String[] INSTRUMENTOS = {"PETR4", "VALE3", "ITUB4", "ABEV3", "BBDC4",
                                                  "BBAS3", "WEGE3", "RENT3", "RADL3", "SUZB3"};

    private final Set<String> orderIds = new HashSet<>();
    private final Set<String> clientOrderIds = new HashSet<>();
    private final Random random = new Random();

    public void generateFixMessages(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < TOTAL_MESSAGES; i++) {
                boolean isFull = i < TOTAL_FULL;
                String fixMessage = generateSingleFixMessage(isFull);
                writer.write(fixMessage);
                writer.newLine();
            }
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
