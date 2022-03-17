package ch.ludovic_mermod.dfasimulator.json;

import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import com.google.gson.JsonObject;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class JSONObject extends JSONElement implements Observable
{
    private final Set<PropertyChangeListener> propertyChangeListeners = new HashSet<>();
    private final Set<ChildUpdateListener>    childUpdateListeners    = new HashSet<>();

    private final Set<SetChangeListener<? super Map.Entry<String, JSONElement>>> setChangeListeners = new HashSet<>();

    private final TreeMap<String, JSONElement> members = new TreeMap<>();

    public JSONObject()
    {
    }

    public JSONObject(JsonObject obj)
    {
        obj.entrySet().forEach(e -> add(e.getKey(), JSONElement.convert(e.getValue())));
    }

    public JSONObject deepCopy()
    {
        JSONObject result = new JSONObject();

        for (Map.Entry<String, JSONElement> entry : members.entrySet())
        {
            result.add(entry.getKey(), entry.getValue().deepCopy());
        }

        return result;
    }
    public JSONElement remove(String property)
    {
        if (members.get(property) instanceof ObservableStringValue)
            ((ObservableStringValue) members.get(property)).removeListener((o, ov, nv) -> addProperty(property, nv));
        if (members.get(property) instanceof ObservableNumberValue)
            ((ObservableNumberValue) members.get(property)).removeListener((o, ov, nv) -> addProperty(property, nv));
        if (members.get(property) instanceof ObservableBooleanValue)
            ((ObservableBooleanValue) members.get(property)).removeListener((o, ov, nv) -> addProperty(property, nv));

        JSONElement remove = members.remove(property);

        setChangeListeners.forEach(l -> l.onChanged(new SetChangeListener.Change<>(FXCollections.observableSet(entrySet()))
        {
            @Override
            public boolean wasAdded()
            {
                return false;
            }
            @Override
            public boolean wasRemoved()
            {
                return true;
            }
            @Override
            public Map.Entry<String, JSONElement> getElementAdded()
            {
                return null;
            }
            @Override
            public Map.Entry<String, JSONElement> getElementRemoved()
            {
                return Map.entry(property, members.get(property));
            }
        }));
        childUpdateListeners.forEach(l -> l.onChildUpdate(this));

        return remove;
    }
    public void add(String property, JSONElement value)
    {
        bindListeners(value, this, childUpdateListeners);

        members.put(property, value == null ? JSONNull.INSTANCE : value);

        if (!members.containsKey(property))
            setChangeListeners.forEach(l -> l.onChanged(new SetChangeListener.Change<>(FXCollections.observableSet(entrySet()))
            {
                @Override
                public boolean wasAdded()
                {
                    return true;
                }
                @Override
                public boolean wasRemoved()
                {
                    return false;
                }
                @Override
                public Map.Entry<String, JSONElement> getElementAdded()
                {
                    return Map.entry(property, value);
                }
                @Override
                public Map.Entry<String, JSONElement> getElementRemoved()
                {
                    return null;
                }
            }));
        else propertyChangeListeners.forEach(l -> l.onPropertyChange(property, members.get(property), value));
        childUpdateListeners.forEach(l -> l.onChildUpdate(this));
    }

    public boolean hasString(String property)
    {
        return has(property) && get(property).isJSONPrimitive() && get(property).getAsJSONPrimitive().isString();
    }
    public boolean hasNumber(String property)
    {
        return has(property) && get(property).isJSONPrimitive() && get(property).getAsJSONPrimitive().isNumber();
    }
    public boolean hasArray(String property)
    {
        return has(property) && get(property).isJSONArray();
    }
    public boolean hasBoolean(String property)
    {
        return has(property) && get(property).isJSONPrimitive() && get(property).getAsJSONPrimitive().isBoolean();
    }
    public boolean hasObject(String property)
    {
        return has(property) && get(property).isJSONObject();
    }

    public void checkHasString(String property) throws IOManager.CorruptedFileException
    {
        if (!hasString(property)) missingProperty(property, "String");
    }
    public void checkHasNumber(String property) throws IOManager.CorruptedFileException
    {
        if (!hasNumber(property)) missingProperty(property, "Number");
    }
    public void checkHasArray(String property) throws IOManager.CorruptedFileException
    {
        if (!hasArray(property)) missingProperty(property, "JSONArray");
    }
    public void checkHasBoolean(String property) throws IOManager.CorruptedFileException
    {
        if (!hasBoolean(property)) missingProperty(property, "Boolean");
    }
    public void checkHasObject(String property) throws IOManager.CorruptedFileException
    {
        if (!hasObject(property)) missingProperty(property, "JSONObject");
    }
    private void missingProperty(String name, String type) throws IOManager.CorruptedFileException
    {
        throw new IOManager.CorruptedFileException(String.format("Could not find %s(%s) in \"%s\"", name, type, toString()));
    }

    public void addProperty(String property, String value)
    {
        add(property, value == null ? JSONNull.INSTANCE : new JSONPrimitive(value));
    }
    public void addProperty(String property, Number value)
    {
        add(property, value == null ? JSONNull.INSTANCE : new JSONPrimitive(value));
    }
    public void addProperty(String property, Boolean value)
    {
        add(property, value == null ? JSONNull.INSTANCE : new JSONPrimitive(value));
    }
    public void addProperty(String property, ObservableStringValue value)
    {
        addProperty(property, value.getValue());
        value.addListener((o, ov, nv) -> addProperty(property, nv));
    }
    public void addProperty(String property, ObservableNumberValue value)
    {
        addProperty(property, value.getValue());
        value.addListener((o, ov, nv) -> addProperty(property, nv));
    }
    public void addProperty(String property, ObservableBooleanValue value)
    {
        addProperty(property, value.getValue());
        value.addListener((o, ov, nv) -> addProperty(property, nv));
    }
    public Set<Map.Entry<String, JSONElement>> entrySet()
    {
        return members.entrySet();
    }
    public Set<String> keySet()
    {
        return members.keySet();
    }
    public boolean has(String memberName)
    {
        return members.containsKey(memberName);
    }
    public JSONElement get(String memberName)
    {
        return members.get(memberName);
    }
    public JSONPrimitive getAsJSONPrimitive(String memberName)
    {
        return (JSONPrimitive) members.get(memberName);
    }
    public JSONArray getAsJSONArray(String memberName)
    {
        return (JSONArray) members.get(memberName);
    }
    public JSONObject getAsJSONObject(String memberName)
    {
        return (JSONObject) members.get(memberName);
    }
    public int hashCode()
    {
        return members.hashCode();
    }
    public boolean equals(Object o)
    {
        return o == this || o instanceof JSONObject && ((JSONObject) o).members.equals(members);
    }
    @Override
    public String toString()
    {
        return members.entrySet().stream().map(e -> String.format("\"%s\": %s", e.getKey(), e.getValue().toString())).collect(Collectors.joining(", ", "{", "}"));
    }
    public void addListener(PropertyChangeListener propertyChangeListener)
    {
        propertyChangeListeners.add(propertyChangeListener);
    }
    public void removeListener(PropertyChangeListener propertyChangeListener)
    {
        propertyChangeListeners.remove(propertyChangeListener);
    }

    public void addListener(ChildUpdateListener propertyChangeListener)
    {
        childUpdateListeners.add(propertyChangeListener);
    }
    public void removeListener(ChildUpdateListener propertyChangeListener)
    {
        childUpdateListeners.remove(propertyChangeListener);
    }

    public void addListener(SetChangeListener<? super Map.Entry<String, JSONElement>> setChangeListener)
    {
        setChangeListeners.add(setChangeListener);
    }
    public void removeListener(SetChangeListener<? super Map.Entry<String, JSONElement>> setChangeListener)
    {
        setChangeListeners.remove(setChangeListener);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener)
    {
    }
    @Override
    public void removeListener(InvalidationListener invalidationListener)
    {
    }

    @FunctionalInterface
    public interface PropertyChangeListener
    {
        void onPropertyChange(String property, JSONElement oldValue, JSONElement newValue);
    }
}
