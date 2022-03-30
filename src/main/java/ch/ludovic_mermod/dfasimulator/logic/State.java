package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.components.Node;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONNull;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.utils.PropertiesMap;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;
import java.util.Objects;

public class State
{
    protected static final String JSON_TRANSITION_MAP = "transition_map";
    protected static final String JSON_IS_ACCEPTING   = "is_accepting";
    protected static final String JSON_NAME           = "name";

    private final JSONObject jsonObject;

    private final StringProperty                  name;
    private final BooleanBinding                  isInitialBinding;
    private final BooleanProperty                 isAcceptingProperty;
    private final PropertiesMap<Character, State> transitionMapProperty;

    private final Node            node;
    private final FiniteAutomaton finiteAutomaton;

    /**
     * Construct a State for a finite automaton
     * @param finiteAutomaton the parent automaton
     */
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
        jsonObject.add(JSON_TRANSITION_MAP, new JSONObject());

        finiteAutomaton.alphabet().forEach(transitionMapProperty::get);

        transitionMapProperty.addListener((p, k, o, n) ->
        {
            if (n != null)
                jsonObject.getAsJSONObject(JSON_TRANSITION_MAP).addProperty(k.toString(), n.nameProperty());
            else jsonObject.getAsJSONObject(JSON_TRANSITION_MAP).add(k.toString(), JSONNull.INSTANCE);
        });
        transitionMapProperty.addListener((k, p) -> jsonObject.getAsJSONObject(JSON_TRANSITION_MAP).remove(String.valueOf(k)));
    }

    /**
     * Creates a State from a JSONObject
     * The transition map is not loaded, it must be with the loadTransitionMap method
     * @param jsonObject the jsonObject containing the information for the state
     * @param finiteAutomaton the parent automaton
     * @return the new State
     * @throws IOManager.CorruptedFileException when the jsonObject does not hold the correct values
     */
    public static State fromJSONObject(JSONObject jsonObject, FiniteAutomaton finiteAutomaton) throws IOManager.CorruptedFileException
    {
        jsonObject.checkHasString(JSON_NAME);
        jsonObject.checkHasBoolean(JSON_IS_ACCEPTING);
        jsonObject.checkHasObject(JSON_TRANSITION_MAP);

        State state = new State(finiteAutomaton);
        state.name.set(jsonObject.get(JSON_NAME).getAsString());
        state.isAcceptingProperty.set(jsonObject.get(JSON_IS_ACCEPTING).getAsBoolean());
        return state;
    }

    /**
     * Load a transition map from a jsonObject
     * @param jsonObject a jsonObject containing the transition map
     * @throws IOManager.CorruptedFileException when the jsonObject is invalid
     */
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
