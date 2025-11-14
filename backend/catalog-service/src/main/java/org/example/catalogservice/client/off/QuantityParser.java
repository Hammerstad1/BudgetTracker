package org.example.catalogservice.client.off;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import static java.text.Normalizer.normalize;


public final class QuantityParser {

    public record QtyParsed(
            String raw,
            Integer packCount,
            BigDecimal packSizeValue,
            String packSizeUnit,
            BigDecimal totalValue,
            String totalUnit
    ) {}

    private record Norm(BigDecimal value, String unit) {}

    private QuantityParser() {}

    public static QtyParsed parse (String raw0) {
        if (raw0 == null || raw0.isBlank()) return null;

        String raw = raw0.trim().toLowerCase()
                .replace(",", ".")
                .replace("x", "x");

        var multipack = Pattern.compile("(\\d+)\\s*x\\s*(\\d+(?:\\.\\d+)?)\\s*([a-z])+").matcher(raw);
        var simple = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*([a-z]+)$")
                .matcher(raw.replaceAll("\\s+", " "));

        BigDecimal totalValue = null;
        String totalUnit = null;
        Integer packCount = null;
        BigDecimal packSizeValue = null;
        String packSizeUnit = null;

        if (multipack.find()) {
            packCount = Integer.parseInt(multipack.group(1));
            packSizeValue = new BigDecimal(multipack.group(2));
            packSizeUnit = multipack.group(3);
            Norm norm = normalize(packSizeValue, packSizeUnit);
            totalValue = norm.value().multiply(BigDecimal.valueOf(packCount));
            totalUnit = norm.unit();
        } else if (simple.find()) {
            BigDecimal v = new BigDecimal(simple.group(1));
            String u = simple.group(2);
            Norm norm = normalize(v, u);
            totalValue = norm.value();
            totalUnit = norm.unit();
        }

        return new QtyParsed(raw0, packCount, packSizeValue, packSizeUnit, totalValue, totalUnit);
    }

    private static Norm normalize(BigDecimal v, String u0) {
        String u = u0.trim();
        switch (u) {
            case "ml": return new Norm(v, "ml");
            case "l": return new Norm(v.multiply(BigDecimal.valueOf(1000)), "ml");
            case "cl": return new Norm(v.multiply(BigDecimal.valueOf(10)), "ml");
            case "g": return new Norm (v, "g");
            case "kg": return new Norm (v.multiply(BigDecimal.valueOf(1000)), "g");
            default: return new Norm(v, u);
        }
    }
}
