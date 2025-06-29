package br.com.fix.processor.handler;

import org.junit.jupiter.api.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FixMessageHandlerTest {
    private FixMessageHandler handler;
    private static final String SOH = "\u0001";
    private File tempInput;
    private File tempOutput;

    @BeforeEach
    void setUp() throws IOException {
        handler = new FixMessageHandler();
        tempInput = File.createTempFile("fix_input", ".txt");
        tempOutput = File.createTempFile("fix_output", ".csv");
    }

    @AfterEach
    void tearDown() {
        tempInput.delete();
        tempOutput.delete();
    }

    @Test
    void testProcessFixFile() throws IOException {
        String fixLine = "60=20240629" + SOH + "1=ACC123" + SOH + "55=PETR4" + SOH + "54=1" + SOH + "38=100" + SOH + "32=50" + SOH + "14=50" + SOH + "6=10.5" + SOH + "448=TRADER1" + SOH;
        Files.write(tempInput.toPath(), List.of(fixLine));
        handler.processFixFile(tempInput.getAbsolutePath(), tempOutput.getAbsolutePath());
        List<String> lines = Files.readAllLines(tempOutput.toPath());
        assertEquals(2, lines.size()); // header + 1 line
        assertTrue(lines.get(1).contains("ACC123"));
        assertTrue(lines.get(1).contains("PETR4"));
    }

    @Test
    void testProcessFullFills() throws IOException {
        String fixLine = "39=2" + SOH + "150=F" + SOH + "38=100" + SOH + "6=10.5" + SOH + "448=TRADER2" + SOH;
        Files.write(tempInput.toPath(), List.of(fixLine));
        handler.processFullFills(tempInput.getAbsolutePath(), tempOutput.getAbsolutePath());
        List<String> lines = Files.readAllLines(tempOutput.toPath());
        assertEquals(1, lines.size());
        String result = lines.get(0);
        assertTrue(result.contains("1010=1050")); // 100*10.5
        assertTrue(result.contains("1011=TRADER2"));
    }
}
