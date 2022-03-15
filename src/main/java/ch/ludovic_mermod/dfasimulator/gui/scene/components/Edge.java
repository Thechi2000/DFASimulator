package ch.ludovic_mermod.dfasimulator.gui.scene.components;

import ch.ludovic_mermod.dfasimulator.Utils;
import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.scene.GraphPane;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.Observable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.text.Text;

import java.util.stream.Collectors;

public class Edge extends Group
{
    private final JSONObject jsonObject;

    private final State     source;
    private final State     target;
    private final GraphPane graphPane;

    private final MoveTo      moveTo;
    private final QuadCurveTo curve;

    private final Line leftLine, rightLine;
    private final Text alphabetDisplay;

    public Edge(State source, State target, GraphPane graphPane)
    {
        this.source = source;
        this.target = target;
        this.graphPane = graphPane;

        alphabetDisplay = new Text();
        updateAlphabetDisplay();
        source.transitionMap().addListener((p, k, o, n) -> updateAlphabetDisplay());

        Path line = new Path(moveTo = new MoveTo(), curve = new QuadCurveTo());
        line.setFill(Color.TRANSPARENT);
        line.strokeProperty().bind(Constants.Link.Line.color);
        line.strokeWidthProperty().bind(Constants.Link.Line.width);
        line.prefWidth(20);
        line.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        leftLine = new Line();
        leftLine.fillProperty().bind(Constants.Link.Line.color);
        leftLine.strokeWidthProperty().bind(Constants.Link.Line.width);
        leftLine.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        rightLine = new Line();
        rightLine.fillProperty().bind(Constants.Link.Line.color);
        rightLine.strokeWidthProperty().bind(Constants.Link.Line.width);
        rightLine.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        addEventHandlers();
        bindPositions();

        getChildren().addAll(line, leftLine, rightLine, alphabetDisplay);

        jsonObject = new JSONObject();
        jsonObject.addProperty("source", source.nameProperty());
        jsonObject.addProperty("target", target.nameProperty());
        jsonObject.addProperty("control_x", curve.controlXProperty());
        jsonObject.addProperty("control_y", curve.controlYProperty());
    }

    public State source()
    {
        return source;
    }
    public State target()
    {
        return target;
    }
    public String getSourceName()
    {
        return source.name();
    }
    public String getTargetName()
    {
        return target.name();
    }

    public JSONObject getJsonObject()
    {
        return jsonObject;
    }

    @Override
    public String toString()
    {
        return "Edge{" +
               "source=" + source.name() +
               ", target=" + target.name() +
               '}';
    }

    private void addEventHandlers()
    {
        setOnMouseDragged(event -> {
            if (graphPane.getTool() == GraphPane.Tool.DRAG)
            {
                curve.setControlX(reverseBezierForControl(moveTo.getX(), curve.getX(), 0.5, event.getX()));
                curve.setControlY(reverseBezierForControl(moveTo.getY(), curve.getY(), 0.5, event.getY()));
            }
        });
    }

