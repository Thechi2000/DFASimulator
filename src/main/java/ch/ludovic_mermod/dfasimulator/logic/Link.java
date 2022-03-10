package ch.ludovic_mermod.dfasimulator.logic;

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

    public Link(State source, State target, Set<Character> alphabetProperty)
    {
        this.source = new SimpleObjectProperty<>(source);
        this.target = new SimpleObjectProperty<>(target);
        this.alphabetProperty = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>(alphabetProperty)));
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
}
