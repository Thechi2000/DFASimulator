package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Edge extends Group
{
    private final ObjectProperty<StateNode> source, target;
    private final SetProperty<Character> alphabetProperty;

    private final Line line, leftLine, rightLine;
    private final Text alphabetDisplay;

    private ContextMenu menu;
    private MenuItem deleteMenuItem;

    public Edge(StateNode source, StateNode target)
    {
        this(source, target, Set.of());
    }

    /**
     * Constructs a link between two StateNodes representing the given Path
     *
     * @param source source StateNode
     * @param target target StateNode
     */
    public Edge(StateNode source, StateNode target, Set<Character> alphabet)
    {
        alphabetProperty = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>(alphabet)));
        this.source = new SimpleObjectProperty<>(source);
        this.target = new SimpleObjectProperty<>(target);

        menu = createContextMenu();

        line = new Line();
        line.fillProperty().bind(Constants.Link.Line.color);
        line.strokeWidthProperty().bind(Constants.Link.Line.width);

        leftLine = new Line();
        leftLine.fillProperty().bind(Constants.Link.Line.color);
        leftLine.strokeWidthProperty().bind(Constants.Link.Line.width);

        rightLine = new Line();
        rightLine.fillProperty().bind(Constants.Link.Line.color);
        rightLine.strokeWidthProperty().bind(Constants.Link.Line.width);

        alphabetDisplay = new Text();
        alphabetDisplay.setText(alphabetProperty.stream().map(Object::toString).collect(Collectors.joining(", ")));
        alphabetProperty.addListener((o, ov, nv) -> alphabetDisplay.setText(nv.stream().map(Object::toString).collect(Collectors.joining(", "))));

        updatePositions();

        this.source.addListener((o, ov, nv) -> updatePositions());
        this.target.addListener((o, ov, nv) -> updatePositions());

        var updatePos = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> o, Number ov, Number nv)
            {
                Edge.this.updatePositions();
            }
        };

        source.layoutXProperty().addListener(updatePos);
        source.layoutYProperty().addListener(updatePos);

        target.layoutXProperty().addListener(updatePos);
        target.layoutYProperty().addListener(updatePos);

        Constants.Node.Circle.radius.addListener(updatePos);
        Constants.Link.Text.distanceFromLine.addListener(updatePos);
        Constants.Link.Line.sidelineLength.addListener(updatePos);

        getChildren().addAll(line, leftLine, rightLine, alphabetDisplay);

        setOnMousePressed(event -> getSimulatorParent().bindEditPane(this));
    }

    public static Edge fromJSONObject(JsonObject object, GraphPane graphPane)
    {
        Set<Character> alphabet = new TreeSet<>();
        JsonArray alphabetArray = object.get("alphabet").getAsJsonArray();
        for (int i = 0; i < alphabetArray.size(); ++i)
            alphabet.add(alphabetArray.get(i).getAsString().charAt(0));

        return new Edge(
                graphPane.getNode(object.get("source_name").getAsString()),
                graphPane.getNode(object.get("target_name").getAsString()),
                alphabet);
    }
    public JsonElement toJSONObject()
    {
        JsonObject object = new JsonObject();
        object.addProperty("source_name", source.get().getName());
        object.addProperty("target_name", target.get().getName());

        JsonArray alphabetArray = new JsonArray();
        alphabetProperty.forEach(c -> alphabetArray.add(c.toString()));

        object.add("alphabet", alphabetArray);
        return object;
    }

    public String getSourceName()
    {
        return source.get().getName();
    }
    public String getTargetName()
    {
        return target.get().getName();
    }
    public GraphPane getSimulatorParent()
    {
        return ((GraphPane) getParent());
    }

    protected void bindSimulationPane(GraphPane graphPane)
    {
        deleteMenuItem.disableProperty().bind(graphPane.getSimulationProperty());
    }

    private ContextMenu createContextMenu()
    {
        menu = new ContextMenu();

        deleteMenuItem = new MenuItem();
        Strings.bind("delete", deleteMenuItem.textProperty());
        deleteMenuItem.setOnAction(event -> getSimulatorParent().deleteLink(this));
        //delete.disableProperty().bind(getSimulatorParent().getSimulationProperty());

        setOnMousePressed(event -> menu.hide());
        setOnContextMenuRequested(event -> menu.show(this, event.getScreenX(), event.getScreenY()));

        return menu;
    }

    private void updatePositions()
    {
        double radius = Constants.Node.Circle.radius.get();
        Point2D startCenter = new Point2D(source.get().getLayoutX() + radius, source.get().getLayoutY() + radius),
                endCenter = new Point2D(target.get().getLayoutX() + radius, target.get().getLayoutY() + radius),
                director = endCenter.subtract(startCenter).normalize(),
                normal = new Point2D(director.getY(), -director.getX()),
                start = startCenter.add(director.multiply(Constants.Node.Circle.radius.get())),
                end = endCenter.subtract(director.multiply(Constants.Node.Circle.radius.get()));

        line.setStartX(start.getX());
        line.setStartY(start.getY());
        line.setEndX(end.getX());
        line.setEndY(end.getY());

        // Position the two lines from the side of the arrows
        {
            Point2D projectionPoint = end.subtract(director.multiply(Constants.Link.Line.sidelineLength.get())),
                    projectionDistance = normal.multiply(Constants.Link.Line.sidelineLength.get()),
                    leftStart = projectionPoint.add(projectionDistance),
                    rightStart = projectionPoint.subtract(projectionDistance);

            leftLine.setStartX(leftStart.getX());
            leftLine.setStartY(leftStart.getY());
            leftLine.setEndX(end.getX());
            leftLine.setEndY(end.getY());

            rightLine.setStartX(rightStart.getX());
            rightLine.setStartY(rightStart.getY());
            rightLine.setEndX(end.getX());
            rightLine.setEndY(end.getY());
        }

        // Position the alphabet display
        {
            Point2D projectionRelative = start.add(end.subtract(start).multiply(Constants.Link.Text.distanceFromNodeFactor.get())),
                    projectionAbsolute = start.add(director.multiply(Constants.Link.Text.distanceFromNodeAbsolute.get())),
                    textPos = (Constants.Link.Text.usesAbsoluteDistance.get() && (projectionRelative.subtract(start).magnitude() > Constants.Link.Text.distanceFromNodeAbsolute.get()) ? projectionAbsolute : projectionRelative).add(normal.multiply(Constants.Link.Text.distanceFromLine.get()));

            double angle = new Point2D(1, 0).angle(director);

            alphabetDisplay.relocate(textPos.getX(), textPos.getY());
            alphabetDisplay.setRotate(director.getY() > 0 ? angle : -angle);
        }
    }

    protected SetProperty<Character> alphabetProperty()
    {
        return alphabetProperty;
    }

    protected ObjectProperty<StateNode> getSource()
    {
        return source;
    }
    protected ObjectProperty<StateNode> getTarget()
    {
        return target;
    }
}
