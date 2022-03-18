package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;

public class Arrow extends Group
{
    private final DoubleProperty endX;
    private final DoubleProperty endY;
    private final DoubleProperty directionX;
    private final DoubleProperty directionY;

    private final DoubleProperty sidelineLength;
    private final DoubleProperty width;

    private final ObjectProperty<Paint> fill;

    private final Line leftLine;
    private final Line rightLine;

    public Arrow(Path path)
    {
        leftLine = new Line();
        rightLine = new Line();
        path.setFill(Color.TRANSPARENT);

        endX = new SimpleDoubleProperty();
        endY = new SimpleDoubleProperty();
        directionX = new SimpleDoubleProperty();
        directionY = new SimpleDoubleProperty();
        sidelineLength = new SimpleDoubleProperty();

        width = new SimpleDoubleProperty();
        leftLine.strokeWidthProperty().bind(width);
        rightLine.strokeWidthProperty().bind(width);
        path.strokeWidthProperty().bind(width);

        fill = new SimpleObjectProperty<>(Color.BLACK);
        leftLine.fillProperty().bind(fill);
        rightLine.fillProperty().bind(fill);
        path.strokeProperty().bind(fill);

        Observable[] observables = new Observable[]{endX, endY, directionX, directionY, sidelineLength};

        CustomBindings.bindDouble(leftLine.startXProperty(), () -> computeLeftStart().getX(), observables);
        CustomBindings.bindDouble(leftLine.startYProperty(), () -> computeLeftStart().getY(), observables);
        leftLine.endXProperty().bind(endX);
        leftLine.endYProperty().bind(endY);

        CustomBindings.bindDouble(rightLine.startXProperty(), () -> computeRightStart().getX(), observables);
        CustomBindings.bindDouble(rightLine.startYProperty(), () -> computeRightStart().getY(), observables);
        rightLine.endXProperty().bind(endX);
        rightLine.endYProperty().bind(endY);

        getChildren().addAll(path, leftLine, rightLine);
    }

    private Point2D computeProjection()
    {
        return new Point2D(endX.get(), endY.get()).subtract(new Point2D(directionX.get(), directionY.get()).normalize().multiply(sidelineLength.get() / Math.sqrt(2)));
    }
    private Point2D computeNormal()
    {
        return new Point2D(-directionY.get(), directionX.get()).normalize();
    }
    private Point2D computeLeftStart()
    {
        return computeProjection().add(computeNormal().multiply(sidelineLength.get() / Math.sqrt(2)));
    }
    private Point2D computeRightStart()
    {
        return computeProjection().subtract(computeNormal().multiply(sidelineLength.get() / Math.sqrt(2)));
    }

    public DoubleProperty endXProperty()
    {
        return endX;
    }
    public DoubleProperty endYProperty()
    {
        return endY;
    }
    public DoubleProperty directionXProperty()
    {
        return directionX;
    }
    public DoubleProperty directionYProperty()
    {
        return directionY;
    }

    public DoubleProperty sidelineLengthProperty()
    {
        return sidelineLength;
    }
    public DoubleProperty widthProperty()
    {
        return width;
    }

    public ObjectProperty<Paint> fillProperty()
    {
        return fill;
    }
}
