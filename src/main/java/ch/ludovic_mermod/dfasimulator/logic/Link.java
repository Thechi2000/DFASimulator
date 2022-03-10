package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.scene.Edge;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;

import java.util.Set;
import java.util.TreeSet;

public class Link
{
    private final ObjectProperty<State> source, target;
    private final SetProperty<Character> alphabetProperty;
    private final Edge edge;

    public Link(State source, State target, Set<Character> alphabetProperty, Simulation simulation)
    {
        this.source = new SimpleObjectProperty<>(source);
        this.target = new SimpleObjectProperty<>(target);
        this.alphabetProperty = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>(alphabetProperty)));
        edge = new Edge(this, simulation.getGraphPane());
    }

    public static Link fromJSONObject(JsonObject object, Simulation simulation)
    {
        Set<Character> alphabet = new TreeSet<>();
        JsonArray alphabetArray = object.get("alphabet").getAsJsonArray();
        for (int i = 0; i < alphabetArray.size(); ++i)
            alphabet.add(alphabetArray.get(i).getAsString().charAt(0));

        return new Link(
                simulation.getState(object.get("source_name").getAsString()),
                simulation.getState(object.get("target_name").getAsString()),
                alphabet,
                simulation);
    }
    public JsonElement toJSONObject()
    {
        JsonObject object = new JsonObject();
        object.addProperty("source_name", source.get().getName());
        object.addProperty("target_name", target.get().getName());

        JsonArray alphabetArray = new JsonArray();
        alphabetProperty.forEach(c -> alphabetArray.add(c.toString()));

        object.add("alphabet", alphabetArray);
        return object;
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
