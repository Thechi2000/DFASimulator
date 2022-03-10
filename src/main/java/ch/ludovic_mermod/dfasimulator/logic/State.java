package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.scene.Node;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

public class State
{
    private final ListProperty<Link> outgoingLinksProperty;
    private final StringProperty nameProperty;
    private final BooleanProperty initialProperty, acceptingProperty;
    private final Node node;

    public State(String name, Node node)
    {
        this.node = node;
        outgoingLinksProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        nameProperty = new SimpleStringProperty(name);
        initialProperty = new SimpleBooleanProperty(false);
        acceptingProperty = new SimpleBooleanProperty(false);
    }

    public Node getNode()
    {
        return node;
    }

    public void addLink(Link link)
    {
        outgoingLinksProperty.add(link);
    }
    public void removeLink(Link link)
    {
        outgoingLinksProperty.remove(link);
    }

    public StringProperty nameProperty()
    {
        return nameProperty;
    }
    public ListProperty<Link> outgoingLinksProperty()
    {
        return outgoingLinksProperty;
    }
    public BooleanProperty initialProperty()
    {
        return initialProperty;
    }
    public BooleanProperty acceptingProperty()
    {
        return acceptingProperty;
    }
    public String getName()
    {
        return nameProperty.get();
    }
}
