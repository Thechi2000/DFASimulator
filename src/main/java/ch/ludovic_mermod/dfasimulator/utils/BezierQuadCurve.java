package ch.ludovic_mermod.dfasimulator.utils;

import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Point2D;

/**
 * Utility class for operation on a Bezier Quadratic Curve
 *
 * @param p0x x position of the first control point
 * @param p0y y position of the first control point
 * @param p1x x position of the second control point
 * @param p1y y position of the second control point
 * @param p2x x position of the third control point
 * @param p2y y position of the third control point
 */
public record BezierQuadCurve(ObservableDoubleValue p0x, ObservableDoubleValue p0y, ObservableDoubleValue p1x, ObservableDoubleValue p1y,
                              ObservableDoubleValue p2x, ObservableDoubleValue p2y)
{
    private static final double DERIVATIVE_COEFFICIENT = 1e-10;

    public BezierQuadCurve
    {
    }

    /**
     * Compute a point of the curve with the given t parameter
     *
     * @param t (double) curve parameter
     * @return B(t)
     */
    public Point2D apply(double t)
    {
        final Point2D p0 = new Point2D(p0x.get(), p0y.get());
        final Point2D p1 = new Point2D(p1x.get(), p1y.get());
        final Point2D p2 = new Point2D(p2x.get(), p2y.get());
        return p0.multiply((1 - t) * (1 - t)).add(p1.multiply(2 * (1 - t) * t)).add(p2.multiply(t * t));
    }

    /**
     * Compute the distance between a point on the curve and another one
     *
     * @param t       (double) a point on the curve
     * @param point2D (Point2D) a point
     * @return the distance between the two points
     */
    public double distance(double t, Point2D point2D)
    {
        return point2D.distance(apply(t));
    }

    /**
     * Compute the antecedent of the closest point from point2D on the curve
     *
     * @param point2D (Point2D) a point
     * @return t such that distance(B(t), point2D) is minimal
     */
    public double findClosest(Point2D point2D)
    {
        double dichotomy = Maths.dichotomy(t -> distanceDerivative(t, point2D), 0, 1);
        return Double.isNaN(dichotomy) ? distance(0, point2D) < distance(1, point2D) ? 0 : 1 : dichotomy;
    }

    private double distanceDerivative(double t, Point2D point2D)
    {
        return (distance(t + DERIVATIVE_COEFFICIENT, point2D) - distance(t - DERIVATIVE_COEFFICIENT, point2D)) / (2 * DERIVATIVE_COEFFICIENT);
    }
}
