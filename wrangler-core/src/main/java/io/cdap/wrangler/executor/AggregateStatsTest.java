package io.cdap.wrangler.executor;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.*;
import org.junit.Assert;
import org.junit.Test;
import java.util.*;

public class AggregateStatsTest {
    @Test
    public void testAggregateStats() throws Exception {
        List<Row> rows = Arrays.asList(
                new Row("size", "10KB", "time", "100ms"),
                new Row("size", "1MB", "time", "2s")
        );

        String[] recipe = {
                "aggregate-stats :size :time total_size_mb total_time_sec mb s"
        };

        List<Row> results = TestingRig.execute(recipe, rows);
        Assert.assertEquals(1, results.size());
        Row result = results.get(0);
        double expectedSize = (10 * 1024 + 1024 * 1024) / (1024.0 * 1024); // MB
        double expectedTime = (100 * 1_000_000 + 2 * 1_000_000_000) / 1_000_000_000.0; // seconds
        Assert.assertEquals(expectedSize, result.getValue("total_size_mb"), 0.001);
        Assert.assertEquals(expectedTime, result.getValue("total_time_sec"), 0.001);
    }

    @Test
    public void testAggregateStatsWithDefaultUnits() throws Exception {
        List<Row> rows = Arrays.asList(
                new Row("size", "10KB", "time", "100ms")
        );

        String[] recipe = {
                "aggregate-stats :size :time total_size_mb total_time_sec"
        };

        List<Row> results = TestingRig.execute(recipe, rows);
        Assert.assertEquals(1, results.size());
        Row result = results.get(0);
        Assert.assertEquals(10 * 1024 / (1024.0 * 1024), result.getValue("total_size_mb"), 0.001);
        Assert.assertEquals(100 * 1_000_000 / 1_000_000_000.0, result.getValue("total_time_sec"), 0.001);
    }
}