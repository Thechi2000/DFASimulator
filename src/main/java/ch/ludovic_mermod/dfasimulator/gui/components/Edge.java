package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.gui.GraphPane;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.State;
import ch.ludovic_mermod.dfasimulator.utils.BezierQuadCurve;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.text.Text;

import java.util.stream.Collectors;

public class Edge extends GraphItem
{
    private final JSONObject jsonObject;

    private final State source;
    private final State target;

    private final DoubleProperty targetT;

    private final MoveTo          moveTo;
    private final QuadCurveTo     curve;
    private final BezierQuadCurve bezier;

    //    private final Line leftLine, rightLine;
    private final Arrow arrow;
    private final Text  alphabetDisplay;
    private final Path  path;

    public Edge(State source, State target, GraphPane graphPane)
    {
        super(graphPane);

        this.source = source;
        this.target = target;
        targetT = new SimpleDoubleProperty(0.5);

        alphabetDisplay = new Text();
        alphabetDisplay.fontProperty().bind(Constants.GRAPH_FONT);
        updateAlphabetDisplay();
        source.transitionMap().addListener((p, k, o, n) -> updateAlphabetDisplay());
        source.transitionMap().addListener((k, p) -> updateAlphabetDisplay());

        path = new Path(moveTo = new MoveTo(), curve = new QuadCurveTo());

        arrow = new Arrow(path);
        arrow.fillProperty().bind(Constants.EDGE_LINE_COLOR);
        arrow.widthProperty().bind(Constants.EDGE_LINE_WIDTH);
        arrow.sidelineLengthProperty().bind(Constants.EDGE_SIDELINE_LENGTH);
        arrow.endXProperty().bind(curve.xProperty());
        arrow.endYProperty().bind(curve.yProperty());
        arrow.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        bezier = new BezierQuadCurve(
                moveTo.xProperty(),
                moveTo.yProperty(),
                curve.controlXProperty(),
                curve.controlYProperty(),
                curve.xProperty(),
                curve.yProperty());

        addEventHandlers();
        bindPositions();

        getChildren().addAll(arrow, alphabetDisplay, new ControlPoint(curve.controlXProperty(), curve.controlYProperty()));

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

    public JSONObject getJSONObject()
    {
        return jsonObject;
    }

    public void setControlPoint(double x, double y)
    {
        curve.setControlX(x);
        curve.setControlY(y);
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
        path.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown() && graphPane.getTool() == GraphPane.Tool.DRAG)
            {
                curve.setControlX(reverseBezierForControl(moveTo.getX(), curve.getX(), targetT.get(), event.getX()));
                curve.setControlY(reverseBezierForControl(moveTo.getY(), curve.getY(), targetT.get(), event.getY()));
            }
        });
    }

    private void bindPositions()
    {
        final Node sn = source.getNode();
        final Node tn = target.getNode();

        Observable[] observables = new Observable[]{
                sn.layoutXProperty(), sn.layoutYProperty(), sn.widthBinding(), sn.heightBinding(),
                tn.layoutXProperty(), tn.layoutYProperty(), tn.widthBinding(), tn.heightBinding(),
                curve.controlXProperty(), curve.controlYProperty(),
                Constants.NODE_INNER_CIRCLE_RADIUS,
                Constants.EDGE_SIDELINE_LENGTH,
                Constants.EDGE_TEXT_DISTANCE_FROM_NODE_FACTOR,
                Constants.EDGE_TEXT_DISTANCE_FROM_NODE_ABSOLUTE,
                Constants.EDGE_TEXT_USE_ABSOLUTE_DISTANCE
        };

        CustomBindings.bindDouble(moveTo.xProperty(), () -> computePoints().start.getX(), observables);
        CustomBindings.bindDouble(moveTo.yProperty(), () -> computePoints().start.getY(), observables);
        CustomBindings.bindDouble(curve.xProperty(), () -> computePoints().end.getX(), observables);
        CustomBindings.bindDouble(curve.yProperty(), () -> computePoints().end.getY(), observables);

        CustomBindings.bindDouble(arrow.directionXProperty(), () -> curve.getX() - curve.getControlX(), curve.xProperty(), curve.controlXProperty());
        CustomBindings.bindDouble(arrow.directionYProperty(), () -> curve.getY() - curve.getControlY(), curve.yProperty(), curve.controlYProperty());

        CustomBindings.bindDouble(alphabetDisplay.xProperty(), () -> new Point2D(curve.getX(), curve.getY()).subtract(bezier.apply(0.5)).normalize().multiply(20).add(bezier.apply(0.5)).getX(), observables);
        CustomBindings.bindDouble(alphabetDisplay.yProperty(), () -> new Point2D(curve.getX(), curve.getY()).subtract(bezier.apply(0.5)).normalize().multiply(20).add(bezier.apply(0.5)).getY(), observables);
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
                startCenter = new Point2D(sn.getLayoutX(), sn.getLayoutY()),
                endCenter = new Point2D(tn.getLayoutX(), tn.getLayoutY()),
                directorStart = controlPoint.subtract(startCenter).normalize(),
                directorEnd = endCenter.subtract(controlPoint).normalize(),
                normalEnd = new Point2D(directorEnd.getY(), -directorEnd.getX()),
                start = startCenter.add(directorStart.multiply(Constants.NODE_INNER_CIRCLE_RADIUS.get())),
                end = endCenter.subtract(directorEnd.multiply(Constants.NODE_INNER_CIRCLE_RADIUS.get())),
                projectionPoint = end.subtract(directorEnd.multiply(Constants.EDGE_SIDELINE_LENGTH.get())),
                projectionDistance = normalEnd.multiply(Constants.EDGE_SIDELINE_LENGTH.get());

        return new Points(
                start,
                end,
                start.add(end.subtract(start).multiply(0.5)),
                projectionPoint.add(projectionDistance),
                projectionPoint.subtract(projectionDistance));
    }

    private double reverseBezierForControl(double p0, double p2, double t, double target)
    {
        return (target - t * t * p2 - (1 - t) * (1 - t) * p0) / (2 * (1 - t) * t);
    }

    private record Points(Point2D start, Point2D end, Point2D center, Point2D leftLineStart, Point2D rightLineStart) {}
}
