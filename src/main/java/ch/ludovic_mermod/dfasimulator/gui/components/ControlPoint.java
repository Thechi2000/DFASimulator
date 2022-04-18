package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.settings.Settings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.shape.Circle;

import java.util.function.Function;

/**
 * Represents a movable small point
 * The position may be constraint (e.g. to force it to be on a given line)
 */
public class ControlPoint extends Group
{
    public static final String RADIUS = "graph.control_point.radius";
    public static final String FILL   = "graph.control_point.fill";

    private Point2D pos;

    private Function<Point2D, Point2D> constraint;

    public ControlPoint()
    {
        Circle circle = new Circle();
        circle.radiusProperty().bind(Settings.getDouble(RADIUS));
        circle.fillProperty().bind(Settings.getColor(FILL));
        getChildren().add(circle);

        addHandlers();
    }
    public ControlPoint(Function<Point2D, Point2D> constraint)
    {
        this();
        this.constraint = constraint;
    }
    public ControlPoint(DoubleProperty xProperty, DoubleProperty yProperty, ObservableBooleanValue visibleProperty)
    {
        this();

        layoutXProperty().bindBidirectional(xProperty);
        layoutYProperty().bindBidirectional(yProperty);
        visibleProperty().bind(visibleProperty);
    }
    public ControlPoint(DoubleProperty xProperty, DoubleProperty yProperty, ObservableBooleanValue visibleProperty, Function<Point2D, Point2D> constraint)
    {
        this(xProperty, yProperty, visibleProperty);
        this.constraint = constraint;
    }

    private void addHandlers()
    {
        setOnMouseEntered(event -> setCursor(Cursor.HAND));
        setOnMouseExited(event -> setCursor(Cursor.DEFAULT));

        setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown())
            {
                setCursor(Cursor.CLOSED_HAND);
                pos = new Point2D(event.getX(), event.getY());
            }
        });
        setOnMouseReleased(event -> setCursor(Cursor.DEFAULT));

        setOnMouseDragged(event ->
        {
            if (event.isPrimaryButtonDown())
            {
                double distanceX = event.getX() - pos.getX();
                double distanceY = event.getY() - pos.getY();

                double x = getLayoutX() + distanceX;
                double y = getLayoutY() + distanceY;

                Point2D target = new Point2D(x, y);
                if (constraint != null) target = constraint.apply(target);

                relocate(target.getX(), target.getY());
            }
        });
    }
}
