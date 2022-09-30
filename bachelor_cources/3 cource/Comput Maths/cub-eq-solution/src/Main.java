import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
    public static void main(String... args)
    {
        if (args.length < 6)
        {
            System.out.println("Wrong arg size!");
            return;
        }

        for (String arg : args)
            if (getNum(arg) == null)
            {
                System.out.println("Wrong argument: " + arg);
                return;
            }

        if (getNum(args[2]) == 0)
        {
            System.out.println("This is not a cubic equation!");
            return;
        }

        var roots = getSolution(getNum(args[0]), getNum(args[1]), getNum(args[2]), getNum(args[3]), getNum(args[4]), getNum(args[5]));
        switch (roots.size())
        {
            case 0:
                System.out.println("Equation haven't solution!");
                break;
            case 1:
                System.out.println("Equation have 1 root: " + roots.get(0));
                break;
            default:
                System.out.print("Equation have " + roots.size() + " roots: ");
                for (int i = 0; i < roots.size(); i++)
                    if (i == roots.size() - 1)
                        System.out.println(roots.get(i));
                    else
                        System.out.print(roots.get(i) + ", ");
        }
    }

    public static ArrayList<Double> getSolution(double eps, double delta, double a, double b, double c, double d)
    {
        ArrayList<Double> roots = new ArrayList<>();

        Equation equation = Equation.getDefaultCubicEquation(a, b, c, d);
        Equation derivative = Equation.getDerivative(equation);
        double dis = derivative.getDiscriminant();

        if (dis < 0)
        {
            Scanner scan = new Scanner(System.in);

            if (Math.abs(equation.calc(0)) < eps)
                roots.add(0d);
            else
            {
                System.out.println("I found that derivative of equation is always positive, \n" +
                        "so root maybe one, or equation haven't root. I will try \n" +
                        "find root on interval " + (equation.calc(0) > eps ? "(-oo, 0]" : "[0, +oo)") + ". You are agree?(y/n)");
                char ans = ' ';

                while (ans != 'y' && ans != 'n')
                    ans = scan.next().toLowerCase().charAt(0);

                if (ans == 'y')
                {
                    long maxIterations = -1;

                    System.out.println("Please enter the maximum number of iterations after which the program will stop:");

                    while (maxIterations <= 0)
                        maxIterations = scan.nextLong();

                    var root = equation.calc(0) > eps ?
                            RangeFinder.findRootAt(equation, eps, delta, Double.NEGATIVE_INFINITY, 0, maxIterations) :
                            RangeFinder.findRootAt(equation, eps, delta, 0, Double.POSITIVE_INFINITY, maxIterations);

                    if (Double.isNaN(root))
                        System.out.println("I didn't find the root :c");
                    else
                        roots.add(root);
                } else
                    System.out.println("Well:c");
            }
        } else if (dis == 0)
        {
            double derCalc = equation.calc(0);

            if (Math.abs(derCalc) < eps)
                roots.add(derCalc);
            else if (derCalc < -eps)
                roots.add(RangeFinder.findRootAt(equation, eps, delta, 0, Double.POSITIVE_INFINITY, Long.MAX_VALUE));
            else if (derCalc > eps)
                roots.add(RangeFinder.findRootAt(equation, eps, delta, Double.NEGATIVE_INFINITY, 0, Long.MAX_VALUE));
        } else if (dis > 0)
        {
            double x1 = (-derivative.getC() + Math.sqrt(dis)) / (2 * derivative.getB());
            double x2 = (-derivative.getC() - Math.sqrt(dis)) / (2 * derivative.getB());

            if (equation.calc(x1) < equation.calc(x2))
            {
                var tmp = x1;
                x1 = x2;
                x2 = tmp;
            }

            if (equation.calc(x1) > eps && equation.calc(x2) > eps) // f(x1) > e & f(x2) > e
                roots.add(RangeFinder.findRootAt(equation, eps, delta, Double.NEGATIVE_INFINITY, x1, Long.MAX_VALUE));
            else if (equation.calc(x1) < -eps && equation.calc(x2) < -eps) // f(x1) < -e & f(x2) < -e
                roots.add(RangeFinder.findRootAt(equation, eps, delta, x2, Double.POSITIVE_INFINITY, Long.MAX_VALUE));
            else if (equation.calc(x1) > eps && Math.abs(equation.calc(x2)) < eps) // f(x1) > e & |f(x2)| < e
            {
                roots.add(x2);
                roots.add(RangeFinder.findRootAt(equation, eps, delta, Double.NEGATIVE_INFINITY, x1, Long.MAX_VALUE));
            } else if (equation.calc(x2) < -eps && Math.abs(equation.calc(x1)) < eps) // f(x2) < -e & |f(x1)| < e
            {
                roots.add(x1);
                roots.add(RangeFinder.findRootAt(equation, eps, delta, x2, Double.POSITIVE_INFINITY, Long.MAX_VALUE));
            } else if (equation.calc(x1) > eps && equation.calc(x2) < -eps) // f(x1) > e & f(x2) < -e
            {
                roots.add(RangeFinder.findRootAt(equation, eps, delta, x2, Double.POSITIVE_INFINITY, Long.MAX_VALUE));
                roots.add(RangeFinder.findRootAt(equation, eps, delta, x1, x2, Long.MAX_VALUE));
                roots.add(RangeFinder.findRootAt(equation, eps, delta, Double.NEGATIVE_INFINITY, x1, Long.MAX_VALUE));
            } else if (Math.abs(equation.calc(x2)) < eps && Math.abs(equation.calc(x1)) < eps) // |f(x1)| < e & |f(x2)| < e
                roots.add((x2 + x1) / 2);
        }

        return roots;
    }

    public static Double getNum(String str)
    {
        try
        {
            return Double.parseDouble(str);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }
}
