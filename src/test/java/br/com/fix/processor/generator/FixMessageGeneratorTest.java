package br.com.fix.processor.generator;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FixMessageGeneratorTest {
    private FixMessageGenerator generator;
    private File tempOutput;

    @BeforeEach
    void setUp() throws IOException {
        generator = new FixMessageGenerator();
        // Set valores via reflexão já que não há setters
        setField(generator, "totalMessages", 2);
        setField(generator, "totalFull", 1);
        setField(generator, "contasStr", "ACC1,ACC2");
        setField(generator, "instrumentosStr", "PETR4,VALE3");
        generator.initArraysForTest();
        tempOutput = File.createTempFile("fixgen_output", ".txt");
    }

    @AfterEach
    void tearDown() {
        tempOutput.delete();
    }

    @Test
    void testGenerateFixMessages() throws IOException {
        generator.generateFixMessages(tempOutput.getAbsolutePath());
        List<String> lines = Files.readAllLines(tempOutput.toPath());
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("8=FIX.4.4"));
        assertTrue(lines.get(1).contains("8=FIX.4.4"));
    }

    // Utilitário para setar campos privados
    private void setField(Object obj, String field, Object value) {
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
