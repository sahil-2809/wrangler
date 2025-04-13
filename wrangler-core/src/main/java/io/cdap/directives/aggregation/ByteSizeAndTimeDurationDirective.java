package io.cdap.directives.aggregation;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Plugin(type = Directive.TYPE)
@Name(ByteSizeAndTimeDurationDirective.NAME)
@Categories(categories = { "aggregation"})
@Description("This directive should operate as an aggregate. So you need a store to\n" +
        "accumulate totals.")
public class ByteSizeAndTimeDurationDirective implements Directive {

    public static final String NAME = "aggregate-byte-time";

    private String sourceByteSize;
    private String targetByteSize;
    private String sourceTimeDuration;

    private String targetTimeDuration;
    private String sizeUnit;
    private String timeUnit;
    private String aggType;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
        builder.define("sourceByteSize", TokenType.COLUMN_NAME);
        builder.define("sourceTimeDuration", TokenType.COLUMN_NAME);
        builder.define("targetByteSize", TokenType.COLUMN_NAME);
        builder.define("targetTimeDuration", TokenType.COLUMN_NAME);
        builder.define("sizeUnit", TokenType.COLUMN_NAME, "Output unit for size (B, KB, MB, GB).",Optional.TRUE);
        builder.define("timeUnit", TokenType.COLUMN_NAME, "Output unit for time (ms, s, min).",Optional.TRUE);
        builder.define("aggType",TokenType.COLUMN_NAME, "Aggregation type: total or average.",Optional.TRUE);
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sourceByteSize =  ((ColumnName) args.value("sourceByteSize")).value();
        this.sourceTimeDuration = ((ColumnName) args.value("sourceTimeDuration")).value();

        this.targetByteSize = ((ColumnName)  args.value("targetByteSize")).value();
        this.targetTimeDuration = ((ColumnName)  args.value("targetTimeDuration")).value();
        this.sizeUnit = ((ColumnName)  args.value("sizeUnit")).value();
        this.timeUnit = ((ColumnName)  args.value("timeUnit")).value();
        this.aggType = ((ColumnName)  args.value("aggType")).value();
        if (args.contains("sizeUnit")) {
            sizeUnit = args.value("sizeUnit").toString().toLowerCase();
        }

        if (args.contains("timeUnit")) {
            timeUnit = args.value("timeUnit").toString().toLowerCase();
        }

        if (args.contains("aggType")) {
            aggType = args.value("aggType").toString().toLowerCase();
       }


    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException, ErrorRowException, ReportErrorAndProceed {

        long totalBytes = 0;
        long totalTimeNs = 0;

        for (Row row : rows) {
            String byteVal = row.getValue(sourceByteSize).toString();
            String timeVal = row.getValue(sourceTimeDuration).toString();

            totalBytes +=  ByteSize.parse(byteVal).getBytes();
            totalTimeNs += TimeDuration.parse(timeVal).getNanos();
        }

        long finalBytes = aggType.equals("average") ? totalBytes / rows.size() : totalBytes;
        long finalTime = aggType.equals("average") ? totalTimeNs / rows.size() : totalTimeNs;

        double sizeOutput = convertSize(finalBytes, sizeUnit);
        double timeOutput = convertTime(finalTime, timeUnit);

        Row output = new Row();
        output.add(targetByteSize, sizeOutput);
        output.add(targetTimeDuration, timeOutput);

        return Collections.singletonList(output);
    }

    private double convertSize(long bytes, String unit) {
        switch (unit) {
            case "kb": return bytes / 1024.0;
            case "mb": return bytes / (1024.0 * 1024);
            case "gb": return bytes / (1024.0 * 1024 * 1024);
            default: return bytes;
        }
    }

    private double convertTime(long ns, String unit) {
        switch (unit) {
            case "ms": return TimeUnit.NANOSECONDS.toMillis(ns);
            case "s": return TimeUnit.NANOSECONDS.toSeconds(ns);
            case "min": return TimeUnit.NANOSECONDS.toMinutes(ns);
            default: return ns;
        }
    }

    @Override
    public void destroy() {

    }
}
