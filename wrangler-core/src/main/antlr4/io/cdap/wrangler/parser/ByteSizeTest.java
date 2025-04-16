package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.ByteSize;
import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTest {
    @Test
    public void testValidByteSizes() {
        Assert.assertEquals(10 * 1024, new ByteSize("10KB").getBytes());
        Assert.assertEquals((long) (1.5 * 1024 * 1024), new ByteSize("1.5MB").getBytes());
        Assert.assertEquals(1024, new ByteSize("1kb").getBytes());
        Assert.assertEquals(100, new ByteSize("100B").getBytes());
        Assert.assertEquals(100, new ByteSize("100").getBytes()); // No unit
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteSize() {
        new ByteSize("10XB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyByteSize() {
        new ByteSize("");
    }
    @Test
    public void testAggregateStatsRecipe() throws Exception {
        String recipe = "aggregate-stats :size :time total_size total_time mb s";
        RecipeParser parser = new RecipeParser(recipe);
        List<Directive> directives = parser.parse();
        Assert.assertEquals(1, directives.size());
        // Optional: Verify directive name or arguments
    }
}