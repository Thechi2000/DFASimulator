package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.scene.components.Edge;
import ch.ludovic_mermod.dfasimulator.json.JSONArray;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Link
{
    private final JSONObject jsonObject;
    private final ObjectProperty<State> source, target;
    private final SetProperty<Character> alphabetProperty;
    private final Edge edge;

    public Link(State source, State target, Set<Character> alphabet, Simulation simulation)
    {
        jsonObject = new JSONObject();

        this.source = new SimpleObjectProperty<>(source);
        this.target = new SimpleObjectProperty<>(target);
        this.alphabetProperty = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>(alphabet)));
        edge = new Edge(this, simulation.getGraphPane());

        jsonObject.addProperty("source_name", this.source.get().nameProperty());
        jsonObject.addProperty("target_name", this.target.get().nameProperty());
        jsonObject.add("alphabet", alphabet.stream().map(Object::toString).collect(JSONArray::new, JSONArray::add, JSONArray::addAll));

        alphabetProperty.addListener((o, ov, nv) -> jsonObject.add("alphabet", alphabetProperty.stream().map(Object::toString).collect(JSONArray::new, JSONArray::add, JSONArray::addAll)));
    }

    public static Link fromJSONObject(JSONObject object, Simulation simulation)
    {
        return new Link(
                simulation.getState(object.get("source_name").getAsString()),
                simulation.getState(object.get("target_name").getAsString()),
                object.get("alphabet").getAsJSONArray().stream().map(e -> e.getAsString().charAt(0)).collect(Collectors.toSet()),
                simulation);
    }
    public JSONObject toJSONObject()
    {
        return jsonObject;
    }

    public SetProperty<Character> alphabetProperty()
    {
        return alphabetProperty;
    }
    public ObjectProperty<State> source()
    {
        return source;
    }
    public ObjectProperty<State> target()
    {
        return target;
    }
    public Edge getEdge()
    {
        return edge;
    }
}
