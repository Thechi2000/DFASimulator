package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.gui.GraphPane;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;

/**
 * Super class for the main components of a Graph
 * Has a special MouseEvent handling, to have a more flexible management of the focus
 */
public class GraphItem extends Group
{
    protected final GraphPane graphPane;

    public GraphItem(GraphPane graphPane)
    {
        this.graphPane = graphPane;
    }

    /**
     * Called when a MousePressedEvent is registered on the GraphPane
     * Must check if this item is concerned by the click and consume the event if it is
     * @param event the MouseEvent
     */
    public void onMousePressed(MouseEvent event)
    {
        if (contains(event.getX(), event.getY()))
        {
            graphPane.grantFocus(this);
            event.consume();
        }
    }

    protected BooleanBinding focusProperty()
    {
        return graphPane.focusedItemProperty().isEqualTo(this);
    }
    protected boolean hasFocus() {return graphPane.focusedItemProperty().get().equals(this);}
}
