package ch.ludovic_mermod.dfasimulator.utils;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Point2D;

public class BezierQuadCurve
{
    private static final double DERIVATIVE_COEFFICIENT = 1e-10;

    private final ObservableDoubleValue p0x;
    private final ObservableDoubleValue p0y;

    private final ObservableDoubleValue p1x;
    private final ObservableDoubleValue p1y;

    private final ObservableDoubleValue p2x;
    private final ObservableDoubleValue p2y;

    public BezierQuadCurve(ObservableDoubleValue p0x, ObservableDoubleValue p0y, ObservableDoubleValue p1x, ObservableDoubleValue p1y, ObservableDoubleValue p2x, ObservableDoubleValue p2y)
    {
        this.p0x = p0x;
        this.p0y = p0y;
        this.p1x = p1x;
        this.p1y = p1y;
        this.p2x = p2x;
        this.p2y = p2y;
    }
    public BezierQuadCurve()
    {
        this(new SimpleDoubleProperty(), new SimpleDoubleProperty(), new SimpleDoubleProperty(), new SimpleDoubleProperty(), new SimpleDoubleProperty(), new SimpleDoubleProperty());
    }

    public Point2D apply(double t)
    {
        return new Point2D(p0x.get(), p0y.get()).multiply((1 - t) * (1 - t)).add(new Point2D(p1x.get(), p1y.get())).multiply(2 * (1 - t) * t).add(new Point2D(p2x.get(), p2y.get()).multiply(t * t));
    }

    public double distance(double t, Point2D point2D)
    {
        return point2D.distance(apply(t));
    }

    public double distanceDerivative(double t, Point2D point2D)
    {
        return (distance(t + DERIVATIVE_COEFFICIENT, point2D) - distance(t - DERIVATIVE_COEFFICIENT, point2D)) / (2 * DERIVATIVE_COEFFICIENT);
    }

    public double findClosest(Point2D point2D)
    {
        double dichotomy = Maths.dichotomy(t -> distanceDerivative(t, point2D), 0, 1);
        return Double.isNaN(dichotomy) ? distance(0, point2D) < distance(1, point2D) ? 0 : 1 : dichotomy;
    }
}
