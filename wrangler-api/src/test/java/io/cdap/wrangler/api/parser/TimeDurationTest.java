package io.cdap.wrangler.api.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeDurationTest {

    @Test
    public void testTimeDurationParsing() {
        assertEquals(5000000, TimeDuration.parse("5ms").getNanos());
        assertEquals(2100000000, TimeDuration.parse("2.1s").getNanos());
        assertEquals(0, TimeDuration.parse("0s").getNanos());
    }
}