    private void bindPositions()
    {
        final Node sn = source.getNode();
        final Node tn = target.getNode();

        Observable[] observables = new Observable[]{
                sn.layoutXProperty(), sn.layoutYProperty(), sn.widthProperty(), sn.heightProperty(),
                tn.layoutXProperty(), tn.layoutYProperty(), tn.widthProperty(), tn.heightProperty(),
                curve.controlXProperty(), curve.controlYProperty(),
                Constants.Node.Circle.radius,
                Constants.Link.Line.sidelineLength,
                Constants.Link.Text.distanceFromNodeFactor,
                Constants.Link.Text.distanceFromNodeAbsolute,
                Constants.Link.Text.usesAbsoluteDistance
        };

        Utils.bindDouble(moveTo.xProperty(), () -> computePoints().start.getX(), observables);
        Utils.bindDouble(moveTo.yProperty(), () -> computePoints().start.getY(), observables);
        Utils.bindDouble(curve.xProperty(), () -> computePoints().end.getX(), observables);
        Utils.bindDouble(curve.yProperty(), () -> computePoints().end.getY(), observables);

        Utils.bindDouble(leftLine.startXProperty(), () -> computePoints().leftLineStart.getX(), observables);
        Utils.bindDouble(leftLine.startYProperty(), () -> computePoints().leftLineStart.getY(), observables);
        Utils.bindDouble(leftLine.endXProperty(), () -> computePoints().end.getX(), observables);
        Utils.bindDouble(leftLine.endYProperty(), () -> computePoints().end.getY(), observables);

        Utils.bindDouble(rightLine.startXProperty(), () -> computePoints().rightLineStart.getX(), observables);
        Utils.bindDouble(rightLine.startYProperty(), () -> computePoints().rightLineStart.getY(), observables);
        Utils.bindDouble(rightLine.endXProperty(), () -> computePoints().end.getX(), observables);
        Utils.bindDouble(rightLine.endYProperty(), () -> computePoints().end.getY(), observables);

        Utils.bindDouble(alphabetDisplay.xProperty(), () -> computePoints().textPos.getX(), observables);
        Utils.bindDouble(alphabetDisplay.yProperty(), () -> computePoints().textPos.getY(), observables);
    }

    private void updateAlphabetDisplay()
    {
        alphabetDisplay.setText(source.transitionMap()
                .entrySet()
                .stream()
                .filter(e -> target.equals(e.getValue().get()))
                .map(e -> e.getKey().toString())
                .sorted()
                .collect(Collectors.joining(", ")));
    }

    private Points computePoints()
    {
        Node sn = source.getNode();
        Node tn = target.getNode();

        Point2D controlPoint = new Point2D(curve.getControlX(), curve.getControlY()),
                startCenter = new Point2D(sn.getLayoutX() + sn.getWidth() / 2, sn.getLayoutY() + sn.getHeight() / 2),
                endCenter = new Point2D(tn.getLayoutX() + tn.getWidth() / 2, tn.getLayoutY() + tn.getHeight() / 2),
                directorStart = controlPoint.subtract(startCenter).normalize(),
                directorEnd = endCenter.subtract(controlPoint).normalize(),
                normalEnd = new Point2D(directorEnd.getY(), -directorEnd.getX()),
                start = startCenter.add(directorStart.multiply(Constants.Node.Circle.radius.get())),
                end = endCenter.subtract(directorEnd.multiply(Constants.Node.Circle.radius.get())),
                projectionPoint = end.subtract(directorEnd.multiply(Constants.Link.Line.sidelineLength.get())),
                projectionDistance = normalEnd.multiply(Constants.Link.Line.sidelineLength.get()),
                projectionRelative = start.add(end.subtract(start).multiply(Constants.Link.Text.distanceFromNodeFactor.get())),
                projectionAbsolute = start.add(directorStart.multiply(Constants.Link.Text.distanceFromNodeAbsolute.get()));

        return new Points(
                start,
                end,
                start.add(end.subtract(start).multiply(0.5)),
                projectionPoint.add(projectionDistance),
                projectionPoint.subtract(projectionDistance),
                (Constants.Link.Text.usesAbsoluteDistance.get() && (projectionRelative.subtract(start).magnitude() > Constants.Link.Text.distanceFromNodeAbsolute.get()) ? projectionAbsolute : projectionRelative).add(normalEnd.multiply(Constants.Link.Text.distanceFromLine.get())));
    }

    private double reverseBezierForControl(double p0, double p2, double t, double target)
    {
        return (target - t * t * p2 - (1 - t) * (1 - t) * p0) / (2 * (1 - t) * t);
    }

    private record Points(Point2D start, Point2D end, Point2D center, Point2D leftLineStart, Point2D rightLineStart, Point2D textPos) {}
}
