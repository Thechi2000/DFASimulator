package ch.thechi2000.dfasimulator.scene;

import ch.thechi2000.dfasimulator.simulator.State;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class StateNode extends Group
{
    private final Position pos = new Position();
    private final State state;

    public StateNode(State state)
    {
        this.state = state;

        Circle circle = new Circle();
        circle.radiusProperty().bind(Constants.Node.Circle.radius);
        circle.fillProperty().bind(Constants.Node.Circle.color);

        Text text = new Text(state.getName());
        text.fontProperty().bind(Constants.Node.Text.font);

        getChildren().addAll(circle, new Text(state.getName()));
        addEventHandlers();
    }

    public State getState()
    {
        return state;
    }

    private void addEventHandlers()
    {
        setOnMouseEntered(event -> setCursor(Cursor.HAND));
        setOnMouseExited(event -> setCursor(Cursor.DEFAULT));

        setOnMousePressed(event ->
        {
            if (event.isPrimaryButtonDown())
            {
                setCursor(Cursor.MOVE);

                //When a press event occurs, the location coordinates of the event are cached
                pos.x = event.getX();
                pos.y = event.getY();
            }
        });
        setOnMouseReleased(event -> setCursor(Cursor.DEFAULT));

        setOnMouseDragged(event ->
        {
            if (event.isPrimaryButtonDown())
            {
                double distanceX = event.getX() - pos.x;
                double distanceY = event.getY() - pos.y;

                double x = getLayoutX() + distanceX - 50;
                double y = getLayoutY() + distanceY - 50;

                //After calculating X and y, relocate the node to the specified coordinate point (x, y)
                relocate(x, y);
            }
        });


        setOnDragDetected(event ->
        {
            if (event.isSecondaryButtonDown())
            {
                Dragboard db = startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString(state.getName());
                db.setContent(content);
            }

            event.consume();
        });
        setOnDragOver(event ->
        {
            if (event.getGestureSource() != this && event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });
        setOnDragDropped(event ->
        {
            if (getParent() instanceof SimulatorPane)
                ((SimulatorPane) getParent()).createLink(event.getDragboard().getString(), state.getName());
        });
    }

    private static class Position
    {
        double x;
        double y;
    }
}
