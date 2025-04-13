package io.cdap.wrangler.api.parser;

/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import com.google.gson.JsonElement;
import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 *
 */
@PublicEvolving
public class ByteSize implements Token {

    private long value;

    public ByteSize(long value) {
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
