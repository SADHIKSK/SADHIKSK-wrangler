package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;

public class ByteSize implements Token {
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

    private final long bytes;
    private final String raw;

    public ByteSize(String value) {
        this.raw = value;
        this.bytes = parseBytes(value);
    }

    private long parseBytes(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid byte size: " + value);
        }
        String num = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toLowerCase();
        if (num.isEmpty()) {
            throw new IllegalArgumentException("No number found in: " + value);
        }
        double number = Double.parseDouble(num);
        switch (unit) {
            case "kb": return (long) (number * 1024);
            case "mb": return (long) (number * 1024 * 1024);
            case "gb": return (long) (number * 1024 * 1024 * 1024);
            case "tb": return (long) (number * 1024 * 1024 * 1024 * 1024);
            case "b":
            case "": return (long) number; // Assume bytes if no unit
            default: throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public String getRaw() {
        return raw;
    }
}