package ch.thechi2000.dfasimulator.scene;

import ch.thechi2000.dfasimulator.simulator.State;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class StateNode extends Group
{
    private State state;
    private final Position pos = new Position();

    public StateNode(State state)
    {
        Circle circle = new Circle();
        circle.radiusProperty().bind(Constants.Node.Circle.radius);
        circle.fillProperty().bind(Constants.Node.Circle.color);

        Text text = new Text(state.getName());
        text.fontProperty().bind(Constants.Node.Text.font);

        getChildren().addAll(circle, new Text(state.getName()));
        addEventHandlers();
    }

    private void addEventHandlers()
    {
        setOnMouseEntered(event -> setCursor(Cursor.HAND));
        setOnMouseExited(event -> setCursor(Cursor.DEFAULT));

        setOnMousePressed(event ->
        {
            setCursor(Cursor.MOVE);

            //When a press event occurs, the location coordinates of the event are cached
            pos.x = event.getX();
            pos.y = event.getY();
        });
        setOnMouseReleased(event -> setCursor(Cursor.DEFAULT));

        setOnMouseDragged(event ->
        {
            double distanceX = event.getX() - pos.x;
            double distanceY = event.getY() - pos.y;

            double x = getLayoutX() + distanceX - 50;
            double y = getLayoutY() + distanceY - 50;

            //After calculating X and y, relocate the node to the specified coordinate point (x, y)
            relocate(x, y);
        });
    }

    private static class Position
    {
        double x;
        double y;
    }
}
