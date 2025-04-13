/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.aggregation;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ErrorRowException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.ReportErrorAndProceed;
import io.cdap.wrangler.api.Row;



import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;



import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Plugin(type = Directive.TYPE)
@Name(ByteSizeAndTimeDurationDirective.NAME)
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
        builder.define("sizeUnit", TokenType.COLUMN_NAME, "Output unit for size (B, KB, MB, GB).", Optional.TRUE);
        builder.define("timeUnit", TokenType.COLUMN_NAME, "Output unit for time (ms, s, min).", Optional.TRUE);
        builder.define("aggType", TokenType.COLUMN_NAME, "Aggregation type: total or average.", Optional.TRUE);
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
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException,
            ErrorRowException, ReportErrorAndProceed {

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
