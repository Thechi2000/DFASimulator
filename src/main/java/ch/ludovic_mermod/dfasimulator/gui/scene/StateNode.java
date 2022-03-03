package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.simulator.State;
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
    private MenuItem deleteMenuItem;

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

    public SimulationPane getSimulatorParent()
    {
        return ((SimulationPane) getParent());
    }
    public State getState()
    {
        return state;
    }

    protected void bindSimulationPane(SimulationPane simulationPane)
    {
        deleteMenuItem.disableProperty().bind(simulationPane.getSimulationProperty());
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
                        getSimulatorParent().bindEditPane(this);
                        break;

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
            if (event.isPrimaryButtonDown() && getSimulatorParent().getTool() == SimulationPane.Tool.DRAG)
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
            if (event.isPrimaryButtonDown() && getSimulatorParent().getTool() == SimulationPane.Tool.LINK)
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
            if (getParent() instanceof SimulationPane && event.getGestureSource() instanceof StateNode)
                ((SimulationPane) getParent()).createLink(event.getDragboard().getString(), state.getName());
        });
    }

    private ContextMenu createContextMenu()
    {
        ContextMenu menu = new ContextMenu();

        deleteMenuItem = new MenuItem();
        Strings.bind("delete", deleteMenuItem.textProperty());
        deleteMenuItem.setOnAction(event -> ((SimulationPane) getParent()).deleteNode(this));
        //delete.disableProperty().bind(getSimulatorParent().getSimulationProperty());
        menu.getItems().add(deleteMenuItem);

        return menu;
    }

    private static class Position
    {
        double x;
        double y;
    }

}
