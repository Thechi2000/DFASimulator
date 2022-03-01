package ch.thechi2000.dfasimulator.scene;

import ch.thechi2000.dfasimulator.scene.lang.Strings;
import ch.thechi2000.dfasimulator.simulator.Path;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class Link extends Group
{
    private final StateNode from;
    private final StateNode to;
    private final Line line, leftLine, rightLine;
    private final Text alphabetDisplay;
    private ContextMenu menu;

    /**
     * Constructs a link between two StateNodes
     * Creates a new Path without any alphabet
     *
     * @param from source StateNode
     * @param to   target StateNode
     */
    public Link(StateNode from, StateNode to)
    {
        this(from, to, new Path(from.getState(), to.getState(), new ArrayList<>()));
    }

    /**
     * Constructs a link between two StateNodes representing the given Path
     *
     * @param from source StateNode
     * @param to   target StateNode
     * @param path path to represent
     */
    public Link(StateNode from, StateNode to, Path path)
    {
        this.from = from;
        this.to = to;

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
        alphabetDisplay.setText(String.join(", ", path.getAlphabet()));

        updatePositions();

        from.layoutXProperty().addListener((o, ov, nv) -> updatePositions());
        from.layoutYProperty().addListener((o, ov, nv) -> updatePositions());

        to.layoutXProperty().addListener((o, ov, nv) -> updatePositions());
        to.layoutYProperty().addListener((o, ov, nv) -> updatePositions());

        Constants.Node.Circle.radius.addListener((o, ov, nv) -> updatePositions());
        Constants.Link.Text.distanceFromLine.addListener((o, ov, nv) -> updatePositions());
        Constants.Link.Line.sidelineLength.addListener((o, ov, nv) -> updatePositions());

        getChildren().addAll(line, leftLine, rightLine, alphabetDisplay);
    }

    public String getSourceName()
    {
        return from.getState().getName();
    }
    public String getTargetName()
    {
        return to.getState().getName();
    }
    public SimulatorPane getSimulatorParent()
    {
        return ((SimulatorPane) getParent());
    }

    private ContextMenu createContextMenu()
    {
        menu = new ContextMenu();

        MenuItem delete = new MenuItem();
        Strings.bind("delete", delete.textProperty());
        delete.setOnAction(event -> getSimulatorParent().deleteLink(this));

        setOnMousePressed(event -> menu.hide());
        setOnContextMenuRequested(event -> menu.show(this, event.getScreenX(), event.getScreenY()));

        return menu;
    }

    private void updatePositions()
    {
        Point2D startCenter = new Point2D(from.getLayoutX(), from.getLayoutY()),
                endCenter = new Point2D(to.getLayoutX(), to.getLayoutY()),
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
}
