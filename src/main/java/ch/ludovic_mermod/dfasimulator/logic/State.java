package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.scene.Node;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

public class State
{
    private final ListProperty<Link> outgoingLinksProperty;
    private final StringProperty nameProperty;
    private final BooleanProperty initialProperty, acceptingProperty;
    private final Node node;

    public State(String name, Simulation simulation)
    {
        outgoingLinksProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        nameProperty = new SimpleStringProperty(name);
        initialProperty = new SimpleBooleanProperty(false);
        acceptingProperty = new SimpleBooleanProperty(false);
        this.node = new Node(this, simulation.getGraphPane());
    }

    public static State fromJSONObject(JsonObject object, Simulation simulation)
    {
        State state = new State(object.get("name").getAsString(), simulation);
        state.initialProperty.set(object.get("initial").getAsBoolean());
        state.acceptingProperty.set(object.get("accepting").getAsBoolean());
        state.getNode().relocate(object.get("x_coord").getAsDouble(), object.get("y_coord").getAsDouble());
        return state;
    }
    public JsonElement toJSONObject()
    {
        JsonObject object = new JsonObject();
        object.addProperty("name", nameProperty.get());
        object.addProperty("initial", initialProperty.get());
        object.addProperty("accepting", acceptingProperty.get());
        object.addProperty("x_coord", node.getLayoutX());
        object.addProperty("y_coord", node.getLayoutY());
        return object;
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
