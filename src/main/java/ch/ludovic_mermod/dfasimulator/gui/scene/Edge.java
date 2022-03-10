package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.Link;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.stream.Collectors;

public class Edge extends Group
{
    private final Link link;

    private final Line line, leftLine, rightLine;
    private final Text alphabetDisplay;
    private final GraphPane graphPane;

    private ContextMenu menu;
    private MenuItem deleteMenuItem;

    public Edge(Link link, GraphPane graphPane)
    {
        this.link = link;
        this.graphPane = graphPane;
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
        alphabetDisplay.setText(link.alphabetProperty().stream().map(Object::toString).collect(Collectors.joining(", ")));
        link.alphabetProperty().addListener((o, ov, nv) -> alphabetDisplay.setText(nv.stream().map(Object::toString).collect(Collectors.joining(", "))));

        updatePositions();

        link.source().addListener((o, ov, nv) -> updatePositions());
        link.target().addListener((o, ov, nv) -> updatePositions());

        var updatePos = new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> o, Number ov, Number nv)
            {
                Edge.this.updatePositions();
            }
        };

        link.source().get().getNode().layoutXProperty().addListener(updatePos);
        link.source().get().getNode().layoutYProperty().addListener(updatePos);

        link.target().get().getNode().layoutXProperty().addListener(updatePos);
        link.target().get().getNode().layoutYProperty().addListener(updatePos);

        Constants.Node.Circle.radius.addListener(updatePos);
        Constants.Link.Text.distanceFromLine.addListener(updatePos);
        Constants.Link.Line.sidelineLength.addListener(updatePos);

        getChildren().addAll(line, leftLine, rightLine, alphabetDisplay);

        setOnMousePressed(event -> graphPane.getMainPane().bindEditPane(this));
    }

    public String getSourceName()
    {
        return link.source().get().getName();
    }
    public String getTargetName()
    {
        return link.target().get().getName();
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
        deleteMenuItem.setOnAction(event -> graphPane.getSimulation().deleteLink(link));
        //delete.disableProperty().bind(getSimulatorParent().getSimulationProperty());

        setOnMousePressed(event -> menu.hide());
        setOnContextMenuRequested(event -> menu.show(this, event.getScreenX(), event.getScreenY()));

        return menu;
    }

    private void updatePositions()
    {
        double radius = Constants.Node.Circle.radius.get();
        Point2D startCenter = new Point2D(link.source().get().getNode().getLayoutX() + radius, link.source().get().getNode().getLayoutY() + radius),
                endCenter = new Point2D(link.target().get().getNode().getLayoutX() + radius, link.target().get().getNode().getLayoutY() + radius),
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

    public SetProperty<Character> alphabetProperty()
    {
        return link.alphabetProperty();
    }

    protected ObjectProperty<State> getSource()
    {
        return link.source();
    }
    protected ObjectProperty<State> getTarget()
    {
        return link.target();
    }
    public Link getLink()
    {
        return link;
    }

}
