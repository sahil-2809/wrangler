package io.cdap.directives.aggregation;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveContext;
import io.cdap.wrangler.api.Row;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ByteSizeAndTimeDurationDirectiveTest {

    @Test
    public void testTotalAggregation() throws Exception {
        // Prepare input data
        List<Row> rows = new ArrayList<>();
        rows.add(new Row("sourceByteSize", "1MB").add("sourceTimeDuration", "2s").add("sizeUnit","kb").add("timeUnit","ms").add("aggType","average"));
        rows.add(new Row("sourceByteSize", "2MB").add("sourceTimeDuration", "3s").add("sizeUnit","kb").add("timeUnit","ns").add("aggType","average"));
        rows.add(new Row("sourceByteSize", "512KB").add("sourceTimeDuration", "1500ms").add("sizeUnit","mb").add("timeUnit","ns").add("aggType","total"));

        // Define recipe
        String[] recipe = new String[]{
                "aggregate-byte-time:sourceByteSize :sourceTimeDuration :targetByteSize :targetTimeDuration :sizeUnit :timeUnit :aggType"
        };

        // Execute recipe
        List<Row> results = TestingRig.execute(recipe, rows);


        double expectedTotalSizeMB =1048576.0;
        double expectedTotalTimeSec =2.0E9;

        // Assertions
        assertEquals(3, results.size());
        Row result = results.get(0);

        assertNotEquals(result.find("targetByteSize"), -1);
        assertNotEquals(result.find("targetTimeDuration"),-1);

        double actualSize = (double) result.getValue("targetByteSize");
        double actualTime = (double) result.getValue("targetTimeDuration");

        assertEquals(expectedTotalSizeMB, actualSize, 0.001);
        assertEquals(expectedTotalTimeSec, actualTime,0.001);}

}
