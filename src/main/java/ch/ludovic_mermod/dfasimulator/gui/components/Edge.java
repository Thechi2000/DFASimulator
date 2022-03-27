package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.gui.GraphPane;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.logic.State;
import ch.ludovic_mermod.dfasimulator.utils.BezierQuadCurve;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Edge extends GraphItem
{
    public static final String JSON_SOURCE    = "source";
    public static final String JSON_TARGET    = "target";
    public static final String JSON_CONTROL_X = "control_x";
    public static final String JSON_CONTROL_Y = "control_y";

    public static final String COLOR           = "graph.edge.color";
    public static final String WIDTH           = "graph.edge.width";
    public static final String SIDELINE_LENGTH = "graph.edge.sideline_length";

    private final JSONObject jsonObject;

    private final State source;
    private final State target;

    private final DoubleProperty targetPointX;
    private final DoubleProperty targetPointY;

    private final MoveTo          moveTo;
    private final QuadCurveTo     curve;
    private final BezierQuadCurve bezier;

    private final Arrow arrow;
    private final Text  alphabetDisplay;
    private final Path  path;

    private final AtomicBoolean updatingControlFromTarget, updatingTargetFromControl;
    private Binding<Point2D> targetBinding;

    public Edge(State source, State target, GraphPane graphPane)
    {
        super(graphPane);

        this.source = source;
        this.target = target;

        targetPointX = new SimpleDoubleProperty();
        targetPointY = new SimpleDoubleProperty();
        updatingControlFromTarget = new AtomicBoolean(false);
        updatingTargetFromControl = new AtomicBoolean(false);

        alphabetDisplay = new Text();
        Constants.getDouble(GraphPane.FONT_SIZE).addListener((o, ov, nv) -> alphabetDisplay.setFont(new Font(alphabetDisplay.getFont().getName(), nv)));
        updateAlphabetDisplay();
        source.transitionMap().addListener((p, k, o, n) -> updateAlphabetDisplay());
        source.transitionMap().addListener((k, p) -> updateAlphabetDisplay());

        path = new Path(moveTo = new MoveTo(), curve = new QuadCurveTo());

        arrow = new Arrow(path);
        arrow.fillProperty().bind(Constants.getColor(COLOR));
        arrow.widthProperty().bind(Constants.getDouble(WIDTH));
        arrow.sidelineLengthProperty().bind(Constants.getDouble(SIDELINE_LENGTH));
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

        bindPositions();

        getChildren().addAll(arrow, alphabetDisplay);
        addControlComponents();

        jsonObject = new JSONObject();
        jsonObject.addProperty(JSON_SOURCE, source.nameProperty());
        jsonObject.addProperty(JSON_TARGET, target.nameProperty());
        jsonObject.addProperty(JSON_CONTROL_X, curve.controlXProperty());
        jsonObject.addProperty(JSON_CONTROL_Y, curve.controlYProperty());
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
    public void loadJSONObject(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasNumber(JSON_CONTROL_X);
        object.checkHasNumber(JSON_CONTROL_Y);
        setControlPoint(object.get(JSON_CONTROL_X).getAsDouble(), object.get(JSON_CONTROL_Y).getAsDouble());
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

    @Override
    public void onMousePressed(MouseEvent event)
    {
        final Point2D mousePosition = new Point2D(event.getX(), event.getY());
        if (bezier.distance(bezier.findClosest(mousePosition), mousePosition) < 10 || new Point2D(curve.getControlX(), curve.getControlY()).distance(mousePosition) < Constants.getDoubleValue(ControlPoint.RADIUS))
            super.onMousePressed(event);
    }

    private void bindPositions()
    {
        final Node sn = source.getNode();
        final Node tn = target.getNode();

        Observable[] observables = new Observable[]{
                sn.layoutXProperty(), sn.layoutYProperty(), sn.widthBinding(), sn.heightBinding(),
                tn.layoutXProperty(), tn.layoutYProperty(), tn.widthBinding(), tn.heightBinding(),
                curve.controlXProperty(), curve.controlYProperty(),
                Constants.getDouble(Node.INNER_CIRCLE_RADIUS),
                Constants.getDouble(SIDELINE_LENGTH)
        };

        CustomBindings.bindDouble(moveTo.xProperty(), () -> computePoints().start.getX(), observables);
        CustomBindings.bindDouble(moveTo.yProperty(), () -> computePoints().start.getY(), observables);
        CustomBindings.bindDouble(curve.xProperty(), () -> computePoints().end.getX(), observables);
        CustomBindings.bindDouble(curve.yProperty(), () -> computePoints().end.getY(), observables);

        CustomBindings.bindDouble(arrow.directionXProperty(), () -> curve.getX() - curve.getControlX(), curve.xProperty(), curve.controlXProperty());
        CustomBindings.bindDouble(arrow.directionYProperty(), () -> curve.getY() - curve.getControlY(), curve.yProperty(), curve.controlYProperty());

        CustomBindings.bindDouble(alphabetDisplay.xProperty(), () -> new Point2D(curve.getX(), curve.getY()).subtract(bezier.apply(0.5)).normalize().multiply(20).add(bezier.apply(0.5)).getX(), observables);
        CustomBindings.bindDouble(alphabetDisplay.yProperty(), () -> new Point2D(curve.getX(), curve.getY()).subtract(bezier.apply(0.5)).normalize().multiply(20).add(bezier.apply(0.5)).getY(), observables);

        ChangeListener<Number> updateControl = (o, ov, nv) -> {
            if (updatingTargetFromControl.get()) return;

            updatingControlFromTarget.set(true);
            curve.setControlX(Edge.this.reverseBezierForControl(moveTo.getX(), curve.getX(), 0.5, targetPointX.get()));
            curve.setControlY(Edge.this.reverseBezierForControl(moveTo.getY(), curve.getY(), 0.5, targetPointY.get()));
            updatingControlFromTarget.set(false);
        };
        targetPointX.addListener(updateControl);
        targetPointY.addListener(updateControl);

        targetBinding = CustomBindings.create(() -> bezier.apply(0.5), moveTo.xProperty(), moveTo.yProperty(), curve.controlXProperty(), curve.controlYProperty(), curve.xProperty(), curve.yProperty());
        targetBinding.addListener((o, ov, nv) -> {
            if (updatingControlFromTarget.get()) return;

            updatingTargetFromControl.set(true);
            targetPointX.set(nv.getX());
            targetPointY.set(nv.getY());
            updatingTargetFromControl.set(false);
        });
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
                start = startCenter.add(directorStart.multiply(Constants.getDoubleValue(Node.INNER_CIRCLE_RADIUS))),
                end = endCenter.subtract(directorEnd.multiply(Constants.getDoubleValue(Node.INNER_CIRCLE_RADIUS))),
                projectionPoint = end.subtract(directorEnd.multiply(Constants.getDoubleValue(SIDELINE_LENGTH))),
                projectionDistance = normalEnd.multiply(Constants.getDoubleValue(SIDELINE_LENGTH));

        return new Points(
                start,
                end,
                start.add(end.subtract(start).multiply(0.5)),
                projectionPoint.add(projectionDistance),
                projectionPoint.subtract(projectionDistance));
    }

    private void addControlComponents()
    {
        ControlPoint controlPoint = new ControlPoint(curve.controlXProperty(), curve.controlYProperty(), focusProperty());
        ControlPoint onCurvePoint = new ControlPoint(targetPointX, targetPointY, focusProperty());

        ControlLine startControlLine = new ControlLine(
                curve.controlXProperty(),
                curve.controlYProperty(),
                moveTo.xProperty(),
                moveTo.yProperty(),
                focusProperty());

        ControlLine endControlLine = new ControlLine(
                curve.controlXProperty(),
                curve.controlYProperty(),
                curve.xProperty(),
                curve.yProperty(),
                focusProperty());

        getChildren().addAll(startControlLine, endControlLine, onCurvePoint, controlPoint);
    }

    private double reverseBezierForControl(double p0, double p2, double t, double target)
    {
        return (target - t * t * p2 - (1 - t) * (1 - t) * p0) / (2 * (1 - t) * t);
    }

    private record Points(Point2D start, Point2D end, Point2D center, Point2D leftLineStart, Point2D rightLineStart) {}
}
