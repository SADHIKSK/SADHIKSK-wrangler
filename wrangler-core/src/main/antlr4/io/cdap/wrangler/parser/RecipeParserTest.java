package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.RecipeParser;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;

public class RecipeParserTest {
    @Test
    public void testAggregateStatsRecipe() throws Exception {
        String recipe = "aggregate-stats :size :time total_size total_time mb s";
        RecipeParser parser = new RecipeParser(recipe);
        List<Directive> directives = parser.parse();
        Assert.assertEquals(1, directives.size());
    }
}