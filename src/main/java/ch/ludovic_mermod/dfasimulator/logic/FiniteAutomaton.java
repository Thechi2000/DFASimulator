package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.scene.MainPane;
import ch.ludovic_mermod.dfasimulator.json.JSONArray;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;

import java.util.Objects;
import java.util.TreeSet;

public class FiniteAutomaton
{
    private final JSONObject jsonObject;

    private final ObjectProperty<State> initialState;
    private final ListProperty<State> states;
    private final SetProperty<Character> alphabet;
    private final MainPane mainPane;

    public FiniteAutomaton(MainPane mainPane)
    {
        jsonObject = new JSONObject();
        jsonObject.add("states", new JSONArray());

        this.mainPane = mainPane;
        initialState = new SimpleObjectProperty<>(this, "initialState", null);
        states = new SimpleListProperty<>(this, "states", FXCollections.observableArrayList());
        alphabet = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>()));

        initialState.addListener((o, ov, nv) ->
        {
            if (!states.contains(nv))
                Platform.runLater(() -> initialState.set(ov));
        });

        states.addListener((ListChangeListener<? super State>) change ->
        {
            change.next();
            if (change.wasAdded())
                change.getAddedSubList().stream()
                        .filter(Objects::nonNull)
                        .forEach(s ->
                        {
                            s.nameProperty().addListener((o, ov, nv) -> validateNameChange(s, ov));
                            jsonObject.getAsJSONArray("states").add(s.getJSONObject());
                        });

            if (change.wasRemoved())
                change.getRemoved().stream()
                        .filter(Objects::nonNull)
                        .forEach(s -> jsonObject.getAsJSONArray("states").remove(s.getJSONObject()));
        });

        alphabet.addListener((SetChangeListener<? super Character>) change ->
        {
            if (change.wasAdded())
                states.forEach(s -> s.transitionMap().put(change.getElementAdded(), null));

            if (change.wasRemoved())
                states.forEach(s -> s.transitionMap().remove(change.getElementRemoved()));
        });
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
        return alphabet.get();
    }
    public SetProperty<Character> alphabetProperty()
    {
        return alphabet;
    }

    private void validateNameChange(State state, String oldName)
    {
        if (states.stream().anyMatch(s -> s != state && s.name().equals(state.name())))
            Platform.runLater(() -> state.nameProperty().set(oldName));
    }

    public void addState(State state)
    {
        states.add(state);
        mainPane.getGraphPane().addState(state);
    }
    public void removeState(State state)
    {
        states.stream()
                .filter(s -> !s.name().equals(state.name()))
                .forEach(s ->
                {
                    s.transitionMap().forEach((key, value) ->
                    {
                        if (value.get().name().equals(state.name()))
                            s.transitionMap().put(key, null);
                    });
                });
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
}
