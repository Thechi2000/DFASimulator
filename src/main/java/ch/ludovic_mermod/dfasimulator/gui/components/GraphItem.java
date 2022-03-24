package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.gui.GraphPane;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;

public class GraphItem extends Group
{
    protected final GraphPane graphPane;

    public GraphItem(GraphPane graphPane)
    {
        this.graphPane = graphPane;
    }

    public void onMousePressed(MouseEvent event)
    {
        if (contains(event.getX(), event.getY()))
        {
            graphPane.grantFocus(this);
            event.consume();
        }
    }

    public BooleanBinding focusProperty()
    {
        return graphPane.focusedItemProperty().isEqualTo(this);
    }
    public boolean hasFocus() {return graphPane.focusedItemProperty().get().equals(this);}
}
