package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

public class TimeDurationTest {
    @Test
    public void testValidTimeDurations() {
        Assert.assertEquals(100 * 1_000_000, new TimeDuration("100ms").getNanos());
        Assert.assertEquals((long) (2.1 * 1_000_000_000), new TimeDuration("2.1s").getNanos());
        Assert.assertEquals(60 * 1_000_000_000, new TimeDuration("1m").getNanos());
        Assert.assertEquals(24 * 3600 * 1_000_000_000, new TimeDuration("1d").getNanos());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeDuration() {
        new TimeDuration("10x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTimeDuration() {
        new TimeDuration("");
    }
}