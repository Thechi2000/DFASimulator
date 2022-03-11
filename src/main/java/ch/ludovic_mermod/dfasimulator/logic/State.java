package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.JsonObjectProperty;
import ch.ludovic_mermod.dfasimulator.gui.scene.Node;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

public class State
{
    private final JSONObject jsonObject;
    private final ListProperty<Link> outgoingLinksProperty;
    private final StringProperty nameProperty;
    private final BooleanProperty initialProperty, acceptingProperty;
    private final Node node;

    public State(String name, Simulation simulation)
    {
        jsonObject = new JSONObject();

        outgoingLinksProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
        nameProperty = new SimpleStringProperty(name);
        initialProperty = new SimpleBooleanProperty(false);
        acceptingProperty = new SimpleBooleanProperty(false);
        this.node = new Node(this, simulation.getGraphPane());


        jsonObject.addProperty("name", nameProperty);
        jsonObject.addProperty("initial", initialProperty);
        jsonObject.addProperty("accepting", acceptingProperty);
        jsonObject.addProperty("x_coord", node.layoutXProperty());
        jsonObject.addProperty("y_coord", node.layoutYProperty());

        /*nameProperty.addListener((o, ov, nv) -> jsonObject.addProperty("name", nv));
        initialProperty.addListener((o, ov, nv) -> jsonObject.addProperty("initial", nv));
        acceptingProperty.addListener((o, ov, nv) -> jsonObject.addProperty("accepting", nv));
        node.layoutXProperty().addListener((o, ov, nv) -> jsonObject.addProperty("x_coord", nv));
        node.layoutYProperty().addListener((o, ov, nv) -> jsonObject.addProperty("y_coord", nv));*/
    }

    public static State fromJSONObject(JSONObject object, Simulation simulation)
    {
        State state = new State(object.get("name").getAsString(), simulation);
        state.initialProperty.set(object.get("initial").getAsBoolean());
        state.acceptingProperty.set(object.get("accepting").getAsBoolean());
        state.getNode().relocate(object.get("x_coord").getAsDouble(), object.get("y_coord").getAsDouble());
        return state;
    }
    public JSONObject toJSONObject()
    {
        return jsonObject;
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
