package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.PropertiesMap;
import ch.ludovic_mermod.dfasimulator.gui.scene.components.Node;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONNull;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.MapChangeListener;

import java.util.Objects;

public class State
{
    private final JSONObject jsonObject;

    private final Node node;
    private final StringProperty name;
    private final BooleanBinding isInitialBinding;
    private final BooleanProperty isAcceptingProperty;
    private final PropertiesMap<Character, State> transitionMapProperty;
    private final FiniteAutomaton finiteAutomaton;

    public State(FiniteAutomaton finiteAutomaton)
    {
        this.finiteAutomaton = finiteAutomaton;
        jsonObject = new JSONObject();

        name = new SimpleStringProperty(this, "name", "");
        isAcceptingProperty = new SimpleBooleanProperty(this, "isAccepting", false);
        transitionMapProperty = new PropertiesMap<>();

        isInitialBinding = new BooleanBinding()
        {
            {
                super.bind(finiteAutomaton.initialStateProperty(), name);
            }

            @Override
            protected boolean computeValue()
            {
                return finiteAutomaton.initialState() != null && name().equals(finiteAutomaton.initialState().name());
            }
        };

        node = new Node(this, finiteAutomaton.getMainPane().getGraphPane());

        jsonObject.addProperty("name", name);
        jsonObject.addProperty("isAccepting", isAcceptingProperty);
        jsonObject.addProperty("x_coord", node.layoutXProperty());
        jsonObject.addProperty("y_coord", node.layoutYProperty());
        jsonObject.add("transitionMap", new JSONObject());

        transitionMapProperty.addListener((p, k, o, n) ->
        {
            if (n != null)
                jsonObject.getAsJSONObject("transitionMap").addProperty(k.toString(), n.nameProperty());
            else jsonObject.getAsJSONObject("transitionMap").add(k.toString(), JSONNull.INSTANCE);
        });
        transitionMapProperty.addListener((MapChangeListener<? super Character, ? super ObjectProperty<State>>) change ->
        {
            if (change.wasRemoved())
                jsonObject.getAsJSONObject("transitionMap").remove(change.getKey().toString());
        });
    }

    public static State fromJSONObject(JSONObject jsonObject, FiniteAutomaton finiteAutomaton)
    {
        State state = new State(finiteAutomaton);
        state.name.set(jsonObject.get("name").getAsString());
        state.isAcceptingProperty.set(jsonObject.get("isAccepting").getAsBoolean());
        state.node.relocate(jsonObject.get("x_coord").getAsDouble(), jsonObject.get("y_coord").getAsDouble());
        return state;
    }
    public void loadTransitionMap(JSONObject jsonObject)
    {
        jsonObject.entrySet().forEach(e -> transitionMapProperty.setValue(e.getKey().charAt(0), finiteAutomaton.getState(e.getValue().getAsString())));
    }

    public JSONElement getJSONObject()
    {
        return jsonObject;
    }

    public String name()
    {
        return name.get();
    }
    public StringProperty nameProperty()
    {
        return name;
    }
    public BooleanBinding isInitialBinding()
    {
        return isInitialBinding;
    }
    public BooleanProperty isAcceptingProperty()
    {
        return isAcceptingProperty;
    }
    public boolean isAccepting()
    {
        return isAcceptingProperty.get();
    }
    public PropertiesMap<Character, State> transitionMap()
    {
        return transitionMapProperty;
    }

    public Node getNode()
    {
        return node;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name.get());
    }
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(name.get(), state.name.get());
    }
    @Override
    public String toString()
    {
        return "State{" +
                "name=" + name.get() +
                '}';
    }
}
