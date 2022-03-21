package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.shape.Circle;

public class ControlPoint extends Group
{
    private Point2D pos;

    public ControlPoint()
    {
        Circle circle = new Circle();
        circle.radiusProperty().bind(Constants.CONTROL_POINT_RADIUS);
        circle.fillProperty().bind(Constants.CONTROL_POINT_FILL);
        getChildren().add(circle);

        addHandlers();
    }
    public ControlPoint(DoubleProperty xProperty, DoubleProperty yProperty, ObservableBooleanValue visibleProperty)
    {
        this();

        layoutXProperty().bindBidirectional(xProperty);
        layoutYProperty().bindBidirectional(yProperty);
        visibleProperty().bind(visibleProperty);
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

                relocate(x, y);
            }
        });
    }
}
