package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.utils.PropertiesMap;
import ch.ludovic_mermod.dfasimulator.gui.scene.components.Node;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONNull;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.MapChangeListener;

import java.util.Map;
import java.util.Objects;

public class State
{
    protected static final String JSON_TRANSITION_MAP = "transition_map";
    protected static final String JSON_X_COORD        = "y_coord";
    protected static final String JSON_Y_COORD        = "x_coord";
    protected static final String JSON_IS_ACCEPTING   = "is_accepting";
    protected static final String JSON_NAME           = "name";

    private final JSONObject jsonObject;

    private final StringProperty                  name;
    private final BooleanBinding                  isInitialBinding;
    private final BooleanProperty                 isAcceptingProperty;
    private final PropertiesMap<Character, State> transitionMapProperty;

    private final Node            node;
    private final FiniteAutomaton finiteAutomaton;

    public State(FiniteAutomaton finiteAutomaton)
    {
        this.finiteAutomaton = finiteAutomaton;
        jsonObject = new JSONObject();

        name = new SimpleStringProperty(this, JSON_NAME, "");
        isAcceptingProperty = new SimpleBooleanProperty(this, JSON_IS_ACCEPTING, false);
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

        jsonObject.addProperty(JSON_NAME, name);
        jsonObject.addProperty(JSON_IS_ACCEPTING, isAcceptingProperty);
        jsonObject.addProperty(JSON_X_COORD, node.layoutXProperty());
        jsonObject.addProperty(JSON_Y_COORD, node.layoutYProperty());
        jsonObject.add(JSON_TRANSITION_MAP, new JSONObject());

        finiteAutomaton.alphabet().forEach(transitionMapProperty::get);

        transitionMapProperty.addListener((p, k, o, n) ->
        {
            if (n != null)
                jsonObject.getAsJSONObject(JSON_TRANSITION_MAP).addProperty(k.toString(), n.nameProperty());
            else jsonObject.getAsJSONObject(JSON_TRANSITION_MAP).add(k.toString(), JSONNull.INSTANCE);
        });
        transitionMapProperty.addListener((MapChangeListener<? super Character, ? super ObjectProperty<State>>) change ->
        {
            if (change.wasRemoved())
                jsonObject.getAsJSONObject(JSON_TRANSITION_MAP).remove(change.getKey().toString());
        });
    }

    public static State fromJSONObject(JSONObject jsonObject, FiniteAutomaton finiteAutomaton) throws IOManager.CorruptedFileException
    {
        jsonObject.checkHasString(JSON_NAME);
        jsonObject.checkHasBoolean(JSON_IS_ACCEPTING);
        jsonObject.checkHasNumber(JSON_Y_COORD);
        jsonObject.checkHasNumber(JSON_X_COORD);
        jsonObject.checkHasObject(JSON_TRANSITION_MAP);

        State state = new State(finiteAutomaton);
        state.name.set(jsonObject.get(JSON_NAME).getAsString());
        state.isAcceptingProperty.set(jsonObject.get(JSON_IS_ACCEPTING).getAsBoolean());
        state.node.relocate(jsonObject.get(State.JSON_X_COORD).getAsDouble(), jsonObject.get(JSON_Y_COORD).getAsDouble());
        return state;
    }
    public void loadTransitionMap(JSONObject jsonObject) throws IOManager.CorruptedFileException
    {
        for (Map.Entry<String, JSONElement> e : jsonObject.entrySet())
        {
            String key = e.getKey();

            if (key.length() != 1
                || !jsonObject.has(key)
                || !(jsonObject.get(key).isJSONNull() || jsonObject.hasString(key)))
                throw new IOManager.CorruptedFileException("Could not parse \"%s\" into a transition map", jsonObject);


            transitionMapProperty.setValue(key.charAt(0), e.getValue().isJSONNull() ? null : finiteAutomaton.getState(e.getValue().getAsString()));
        }
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
