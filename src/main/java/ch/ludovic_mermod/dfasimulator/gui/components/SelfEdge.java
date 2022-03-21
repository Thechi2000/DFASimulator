package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.logic.State;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import ch.ludovic_mermod.dfasimulator.utils.Maths;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.stream.Collectors;

public class SelfEdge extends Group
{
    private final State state;

    private final Arrow arrow;
    private final Text  alphabetDisplay;

    private final DoubleProperty centerX;
    private final DoubleProperty centerY;
    private final DoubleProperty radius;

    private final MoveTo startingPoint;
    private final ArcTo  arc;

    public SelfEdge(State state)
    {
        this.state = state;

        centerX = new SimpleDoubleProperty();
        centerY = new SimpleDoubleProperty();
        radius = new SimpleDoubleProperty();

        Observable[] circleObservables = new Observable[]{
                centerX, centerY, radius,
                state.getNode().centerXProperty(),
                state.getNode().centerYProperty(),
                state.getNode().radiusProperty()
        };

        centerX.bind(state.getNode().centerXProperty());
        centerY.bind(state.getNode().centerYProperty().subtract(state.getNode().radiusProperty().multiply(1.5)));
        radius.bind(state.getNode().radiusProperty());

        startingPoint = new MoveTo();
        CustomBindings.bindDouble(startingPoint.xProperty(), () -> computeStartPoint().getX(), circleObservables);
        CustomBindings.bindDouble(startingPoint.yProperty(), () -> computeStartPoint().getY(), circleObservables);

        arc = new ArcTo();
        arc.radiusXProperty().bind(radius);
        arc.radiusYProperty().bind(radius);
        arc.largeArcFlagProperty().set(true);
        arc.sweepFlagProperty().set(true);
        CustomBindings.bindDouble(arc.xProperty(), () -> computeEndPoint().getX(), circleObservables);
        CustomBindings.bindDouble(arc.yProperty(), () -> computeEndPoint().getY(), circleObservables);

        arrow = new Arrow(new Path(startingPoint, arc));
        arrow.endXProperty().bind(arc.xProperty());
        arrow.endYProperty().bind(arc.yProperty());
        arrow.widthProperty().bind(Constants.EDGE_LINE_WIDTH);
        arrow.sidelineLengthProperty().bind(Constants.EDGE_SIDELINE_LENGTH);
        arrow.fillProperty().bind(Constants.EDGE_LINE_COLOR);

        Observable[] tangentObservables = new Observable[]{
                arc.xProperty(), arc.yProperty()
        };
        CustomBindings.bindDouble(arrow.directionXProperty(), () -> computeTangent(new Point2D(arc.getX(), arc.getY())).getX(), tangentObservables);
        CustomBindings.bindDouble(arrow.directionYProperty(), () -> computeTangent(new Point2D(arc.getX(), arc.getY())).getY(), tangentObservables);

        alphabetDisplay = new Text();
        alphabetDisplay.xProperty().bind(centerX);
        alphabetDisplay.yProperty().bind(centerY. subtract(radius).subtract(20));
        updateAlphabetDisplay();
        state.transitionMap().addListener((p, k, o, n) -> updateAlphabetDisplay());
        state.transitionMap().addListener((k, p) -> updateAlphabetDisplay());

        getChildren().addAll(arrow, alphabetDisplay);
    }

    private void updateAlphabetDisplay()
    {
        alphabetDisplay.setText(state.transitionMap()
                .entrySet()
                .stream()
                .filter(e -> state.equals(e.getValue().get()))
                .map(e -> e.getKey().toString())
                .sorted()
                .collect(Collectors.joining(", ")));
    }

    private Point2D computeStartPoint()
    {
        return Maths.circleIntersection(
                        new Point2D(centerX.get(), centerY.get()),
                        radius.get(),
                        new Point2D(state.getNode().centerXProperty().get(), state.getNode().centerYProperty().get()),
                        state.getNode().radiusProperty().get())
                .getKey();
    }
    private Point2D computeEndPoint()
    {
        return Maths.circleIntersection(
                        new Point2D(centerX.get(), centerY.get()),
                        radius.get(),
                        new Point2D(state.getNode().centerXProperty().get(), state.getNode().centerYProperty().get()),
                        state.getNode().radiusProperty().get())
                .getValue();
    }
    private Point2D computeTangent(Point2D point)
    {
        Point2D rad = point.subtract(new Point2D(centerX.get(), centerY.get()));
        return new Point2D(-rad.getY(), rad.getX());
    }
}
