package org.example.catalogservice;

import org.example.catalogservice.client.off.QuantityParser;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class QuantityParserTest {

    @Test public void parseSimpleMl() {
        var q = QuantityParser.parse("330ml");
        assertEquals(new BigDecimal("330"), q.totalValue());
        assertEquals("ml", q.totalUnit());
    }

    @Test public void parseClMultipack() {
        var q = QuantityParser.parse("6 x 33 cl");
        assertEquals(new BigDecimal("1980"), q.totalValue());
        assertEquals("ml", q.totalUnit());
    }

    @Test public void parsesKg(){
        var q = QuantityParser.parse("1kg");
        assertEquals(new BigDecimal("1000"), q.totalValue());
        assertEquals("g", q.totalUnit());
    }

}
