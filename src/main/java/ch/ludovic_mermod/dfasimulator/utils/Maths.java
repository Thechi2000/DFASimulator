package ch.ludovic_mermod.dfasimulator.utils;

import javafx.geometry.Point2D;
import javafx.util.Pair;

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

    public static Pair<Point2D, Point2D> circleIntersection(Point2D aCenter, double aRadius, Point2D bCenter, double bRadius)
    {
        Point2D director = bCenter.subtract(aCenter).normalize(),
                normal = new Point2D(-director.getY(), director.getX()).normalize();

        if (aCenter.distance(bCenter) > (aRadius) + bRadius)
            return new Pair<>(null, null);
        else if (aCenter.distance(bCenter) == (aRadius) + bRadius)
            return new Pair<>(aCenter.add(director.multiply(aRadius)), null);
        else
        {
            double d = aCenter.distance(bCenter),
                    d1 = (aRadius + d - bRadius) / 2,
                    alpha = Math.asin(d1 / aRadius),
                    h = aRadius * Math.cos(alpha);

            return new Pair<>(aCenter.add(director.multiply(d1)).add(normal.multiply(h)), aCenter.add(director.multiply(d1)).subtract(normal.multiply(h)));
        }
    }
}
