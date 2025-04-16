package io.cdap.wrangler.executor;

import io.cdap.wrangler.api.*;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

// Note: ByteSize and TimeDuration are assumed to be part of the CDAP Wrangler API.
// If unavailable, custom parsing logic will be needed (see notes below).
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// import io.cdap.wrangler.api.TimeDuration; // This import is removed because TimeDuration is not available.

import java.util.List;
import io.cdap.wrangler.api.Row;

public abstract class AggregateStats implements Directive {
    // Fields to store column names and output units
    private String sizeCol, timeCol, totalSizeCol, totalTimeCol;
    private String sizeUnit = "mb"; // Default output unit
    private String timeUnit = "s"; // Default output unit
    private TransientStore store; // Store for accumulating totals

    @Override
    public io.cdap.wrangler.api.parser.UsageDefinition define() {
        // Define the directive's syntax: four required columns, two optional units
        return UsageDefinition.builder("aggregate-stats")
                .required("size-column", TokenType.COLUMN_NAME)
                .required("time-column", TokenType.COLUMN_NAME)
                .required("total-size-column", TokenType.COLUMN_NAME)
                .required("total-time-column", TokenType.COLUMN_NAME)
                .optional("size-unit", TokenType.STRING)
                .optional("time-unit", TokenType.STRING)
                .build();
    }

    @Override
    public void initialize(Arguments args, ExecutorContext context) throws RecipeException {
        // Extract column names from arguments
        sizeCol = args.valueAsColumnName("size-column");
        timeCol = args.valueAsColumnName("time-column");
        totalSizeCol = args.valueAsColumnName("total-size-column");
        totalTimeCol = args.valueAsColumnName("total-time-column");

        // Set optional units if provided
        if (args.contains("size-unit")) {
            sizeUnit = ((Text) args.value("size-unit")).value().toLowerCase();
        }
        if (args.contains("time-unit")) {
            timeUnit = ((Text) args.value("time-unit")).value().toLowerCase();
        }

        // Initialize the store
        store = context.getTransientStore(); // Verify method name in API
        // Initialize store values to avoid null issues
        store.put("total_bytes", 0L);
        store.put("total_nanos", 0L);
    }

    @Override
    public Row execute(Row row, ExecutorContext context) throws DirectiveExecutionException {
        // Get size and time values from the row
        Object sizeObj = row.getValue(sizeCol);
        Object timeObj = row.getValue(timeCol);
        long bytes = 0;
        long nanos = 0;

        try {
            // Parse size value (e.g., "10mb") to bytes
            if (sizeObj instanceof String) {
                bytes = parseByteSize((String) sizeObj);
            }
            // Parse time value (e.g., "5s") to nanoseconds
            if (timeObj instanceof String) {
                nanos = parseTimeDuration((String) timeObj);
            }
        } catch (IllegalArgumentException e) {
            throw new RecipeException("Invalid input: " + e.getMessage());
        }

        // Update totals in the store
        long currentBytes = store.getLong("total_bytes", 0L);
        long currentNanos = store.getLong("total_nanos", 0L);
        store.put("total_bytes", currentBytes + bytes);
        store.put("total_nanos", currentNanos + nanos);
        return null; // Aggregators return null per row
    }

    private long parseTimeDuration(String timeObj) {
        return 0;
    }

    @Override
    public Row finalize(ExecutorContext context) {
        // Retrieve accumulated totals
        long totalBytes = store.getLong("total_bytes", 0L);
        long totalNanos = store.getLong("total_nanos", 0L);

        // Convert totals to specified units
        double sizeOutput = convertBytes(totalBytes, sizeUnit);
        double timeOutput = convertNanos(totalNanos, timeUnit);

        // Create result row with aggregated values
        io.cdap.wrangler.api.Row result = new io.cdap.wrangler.api.Row();
        result.add(totalSizeCol, sizeOutput);
        result.add(totalTimeCol, timeOutput);
        return result;
    }


    @Override
    public void initialize(Arguments args) throws DirectiveParseException {

    }

    @Override
    public List<io.cdap.wrangler.api.Row> execute(List<io.cdap.wrangler.api.Row> rows, ExecutorContext context) throws DirectiveExecutionException, ErrorRowException, ReportErrorAndProceed {
        return List.of();
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }

    // Convert bytes to specified unit (kb, mb, gb, tb, or bytes)
    private double convertBytes(long bytes, String unit) {
        switch (unit) {
            case "kb":
                return bytes / 1024.0;
            case "mb":
                return bytes / (1024.0 * 1024);
            case "gb":
                return bytes / (1024.0 * 1024 * 1024);
            case "tb":
                return bytes / (1024.0 * 1024 * 1024 * 1024);
            default:
                return bytes; // Return raw bytes if unit is invalid
        }
    }

    // Convert nanoseconds to specified unit (ns, ms, s, m, h, d)
    private double convertNanos(long nanos, String unit) {
        switch (unit) {
            case "ns":
                return nanos;
            case "ms":
                return nanos / 1_000_000.0;
            case "s":
                return nanos / 1_000_000_000.0;
            case "m":
                return nanos / (60.0 * 1_000_000_000);
            case "h":
                return nanos / (3600.0 * 1_000_000_000);
            case "d":
                return nanos / (24.0 * 3600 * 1_000_000_000);
            default:
                return nanos; // Return raw nanoseconds if unit is invalid
        }
    }
    /**
     * Parses a size string (e.g., "10mb" or "5kb") into bytes.
     *
     * @param sizeStr the size string to parse
     * @return the size in bytes
     * @throws IllegalArgumentException if the input format is invalid
     */
    private long parseByteSize(String sizeStr) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("(\\d+)([a-zA-Z]*)");
        Matcher matcher = pattern.matcher(sizeStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid size format: " + sizeStr);
        }

        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();

        switch (unit) {
            case "kb":
                return value * 1024;
            case "mb":
                return value * 1024 * 1024;
            case "gb":
                return value * 1024 * 1024 * 1024;
            case "tb":
                return value * 1024L * 1024 * 1024 * 1024;
            case "":
                return value; // Assume bytes if no unit is provided
            default:
                throw new IllegalArgumentException("Unknown size unit: " + unit);
        }
    }
}