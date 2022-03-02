package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.simulator.Path;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Link extends Group
{
    private final Path path;
    private final Line line, leftLine, rightLine;
    private final Text alphabetDisplay;
    private final ObjectProperty<StateNode> source, target;
    private ContextMenu menu;
    private MenuItem deleteMenuItem;

    /**
     * Constructs a link between two StateNodes
     * Creates a new Path without any alphabet
     *
     * @param source source StateNode
     * @param target target StateNode
     */
    public Link(StateNode source, StateNode target)
    {
        this(source, target, new Path(source.getState(), target.getState(), new TreeSet<>()));
    }

    /**
     * Constructs a link between two StateNodes representing the given Path
     *
     * @param source source StateNode
     * @param target target StateNode
     * @param path   path to represent
     */
    public Link(StateNode source, StateNode target, Path path)
    {
        this.source = new SimpleObjectProperty<>(source);
        this.target = new SimpleObjectProperty<>(target);
        this.path = path;

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
        alphabetDisplay.setText(path.getAlphabet().stream().map(Object::toString).collect(Collectors.joining(", ")));

        updatePositions();

        this.source.addListener((o, ov, nv) -> updatePositions());
        this.target.addListener((o, ov, nv) -> updatePositions());

        var updatePos = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> o, Number ov, Number nv)
            {
                Link.this.updatePositions();
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

    public String getSourceName()
    {
        return source.get().getState().getName();
    }
    public String getTargetName()
    {
        return target.get().getState().getName();
    }
    public SimulationPane getSimulatorParent()
    {
        return ((SimulationPane) getParent());
    }

    protected void bindSimulationPane(SimulationPane simulationPane)
    {
        deleteMenuItem.disableProperty().bind(simulationPane.getSimulationProperty());
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
        Point2D startCenter = new Point2D(source.get().getLayoutX(), source.get().getLayoutY()),
                endCenter = new Point2D(target.get().getLayoutX(), target.get().getLayoutY()),
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

    public void setAlphabet(Set<Character> alphabet)
    {
        path.setAlphabet(alphabet);
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
