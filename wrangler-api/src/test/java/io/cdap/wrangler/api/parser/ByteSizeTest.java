package io.cdap.wrangler.api.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteSizeTest {

    @Test
    public void testByteSizeParsing() {
        assertEquals(10240, ByteSize.parse("10kb").getBytes());
        assertEquals(1572864, ByteSize.parse("1.5MB").getBytes());
        assertEquals(0, ByteSize.parse("0B").getBytes());
    }
}
