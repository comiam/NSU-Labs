import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class MainTest
{
    @Test
    public void testMain()
    {
        double eps = 0.001;
        double delta = 0.0005;

        //equation coefficients
        double[][] eqSet = {
                {8, -36, 54, -27},
                {8, 12, 6, 1},
                {1, 9, 27, 27},
                {1, 4, -3, -18},
                {1, -2, -16, 32},
                {3, 9, 1, 3},
                {2, -7, 4, -14},
                {-1, -5, 4, 20},
                {3, -3, -0.75, 0.75},
                {1, 5, 3, -9},
                {1, -21, 111, -91}
        };

        //answers to equation
        double[][] ansSet = {
                {1.5},
                {-0.5},
                {-3},
                {2, -3},
                {4, -4, 2},
                {-3},
                {3.5},
                {-5, 2, -2},
                {-0.5, 1, 0.5},
                {1, -3},
                {1, 13, 7},
        };

        System.out.println("Tests with eps=" + eps + " and delta=" + delta);

        boolean ok = true;
        int fallIndex = -1;
        for(int i = 0;i < eqSet.length;i++)
        {
            var sol = Main.getSolution(eps, delta, eqSet[i][0], eqSet[i][1], eqSet[i][2], eqSet[i][3]);
            var ans = getList(ansSet[i]);
            System.out.print(sol);
            System.out.print(" vs ");
            System.out.print(ans);
            if(!equalLists(eps, Main.getSolution(eps, delta, eqSet[i][0], eqSet[i][1], eqSet[i][2], eqSet[i][3]), getList(ansSet[i])))
            {
                ok = false;
                fallIndex = i;
                System.out.println(" - false");
                break;
            }
            System.out.println(" - ok");
        }

        if(ok)
            System.out.println("\n" + eqSet.length + " tests complete successfully c:");
        else
            System.out.println("\nTest " + (fallIndex+1) + " failed :c");

        Assert.assertTrue(ok);
    }

    public List<Double> getList(double[] arr)
    {
        return DoubleStream.of(arr).boxed().collect(Collectors.toList());
    }

    public boolean equalLists(double eps, List<Double> one, List<Double> two)
    {
        if (one == null && two == null)
            return true;

        assert one != null;
        assert two != null;
        if(one.size() != two.size())
            return false;

        one = new ArrayList<>(one);
        two = new ArrayList<>(two);

        Collections.sort(one);
        Collections.sort(two);

        boolean ok = false;
        for(int i = 0; i < two.size(); i++)
        {
            for(int j = 0; j < one.size(); j++)
                if(Math.abs(one.get(i) - two.get(i)) < eps*2)
                {
                    ok = true;
                    break;
                }

            if(ok)
                ok = false;
            else
                return false;
        }

        return true;
    }
}