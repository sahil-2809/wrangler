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
public class TimeDuration implements Token {

    private long value;

    public TimeDuration(long value) {
        this.value = value;
    }

    @Override
    public Long value() {
       return  this.value;
    }

    @Override
    public TokenType type() {
        return null;
    }

    @Override
    public JsonElement toJson() {
        return null;
    }

    public long getNanos() {
        return this.value;
    }

    public static TimeDuration parse(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Time duration string cannot be null or empty");
        }

        String trimmed = value.trim().toLowerCase();
        double number;
        long multiplier; // convert to nanoseconds

        if (trimmed.endsWith("ns")) {
            number = Double.parseDouble(trimmed.replace("ns", ""));
            multiplier = 1L;
        } else if (trimmed.endsWith("ms")) {
            number = Double.parseDouble(trimmed.replace("ms", ""));
            multiplier = 1_000_000L;
        } else if (trimmed.endsWith("s")) {
            number = Double.parseDouble(trimmed.replace("s", ""));
            multiplier = 1_000_000_000L;
        } else if (trimmed.endsWith("min")) {
            number = Double.parseDouble(trimmed.replace("min", ""));
            multiplier = 60L * 1_000_000_000L;
        } else if (trimmed.endsWith("h")) {
            number = Double.parseDouble(trimmed.replace("h", ""));
            multiplier = 3600L * 1_000_000_000L;
        } else {
            throw new IllegalArgumentException("Invalid time unit in: " + value);
        }

        long nanos = (long) (number * multiplier);
        return new TimeDuration(nanos);
    }

}
