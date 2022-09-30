public class Equation
{
    private double a_eq = 0; //coefficient of x^3
    private double b_eq = 0; //coefficient of x^2
    private double c_eq = 0; //coefficient of x
    private double d_eq = 0; //coefficient of constant
    private int hashedDegree = -1;

    /**
     * @param b_eq
     * @param c_eq
     * @param d_eq
     * @return equation of type ax^3 + bx^2 + cx + d
     */
    public static Equation getDefaultCubicEquation(double a_eq, double b_eq, double c_eq, double d_eq)
    {
        Equation cub = new Equation();
        cub.a_eq = 1;
        cub.b_eq = b_eq / a_eq;
        cub.c_eq = c_eq / a_eq;
        cub.d_eq = d_eq / a_eq;

        return cub;
    }

    public double getA()
    {
        return a_eq;
    }

    public double getB()
    {
        return b_eq;
    }

    public double getC()
    {
        return c_eq;
    }

    public double getD()
    {
        return d_eq;
    }

    public double calc(double arg)
    {
        return a_eq * (arg * arg * arg) + b_eq * (arg * arg) + c_eq * (arg) + d_eq;
    }

    public double getDiscriminant()
    {
        if(getDegree() != 2)
            return Double.NaN;

        return c_eq*c_eq - 4 * b_eq * d_eq;
    }

    public int getDegree()
    {
        if(hashedDegree != -1)
            return hashedDegree;

        if(a_eq != 0)
            return (hashedDegree = 3);
        else if(b_eq != 0)
            return (hashedDegree = 2);
        else if(c_eq != 0)
            return (hashedDegree = 1);
        else
            return (hashedDegree = 0);
    }

    public static Equation getDerivative(Equation eq)
    {
        Equation der = new Equation();

        der.d_eq = eq.c_eq;
        der.c_eq = eq.b_eq * 2;
        der.b_eq = eq.a_eq * 3;

        return der;
    }
}
