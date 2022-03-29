package ch.ludovic_mermod.dfasimulator.utils;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;

/**
 * Represent a Point2D property
 */
public class Point2DProperty extends ObjectProperty<Point2D>
{
    private final String         name;
    private final Object         bean;
    private final DoubleProperty x;
    private final DoubleProperty y;

    /**
     * Constructs a Point2DProperty with value {0, 0}
     */
    public Point2DProperty()
    {
        this(null, "");
    }
    /**
     * Constructs a Point2DProperty with value {x, y}
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     */
    public Point2DProperty(double x, double y)
    {
        this(null, "", x, y);
    }
    /**
     * Constructs a Point2DProperty with value {0, 0}
     *
     * @param bean bean of the property
     * @param name name of the property
     */
    public Point2DProperty(Object bean, String name)
    {
        this(bean, name, 0, 0);
    }
    /**
     * Constructs a Point2DProperty with value {x, y}
     *
     * @param bean bean of the property
     * @param name name of the property
     * @param x    x coordinate of the point
     * @param y    y coordinate of the point
     */
    public Point2DProperty(Object bean, String name, double x, double y)
    {
        this.bean = bean;
        this.name = name;

        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
    }

    @Override
    public void bind(ObservableValue<? extends Point2D> observableValue)
    {
        CustomBindings.bindDouble(x, () -> observableValue.getValue().getX(), observableValue);
        CustomBindings.bindDouble(y, () -> observableValue.getValue().getY(), observableValue);
    }
    @Override
    public void unbind()
    {
        x.unbind();
        y.unbind();
    }
    @Override
    public boolean isBound()
    {
        return x.isBound() || y.isBound();
    }
    @Override
    public Object getBean()
    {
        return bean;
    }
    @Override
    public String getName()
    {
        return name;
    }
    @Override
    public Point2D get()
    {
        return new Point2D(x.get(), y.get());
    }
    @Override
    public void set(Point2D point2D)
    {
        set(point2D.getX(), point2D.getY());
    }
    public void set(double x, double y)
    {
        this.x.set(x);
        this.y.set(y);
    }
    @Override
    public void addListener(ChangeListener<? super Point2D> changeListener)
    {
        x.addListener((o, ov, nv) -> changeListener.changed(this, new Point2D(ov.doubleValue(), y.get()), new Point2D(nv.doubleValue(), y.get())));
        y.addListener((o, ov, nv) -> changeListener.changed(this, new Point2D(x.get(), ov.doubleValue()), new Point2D(x.get(), nv.doubleValue())));
    }
    @Override
    public void removeListener(ChangeListener<? super Point2D> changeListener)
    {
        x.removeListener((o, ov, nv) -> changeListener.changed(this, new Point2D(ov.doubleValue(), y.get()), new Point2D(nv.doubleValue(), y.get())));
        y.removeListener((o, ov, nv) -> changeListener.changed(this, new Point2D(x.get(), ov.doubleValue()), new Point2D(x.get(), nv.doubleValue())));
    }
    @Override
    public void addListener(InvalidationListener invalidationListener)
    {
        x.addListener(invalidationListener);
        y.addListener(invalidationListener);
    }
    @Override
    public void removeListener(InvalidationListener invalidationListener)
    {
        x.addListener(invalidationListener);
        y.addListener(invalidationListener);
    }

    public double getX()
    {
        return x.get();
    }
    public void setX(double x) {this.x.set(x);}

    public double getY()
    {
        return y.get();
    }
    public void setY(double y) {this.y.set(y);}

    public DoubleProperty xProperty()
    {
        return x;
    }
    public DoubleProperty yProperty()
    {
        return y;
    }
}
