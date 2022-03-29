package ch.ludovic_mermod.dfasimulator.utils;

import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.util.function.Function;

public class Maths
{
    private static final double EPSILON = 1e-7;
    private Maths() {}

    /**
     * Use dichotomy to find a root of f in the interval [a, b]
     * If no root is found, return Nan
     *
     * @param f function to compute the root from
     * @param a minimum bound of the interval
     * @param b maximum bound of the interval
     * @return a root of f in the interval [a, b] or Nan if none is found
     */
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

    /**
     * Compute the intersections of two circle
     * If there is only one, the pair will contain two times the same point
     *
     * @param p0 center of the first circle
     * @param r0 radius of the first circle
     * @param p1 center of the second circle
     * @param r1 radius of the second circle
     * @return the intersection of the two circles
     */
    public static Pair<Point2D, Point2D> circleIntersection(Point2D p0, double r0, Point2D p1, double r1)
    {
        Point2D director = p1.subtract(p0).normalize(),
                normal = new Point2D(-director.getY(), director.getX()).normalize();

        if (p0.distance(p1) > (r0) + r1)
            return new Pair<>(null, null);
        else if (p0.distance(p1) == (r0) + r1)
            return new Pair<>(p0.add(director.multiply(r0)), p0.add(director.multiply(r0)));
        else
        {
            //https://stackoverflow.com/questions/3349125/circle-circle-intersection-points
            double d = p0.distance(p1),
                    x0 = p0.getX(), y0 = p0.getY(),
                    x1 = p1.getX(), y1 = p1.getY();

            double a = (r0 * r0 - r1 * r1 + d * d) / (2 * d),
                    h = Math.sqrt(r0 * r0 - a * a);

            Point2D p2 = p0.add(p1.subtract(p0).normalize().multiply(a));
            double x2 = p2.getX(), y2 = p2.getY();

            return new Pair<>(
                    new Point2D(
                            x2 + h * (y1 - y0) / d,
                            y2 - h * (x1 - x0) / d
                    ),
                    new Point2D(
                            x2 - h * (y1 - y0) / d,
                            y2 + h * (x1 - x0) / d
                    )
            );
        }
    }

    /**
     * Check if a ~ b
     *
     * @return a ~ b
     */
    public static boolean approx(double a, double b)
    {
        return Math.abs(a - b) < EPSILON;
    }
    /**
     * Check if a.x ~ b.x and a.y ~ b.y
     *
     * @return a.x ~ b.x and a.y ~ b.y
     */
    public static boolean approx(Point2D a, Point2D b)
    {
        return approx(a.getX(), b.getX()) && approx(a.getY(), b.getY());
    }

    /**
     * Return the angle between a vector and the horizontal
     *
     * @param p a vector
     * @return the angle between p and the horizontal
     */
    public static double angle(Point2D p)
    {
        double a = Math.atan(p.getY() / p.getX());
        if (p.getY() < 0) a += Math.PI;
        return a;
    }
    /**
     * Return the cosine of the angle between two vectors
     *
     * @param a the first vector
     * @param b the second vector
     * @return the cosine of the angle between a and b
     */
    public static double angleCos(Point2D a, Point2D b)
    {
        return a.dotProduct(b) / (a.magnitude() * b.magnitude());
    }
    /**
     * Return the angle between two vectors
     *
     * @param a the first vector
     * @param b the second vector
     * @return the angle between a and b
     */
    public static double angle(Point2D a, Point2D b)
    {
        return Math.acos(angle(a, b));
    }
    /**
     * Computes a unary vector with a given angle with the horizontal
     *
     * @param a an angle
     * @return a unary vector such that angle(v, horizontal) == a
     */
    public static Point2D fromAngle(double a)
    {
        return new Point2D(Math.cos(a), Math.sin(a));
    }

    /**
     * Computes a vector orthogonal to another
     *
     * @param p a vector
     * @return a vector orthogonal to p
     */
    public static Point2D orthogonal(Point2D p)
    {
        return new Point2D(-p.getY(), p.getX());
    }
    /**
     * Computes the projection of a vector on another
     *
     * @param point  the vector to project
     * @param target the vector to project on
     * @return the projection of point on target
     */
    public static Point2D projection(Point2D point, Point2D target)
    {
        return target.normalize().multiply(point.magnitude() * angleCos(point, target));
    }
}
