package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;

public class ByteSize implements Token{

    private long value;

    public ByteSize(long value){
        this.value = value;
    }
    @Override
    public Long value() {
        return this.value;
    }

    @Override
    public TokenType type() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    public long getBytes() {
        return  this.value;
    }

    public static ByteSize parse(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Byte size string cannot be null or empty");
        }

        String trimmed = value.trim().toLowerCase();

        double number;
        long multiplier;

        if (trimmed.endsWith("tb")) {
            number = Double.parseDouble(trimmed.replace("tb", ""));
            multiplier = 1024L * 1024 * 1024 * 1024;
        } else if (trimmed.endsWith("gb")) {
            number = Double.parseDouble(trimmed.replace("gb", ""));
            multiplier = 1024L * 1024 * 1024;
        } else if (trimmed.endsWith("mb")) {
            number = Double.parseDouble(trimmed.replace("mb", ""));
            multiplier = 1024L * 1024;
        } else if (trimmed.endsWith("kb")) {
            number = Double.parseDouble(trimmed.replace("kb", ""));
            multiplier = 1024L;
        } else if (trimmed.endsWith("b")) {
            number = Double.parseDouble(trimmed.replace("b", ""));
            multiplier = 1L;
        } else {
            throw new IllegalArgumentException("Invalid byte size unit in: " + value);
        }

        long bytes = (long) (number * multiplier);
        return new ByteSize(bytes);
    }

}
