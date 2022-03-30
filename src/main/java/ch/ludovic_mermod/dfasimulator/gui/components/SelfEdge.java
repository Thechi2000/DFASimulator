package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.gui.GraphPane;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.logic.State;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import ch.ludovic_mermod.dfasimulator.utils.Maths;
import ch.ludovic_mermod.dfasimulator.utils.Mutex;
import ch.ludovic_mermod.dfasimulator.utils.Point2DProperty;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.stream.Collectors;

/**
 * Represent an Edge from a Node to itself
 */
public class SelfEdge extends GraphItem
{
    public static final String JSON_RADIUS   = "radius";
    public static final String JSON_CENTER_Y = "center_y";
    public static final String JSON_CENTER_X = "center_x";
    public static final String JSON_STATE    = "state";

    private final JSONObject jsonObject;

    private final State state;

    private final Arrow arrow;
    private final Text  alphabetDisplay;

    private final DoubleProperty radius;

    private final Point2DProperty center;
    private final Point2DProperty radiusControlPoint;

    private final MoveTo startingPoint;
    private final ArcTo  arc;

    private final Mutex mutex;

    public SelfEdge(State state, GraphPane graphPane)
    {
        super(graphPane);

        this.state = state;
        mutex = new Mutex();

        center = new Point2DProperty(this, "center", state.getNode().centerXProperty().get(), state.getNode().centerYProperty().get() - 75);
        radius = new SimpleDoubleProperty(this, "radius", state.getNode().radiusProperty().get());
        radiusControlPoint = new Point2DProperty();

        // Create alphabet display
        {
            alphabetDisplay = new Text();
            alphabetDisplay.fontProperty().bind(Constants.getFont("graph.font"));
            alphabetDisplay.xProperty().bind(center.xProperty());
            alphabetDisplay.yProperty().bind(center.yProperty().subtract(radius).subtract(20));
            updateAlphabetDisplay();
            state.transitionMap().addListener((p, k, o, n) -> updateAlphabetDisplay());
            state.transitionMap().addListener((k, p) -> updateAlphabetDisplay());
        }

        // Create path components
        {
            startingPoint = new MoveTo();

            arc = new ArcTo();
            arc.radiusXProperty().set(radius.get());
            arc.radiusYProperty().set(radius.get());
            arc.largeArcFlagProperty().set(true);
            arc.sweepFlagProperty().set(true);
            arc.radiusXProperty().bind(radius);
            arc.radiusYProperty().bind(radius);
        }

        // Create arrow
        {
            arrow = new Arrow(new Path(startingPoint, arc));
            arrow.endXProperty().bind(arc.xProperty());
            arrow.endYProperty().bind(arc.yProperty());
            arrow.widthProperty().bind(Constants.getDouble(Edge.WIDTH));
            arrow.sidelineLengthProperty().bind(Constants.getDouble(Edge.SIDELINE_LENGTH));
            arrow.fillProperty().bind(Constants.getColor(Edge.COLOR));
            arrow.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());
        }

        // Add endpoints bindings
        {
            Observable[] circleObservables = new Observable[]{
                    center, radius,
                    state.getNode().centerXProperty(),
                    state.getNode().centerYProperty(),
                    state.getNode().radiusProperty()
            };

            CustomBindings.bindDouble(startingPoint.xProperty(), () -> computeStartPoint().getX(), circleObservables);
            CustomBindings.bindDouble(startingPoint.yProperty(), () -> computeStartPoint().getY(), circleObservables);
            CustomBindings.bindDouble(arc.xProperty(), () -> computeEndPoint().getX(), circleObservables);
            CustomBindings.bindDouble(arc.yProperty(), () -> computeEndPoint().getY(), circleObservables);
        }

        // Add move listeners
        {
            state.getNode().centerXProperty().addListener((o1, ov1, nv1) -> center.setX(center.getX() - ov1.doubleValue() + nv1.doubleValue()));
            state.getNode().centerYProperty().addListener((o, ov, nv) -> center.setY(center.getY() - ov.doubleValue() + nv.doubleValue()));
        }

        // Add arrow direction bindings
        {
            Observable[] tangentObservables = new Observable[]{
                    arc.xProperty(), arc.yProperty()
            };
            CustomBindings.bindDouble(arrow.directionXProperty(), () -> computeTangent(new Point2D(arc.getX(), arc.getY())).getX(), tangentObservables);
            CustomBindings.bindDouble(arrow.directionYProperty(), () -> computeTangent(new Point2D(arc.getX(), arc.getY())).getY(), tangentObservables);
        }

        // Add listeners
        {
            radiusControlPoint.addListener((o, ov, nv) -> {
                if (mutex.isLocked()) return;
                mutex.lock();
                recomputeRadius();
                mutex.unlock();
            });

            center.addListener((o, ov, nv) -> {
                if (mutex.isLocked()) return;
                mutex.lock();
                recomputeControl();
                mutex.unlock();
            });
        }

        recomputeControl();
        getChildren().addAll(arrow, alphabetDisplay);
        addControlComponents();

        jsonObject = new JSONObject();
        jsonObject.addProperty(JSON_STATE, state.nameProperty());
        jsonObject.addProperty(JSON_CENTER_X, center.xProperty());
        jsonObject.addProperty(JSON_CENTER_Y, center.yProperty());
        jsonObject.addProperty(JSON_RADIUS, radius);
    }

    public JSONObject getJSONObject()
    {
        return jsonObject;
    }
    public void loadJSONObject(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasNumber(JSON_CENTER_X);
        object.checkHasNumber(JSON_CENTER_Y);
        object.checkHasNumber(JSON_RADIUS);

        center.set(object.get(JSON_CENTER_X).getAsDouble(), object.get(JSON_CENTER_Y).getAsDouble());
        radius.set(object.get(JSON_RADIUS).getAsDouble());

        recomputeControl();
    }

    private void addControlComponents()
    {
        getChildren().addAll(new ControlLine(center, radiusControlPoint, focusProperty()),
                new ControlPoint(center.xProperty(), center.yProperty(), focusProperty()),
                new ControlPoint(radiusControlPoint.xProperty(), radiusControlPoint.yProperty(), focusProperty(),
                        p -> center.get().add(Maths.projection(p.subtract(center.get()), state.getNode().getCenter().subtract(center.get())))));
    }

    private void recomputeRadius()
    {
        radius.set(center.get().distance(radiusControlPoint.get()));
    }
    private void recomputeControl()
    {
        radiusControlPoint.set(center.get().add((center.get().subtract(state.getNode().getCenter()).normalize()).multiply(arc.getRadiusX())));
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
        return computeIntersections().getValue();
    }
    private Point2D computeEndPoint()
    {
        return computeIntersections().getKey();
    }
    private Pair<Point2D, Point2D> computeIntersections()
    {
        final Point2D center = this.center.get();
        final Point2D nodeCenter = state.getNode().getCenter();
        final Pair<Point2D, Point2D> intersection = Maths.circleIntersection(
                center,
                radius.get(),
                nodeCenter,
                state.getNode().radiusProperty().get());

        return new Pair<>(
                intersection.getKey() == null
                ? center.subtract(center.subtract(nodeCenter).normalize().multiply(radius.get()))
                : intersection.getKey(),
                intersection.getValue() == null
                ? center.add(center.subtract(nodeCenter).normalize().multiply(radius.get()))
                : intersection.getValue());
    }

    private Point2D computeTangent(Point2D point)
    {
        Point2D rad = point.subtract(center.get());
        return new Point2D(-rad.getY(), rad.getX());
    }

    public State state()
    {
        return state;
    }
}
