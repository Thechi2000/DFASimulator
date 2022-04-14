package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public class Sentinel extends Element
{
    private final ObjectProperty<Element> element;

    public Sentinel(Element e)
    {
        element = new SimpleObjectProperty<>(e);
        element.get().parent = this;
    }

    @Override
    protected void update(Element oldValue, Element newValue)
    {
        if (element.get() == oldValue) element.set(newValue);
        else throw new IllegalArgumentException();
    }

    @Override
    public void add(Item i, double x, double y)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Element remove(Item i)
    {
        element.set(element.get().remove(i));
        return this;
    }

    @Override
    public Parent getContent()
    {
        return element.get() == null ? new Pane() : element.get().getContent();
    }
    public ObservableValue<Parent> getContentBinding()
    {
        return CustomBindings.create(() -> element.get() == null ? new Pane() : element.get().getContent(), element);
    }

    @Override
    public JSONObject getJSONObject()
    {
        return element == null ? new JSONObject() : element.get().getJSONObject();
    }
}
