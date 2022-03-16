package ch.ludovic_mermod.dfasimulator.utils;

import java.util.function.Function;

public class Maths
{
    public static double dichotomy(Function<Double, Double> f, double a, double b)
    {
        if (Math.signum(f.apply(a)) == Math.signum(f.apply(b)))
            return Double.NaN;

        double neg = f.apply(a) < 0 ? a : b,
                pos = f.apply(a) > 0 ? a : b;

        while (Math.abs(f.apply((neg + pos) / 2)) > 1e-3)
        {
            double d = (neg + pos) / 2;
            double i = f.apply(d);

            if (i == 0) return 0;
            else if (i > 0) pos = d;
            else neg = d;
        }

        return (neg + pos) / 2;
    }
}
