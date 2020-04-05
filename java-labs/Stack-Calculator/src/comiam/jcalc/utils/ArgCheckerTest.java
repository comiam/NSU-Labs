package comiam.jcalc.utils;

import org.junit.Assert;
import org.junit.Test;

public class ArgCheckerTest
{

    @Test
    public void isNumeric()
    {
        String num = "2430";
        Assert.assertTrue(ArgChecker.isNumeric(num));

        num = "azazaza";
        Assert.assertFalse(ArgChecker.isNumeric(num));
    }

    @Test
    public void containsSpecialSymbols()
    {
        String word = "^aawd&";
        Assert.assertTrue(ArgChecker.containsSpecialSymbols(word));

        word = "ajd%wda";
        Assert.assertTrue(ArgChecker.containsSpecialSymbols(word));

        word = "aawd&";
        Assert.assertTrue(ArgChecker.containsSpecialSymbols(word));

        word = "----_awd";
        Assert.assertTrue(ArgChecker.containsSpecialSymbols(word));

        word = "aawd";
        Assert.assertFalse(ArgChecker.containsSpecialSymbols(word));
    }
}