package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Group;
import javafx.scene.shape.Line;

public class ControlLine extends Group
{
    private final Line line;

    public ControlLine()
    {
        line = new Line();
        line.strokeWidthProperty().bind(Constants.CONTROL_LINE_WIDTH);
        line.strokeProperty().bind(Constants.CONTROL_LINE_FILL);
        line.getStrokeDashArray().addAll(5d, 5d);

        getChildren().add(line);
    }
    public ControlLine(DoubleProperty startX, DoubleProperty startY, DoubleProperty endX, DoubleProperty endY, ObservableBooleanValue visible)
    {
        this();

        line.startXProperty().bind(startX);
        line.startYProperty().bind(startY);
        line.endXProperty().bind(endX);
        line.endYProperty().bind(endY);
        line.visibleProperty().bind(visible);
    }
}
