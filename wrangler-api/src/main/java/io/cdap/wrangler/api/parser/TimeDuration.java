package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;

public class TimeDuration implements Token {
    private final long nanos;
    private final String raw;

    public TimeDuration(String value) {
        this.raw = value;
        this.nanos = parseNanos(value);
    }

    private long parseNanos(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid time duration: " + value);
        }
        String num = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toLowerCase();
        if (num.isEmpty()) {
            throw new IllegalArgumentException("No number found in: " + value);
        }
        double number = Double.parseDouble(num);
        switch (unit) {
            case "ns": return (long) number;
            case "ms": return (long) (number * 1_000_000);
            case "s": return (long) (number * 1_000_000_000);
            case "m": return (long) (number * 60 * 1_000_000_000);
            case "h": return (long) (number * 3600 * 1_000_000_000);
            case "d": return (long) (number * 24 * 3600 * 1_000_000_000);
            default: throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    public long getNanos() {
        return nanos;
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public TokenType type() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    @Override
    public String getRaw() {
        return raw;
    }
}