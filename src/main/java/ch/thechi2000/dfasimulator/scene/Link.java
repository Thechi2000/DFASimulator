package ch.thechi2000.dfasimulator.scene;

import ch.thechi2000.dfasimulator.simulator.Path;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class Link extends Group
{
    private final StateNode from;
    private final StateNode to;

    private final Line line, leftLine, rightLine;
    private final Text alphabetDisplay;

    public Link(StateNode from, StateNode to, Path path)
    {
        this.from = from;
        this.to = to;

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

        getChildren().addAll(line, leftLine, rightLine, alphabetDisplay);
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

        Point2D mid = start.midpoint(end),
                textPos = mid.add(normal.multiply(Constants.Link.Text.distance.get()));
        alphabetDisplay.relocate(textPos.getX(), textPos.getY());

        var angle = new Point2D(1, 0).angle(director);
        alphabetDisplay.setRotate(director.getY() > 0 ? angle : -angle);
    }
}
