package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.gui.GraphPane;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Group;

public class GraphItem extends Group
{
    protected final GraphPane graphPane;

    public GraphItem(GraphPane graphPane) {this.graphPane = graphPane;}

    public BooleanBinding focusProperty()
    {
        return graphPane.focusedItemProperty().isEqualTo(this);
    }
}
