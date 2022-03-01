package ch.thechi2000.dfasimulator.scene;

import ch.thechi2000.dfasimulator.scene.lang.Strings;
import ch.thechi2000.dfasimulator.simulator.State;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class StateNode extends Group
{
    private final Position pos;
    private final State state;
    private final ContextMenu menu;

    /**
     * Constructs a StateNode representing a State
     *
     * @param state the state to represent
     */
    public StateNode(State state)
    {
        this.state = state;
        menu = createContextMenu();
        pos = new Position();

        Circle circle = new Circle();
        circle.radiusProperty().bind(Constants.Node.Circle.radius);
        circle.fillProperty().bind(Constants.Node.Circle.color);

        Text text = new Text(state.getName());
        text.fontProperty().bind(Constants.Node.Text.font);

        getChildren().addAll(circle, new Text(state.getName()));
        addEventHandlers();
        setOnContextMenuRequested(event ->
        {
            menu.show(this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    public SimulatorPane getSimulatorParent()
    {
        return ((SimulatorPane) getParent());
    }
    public State getState()
    {
        return state;
    }

    private void addEventHandlers()
    {
        setOnMouseEntered(event ->
                setCursor(switch (getSimulatorParent().getTool())
                        {
                            case EDIT -> Cursor.DEFAULT;
                            case DRAG, LINK -> Cursor.HAND;
                        }));
        setOnMouseExited(event -> setCursor(Cursor.DEFAULT));

        setOnMousePressed(event ->
        {
            menu.hide();
            if (event.isPrimaryButtonDown())
                switch (getSimulatorParent().getTool())
                {
                    case EDIT:
                    case LINK:
                        break;

                    case DRAG:
                        setCursor(Cursor.CLOSED_HAND);

                        //When a press event occurs, the location coordinates of the event are cached
                        pos.x = event.getX();
                        pos.y = event.getY();
                        break;
                }
        });
        setOnMouseReleased(event -> setCursor(Cursor.DEFAULT));

        setOnMouseDragged(event ->
        {
            if (event.isPrimaryButtonDown() && getSimulatorParent().getTool() == SimulatorPane.Tool.DRAG)
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
            if (event.isPrimaryButtonDown() && getSimulatorParent().getTool() == SimulatorPane.Tool.LINK)
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
            if (getParent() instanceof SimulatorPane && event.getGestureSource() instanceof StateNode)
                ((SimulatorPane) getParent()).createLink(event.getDragboard().getString(), state.getName());
        });
    }

    private ContextMenu createContextMenu()
    {
        ContextMenu menu = new ContextMenu();

        MenuItem delete = new MenuItem();
        Strings.bind("delete", delete.textProperty());
        delete.setOnAction(event -> ((SimulatorPane) getParent()).deleteNode(state.getName()));
        menu.getItems().add(delete);

        return menu;
    }

    private static class Position
    {
        double x;
        double y;
    }

}
