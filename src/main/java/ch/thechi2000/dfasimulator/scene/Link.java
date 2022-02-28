package ch.thechi2000.dfasimulator.scene;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Line;

public class Link extends Group
{
    private final StateNode from;
    private final StateNode to;
    private final Line line;

    public Link(StateNode from, StateNode to)
    {
        this.from = from;
        this.to = to;

        line = new Line();
        line.fillProperty().bind(Constants.Link.Line.color);
        line.strokeWidthProperty().bind(Constants.Link.Line.width);
        updateLinePosition();

        from.layoutXProperty().addListener((o, ov, nv) -> updateLinePosition());
        from.layoutYProperty().addListener((o, ov, nv) -> updateLinePosition());

        to.layoutXProperty().addListener((o, ov, nv) -> updateLinePosition());
        to.layoutYProperty().addListener((o, ov, nv) -> updateLinePosition());

        Constants.Node.Circle.radius.addListener((o, ov, nv) -> updateLinePosition());

        getChildren().add(line);
    }

    private void updateLinePosition()
    {
        Point2D start = new Point2D(from.getLayoutX(), from.getLayoutY()),
                end = new Point2D(to.getLayoutX(), to.getLayoutY()),
                director = end.subtract(start).normalize().multiply(Constants.Node.Circle.radius.get());

        start = start.add(director);
        end = end.subtract(director);

        line.setStartX(start.getX());
        line.setStartY(start.getY());

        line.setEndX(end.getX());
        line.setEndY(end.getY());
    }
}
