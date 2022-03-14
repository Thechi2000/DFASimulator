package ch.ludovic_mermod.dfasimulator.gui.scene.components;

import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.scene.GraphPane;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.value.ChangeListener;
import javafx.collections.MapChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.stream.Collectors;

public class Edge extends Group
{
    private final State source;
    private final State target;

    private final Line line, leftLine, rightLine;
    private final Text alphabetDisplay;
    private final GraphPane graphPane;

    private MenuItem deleteMenuItem;

    public Edge(State source, State target, GraphPane graphPane)
    {
        this.source = source;
        this.target = target;
        this.graphPane = graphPane;

        alphabetDisplay = new Text();
        updateAlphabetDisplay();
        source.transitionMap().addListener((p, k, o, n) -> updateAlphabetDisplay());

        line = new Line();
        line.fillProperty().bind(Constants.Link.Line.color);
        line.strokeWidthProperty().bind(Constants.Link.Line.width);
        line.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        leftLine = new Line();
        leftLine.fillProperty().bind(Constants.Link.Line.color);
        leftLine.strokeWidthProperty().bind(Constants.Link.Line.width);
        leftLine.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        rightLine = new Line();
        rightLine.fillProperty().bind(Constants.Link.Line.color);
        rightLine.strokeWidthProperty().bind(Constants.Link.Line.width);
        rightLine.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        updatePositions();

        ChangeListener<Number> updatePos = (o, ov, nv) -> Edge.this.updatePositions();

        source.getNode().layoutXProperty().addListener(updatePos);
        source.getNode().layoutYProperty().addListener(updatePos);

        target.getNode().layoutXProperty().addListener(updatePos);
        target.getNode().layoutYProperty().addListener(updatePos);

        Constants.Node.Circle.radius.addListener(updatePos);
        Constants.Link.Text.distanceFromLine.addListener(updatePos);
        Constants.Link.Line.sidelineLength.addListener(updatePos);

        getChildren().addAll(line, leftLine, rightLine, alphabetDisplay);
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

    protected void bindSimulationPane(GraphPane graphPane)
    {
        deleteMenuItem.disableProperty().bind(graphPane.getSimulationProperty());
    }

    /*private ContextMenu createContextMenu()
    {
        menu = new ContextMenu();

        deleteMenuItem = new MenuItem();
        Strings.bind("delete", deleteMenuItem.textProperty());
        deleteMenuItem.setOnAction(event -> graphPane.fi().deleteLink(link));
        //delete.disableProperty().bind(getSimulatorParent().getSimulationProperty());

        setOnMousePressed(event -> menu.hide());
        setOnContextMenuRequested(event -> menu.show(this, event.getScreenX(), event.getScreenY()));

        return menu;
    }*/

    private void updatePositions()
    {
        double radius = Constants.Node.Circle.radius.get();
        Point2D startCenter = new Point2D(source.getNode().getLayoutX() + radius, source.getNode().getLayoutY() + radius),
                endCenter = new Point2D(target.getNode().getLayoutX() + radius, target.getNode().getLayoutY() + radius),
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
}
