package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.MainPane;
import ch.ludovic_mermod.dfasimulator.gui.components.Edge;
import ch.ludovic_mermod.dfasimulator.json.JSONArray;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.json.JSONPrimitive;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.*;

import java.util.Objects;
import java.util.TreeSet;

public class FiniteAutomaton
{
    public static final String JSON_ALPHABET = "alphabet";
    public static final String JSON_INITIAL  = "initial";
    public static final String JSON_STATES   = "states";

    private final JSONObject jsonObject;

    private final ObjectProperty<State>    initialState;
    private final ListProperty<State>      states;
    private final ObservableSet<Character> alphabet;
    private       MainPane                 mainPane;

    public FiniteAutomaton()
    {
        initialState = new SimpleObjectProperty<>(this, "initialState", null);
        states = new SimpleListProperty<>(this, "states", FXCollections.observableArrayList());
        alphabet = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>()));

        initialState.addListener((o, ov, nv) ->
        {
            if (!states.contains(nv) && !states.isEmpty())
                Platform.runLater(() -> initialState.set(ov));
        });

        states.addListener((ListChangeListener<? super State>) change ->
        {
            change.next();
            if (change.wasAdded())
                change.getAddedSubList().stream()
                        .filter(Objects::nonNull)
                        .forEach(s -> s.nameProperty().addListener((o, ov, nv) -> validateNameChange(s, ov)));
        });

        alphabet.addListener((SetChangeListener<? super Character>) change ->
        {
            if (change.wasAdded())
                states.forEach(s -> s.transitionMap().setValue(change.getElementAdded(), null));

            if (change.wasRemoved())
                states.forEach(s -> s.transitionMap().remove(change.getElementRemoved()));
        });

        jsonObject = new JSONObject();
        jsonObject.add("states", JSONArray.fromObservableList(states, State::getJSONObject));
        jsonObject.add(JSON_ALPHABET, JSONArray.fromObservableSet(alphabet, c -> new JSONPrimitive(c.toString())));
        jsonObject.addProperty(JSON_INITIAL, new StringBinding()
        {
            {
                super.bind(initialState);
                initialState.addListener((o, ov, nv) -> {
                    if (ov != null) unbind(ov.nameProperty());
                    if (nv != null) bind(nv.nameProperty());
                });
            }

            @Override
            protected String computeValue()
            {
                return initialState.get() == null ? "null" : initialState.get().name();
            }
        });
    }

    public void create(MainPane mainPane)
    {
        this.mainPane = mainPane;
        jsonObject.add("edges", JSONArray.fromObservableSet(mainPane.getGraphPane().edges(), Edge::getJSONObject));
    }

    public State initialState()
    {
        return initialState.get();
    }
    public ObjectProperty<State> initialStateProperty()
    {
        return initialState;
    }
    public ObservableList<State> states()
    {
        return states.get();
    }
    public ListProperty<State> statesProperty()
    {
        return states;
    }
    public ObservableSet<Character> alphabet()
    {
        return alphabet;
    }

    public Binding<Boolean> hasBinaryAlphabet()
    {
        return CustomBindings.create(() -> alphabet.size() == 2 && alphabet.contains('0') && alphabet.contains('1'), alphabet);
    }

    private void validateNameChange(State state, String oldName)
    {
        if (states.stream().anyMatch(s -> s != state && s.name().equals(state.name())))
            Platform.runLater(() -> state.nameProperty().set(oldName));
    }

    public void addState(String name)
    {
        State state = new State(this);
        state.nameProperty().set(name);
        addState(state);
    }
    public void addState(State state)
    {
        states.add(state);
        mainPane.getGraphPane().addState(state);
    }
    public void removeState(State state)
    {
        states.stream()
                .filter(s -> !state.equals(s))
                .forEach(s ->
                        s.transitionMap().forEach((key, value) ->
                        {
                            if (state.equals(value.get()))
                                s.transitionMap().setValue(key, null);
                        }));
        states.remove(state);
        mainPane.getGraphPane().removeState(state);
    }
    public void createNode(double x, double y)
    {
        State state = new State(this);
        addState(state);
        state.getNode().relocate(x, y);
    }
    public void clear()
    {
        initialState.set(null);
        alphabet.clear();
        states.clear();
        mainPane.getGraphPane().children().clear();
        mainPane.getGraphPane().edges().clear();
    }

    public MainPane getMainPane()
    {
        return mainPane;
    }

    public JSONElement getJSONObject()
    {
        return jsonObject;
    }

    public State getState(String name)
    {
        return states.stream().filter(s -> s.name().equals(name)).findAny().orElse(null);
    }
    public boolean hasState(String name)
    {
        return states.stream().anyMatch(s -> s.name().equals(name));
    }

    public void loadJSON(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasArray(JSON_STATES);
        object.checkHasString(JSON_INITIAL);
        object.checkHasArray(JSON_ALPHABET);

        var nodesArray = object.get(JSON_STATES).getAsJSONArray();

        clear();

        for (JSONElement e : object.getAsJSONArray(JSON_ALPHABET))
        {
            if (!e.isJSONPrimitive() || !e.getAsJSONPrimitive().isString() || e.getAsJSONPrimitive().getAsString().length() != 1)
                throw new IOManager.CorruptedFileException("Could not convert \"%s\" to a character", e);
            alphabet().add(e.getAsString().charAt(0));
        }

        for (JSONElement jsonElement : nodesArray)
            addState(State.fromJSONObject(jsonElement.getAsJSONObject(), this));
        initialStateProperty().set(getState(object.get(JSON_INITIAL).getAsString()));

        for (JSONElement e : nodesArray)
            getState(e.getAsJSONObject().get(State.JSON_NAME).getAsString()).loadTransitionMap(e.getAsJSONObject().get(State.JSON_TRANSITION_MAP).getAsJSONObject());
    }
}
