package ch.ludovic_mermod.dfasimulator.json;

import com.google.gson.JsonArray;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class JSONArray extends JSONElement implements ObservableList<JSONElement>
{
    private final ObservableList<JSONElement> elements = FXCollections.observableArrayList();
    private final Set<ChildUpdateListener> childUpdateListenerSet = new HashSet<>();

    public JSONArray()
    {

    }
    public JSONArray(JsonArray array)
    {
        this();
        array.forEach(e -> add(JSONElement.convert(e)));
    }

    public JSONArray deepCopy()
    {
        return elements.stream().collect(JSONArray::new, JSONArray::add, JSONArray::addAll);
    }
    public boolean getAsBoolean()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsBoolean();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public Number getAsNumber()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsNumber();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public String getAsString()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsString();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public double getAsDouble()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsDouble();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public float getAsFloat()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsFloat();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public long getAsLong()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsLong();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public int getAsInt()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsInt();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public byte getAsByte()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsByte();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public BigDecimal getAsBigDecimal()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsBigDecimal();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public BigInteger getAsBigInteger()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsBigInteger();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public short getAsShort()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsShort();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    @Deprecated
    public char getAsCharacter()
    {
        if (elements.size() == 1)
        {
            return elements.get(0).getAsCharacter();
        }
        else
        {
            throw new IllegalStateException();
        }
    }
    public void add(ObservableBooleanValue bool)
    {
        add(bool.get());
        bool.addListener((o, ov, nv) -> add(nv));
    }
    public void add(ObservableNumberValue number)
    {
        add(number.getValue());
        number.addListener((o, ov, nv) -> add(nv));
    }
    public void add(ObservableStringValue string)
    {
        add(string.get());
        string.addListener((o, ov, nv) -> add(nv));
    }
    public void add(Boolean bool)
    {
        add(bool == null ? JSONNull.INSTANCE : new JSONPrimitive(bool));
    }
    public void add(Character character)
    {
        add(character == null ? JSONNull.INSTANCE : new JSONPrimitive(character));
    }
    public void add(Number number)
    {
        add(number == null ? JSONNull.INSTANCE : new JSONPrimitive(number));
    }
    public void add(String string)
    {
        add(string == null ? JSONNull.INSTANCE : new JSONPrimitive(string));
    }
    public int size()
    {
        return elements.size();
    }
    public boolean isEmpty()
    {
        return elements.isEmpty();
    }
    public boolean contains(Object o)
    {
        return elements.contains(o);
    }
    @Override
    public Iterator<JSONElement> iterator()
    {
        return elements.iterator();
    }
    @Override
    public Object[] toArray()
    {
        return elements.toArray();
    }
    @Override
    public <T> T[] toArray(T[] a)
    {
        return elements.toArray(a);
    }
    public boolean add(JSONElement element)
    {
        if (element == null) element = JSONNull.INSTANCE;
        bindListeners(element, this, childUpdateListenerSet);
        return elements.add(element);
    }
    public boolean remove(Object o)
    {
        if (o instanceof JSONElement) unbindListeners((JSONElement) o, this, childUpdateListenerSet);
        return elements.remove(o);
    }
    @Override
    public boolean containsAll(Collection<?> c)
    {
        return elements.containsAll(c);
    }
    @Override
    public boolean addAll(Collection<? extends JSONElement> c)
    {
        return elements.addAll(c);
    }
    @Override
    public boolean addAll(int index, Collection<? extends JSONElement> c)
    {
        return elements.addAll(index, c);
    }
    @Override
    public boolean removeAll(Collection<?> c)
    {
        return elements.removeAll(c);
    }
    @Override
    public boolean retainAll(Collection<?> c)
    {
        return elements.retainAll(c);
    }
    @Override
    public void clear()
    {
        elements.clear();
    }
    public JSONElement get(int i)
    {
        return elements.get(i);
    }
    public JSONElement set(int index, JSONElement element)
    {
        unbindListeners(elements.get(index), this, childUpdateListenerSet);
        bindListeners(element, this, childUpdateListenerSet);
        return elements.set(index, element);
    }
    @Override
    public void add(int index, JSONElement element)
    {
        bindListeners(element, this, childUpdateListenerSet);
        elements.add(index, element);
    }
    public JSONElement remove(int index)
    {
        unbindListeners(elements.get(index), this, childUpdateListenerSet);
        return elements.remove(index);
    }
    @Override
    public int indexOf(Object o)
    {
        return elements.indexOf(o);
    }
    @Override
    public int lastIndexOf(Object o)
    {
        return elements.lastIndexOf(o);
    }
    @Override
    public ListIterator<JSONElement> listIterator()
    {
        return elements.listIterator();
    }
    @Override
    public ListIterator<JSONElement> listIterator(int index)
    {
        return elements.listIterator(index);
    }
    @Override
    public List<JSONElement> subList(int fromIndex, int toIndex)
    {
        return elements.subList(fromIndex, toIndex);
    }
    public int hashCode()
    {
        return elements.hashCode();
    }
    public boolean equals(Object o)
    {
        return o == this || o instanceof JSONArray && ((JSONArray) o).elements.equals(elements);
    }
    @Override
    public String toString()
    {
        return elements.stream().map(Object::toString).collect(Collectors.joining(", ", "[", "]"));
    }
    @Override
    public void addListener(ListChangeListener<? super JSONElement> listChangeListener)
    {
        elements.addListener(listChangeListener);
    }
    @Override
    public void removeListener(ListChangeListener<? super JSONElement> listChangeListener)
    {
        elements.removeListener(listChangeListener);
    }
    @Override
    public boolean addAll(JSONElement... jsonElements)
    {
        return elements.addAll(jsonElements);
    }
    @Override
    public boolean setAll(JSONElement... jsonElements)
    {
        return elements.setAll(jsonElements);
    }
    @Override
    public boolean setAll(Collection<? extends JSONElement> collection)
    {
        return elements.setAll(collection);
    }
    @Override
    public boolean removeAll(JSONElement... jsonElements)
    {
        return elements.removeAll(jsonElements);
    }
    @Override
    public boolean retainAll(JSONElement... jsonElements)
    {
        return elements.retainAll(jsonElements);
    }
    public void remove(int i, int i1)
    {
        for (int j = i; j < i1; ++j) remove(j);
    }
    @Override
    public void addListener(InvalidationListener invalidationListener)
    {
        elements.addListener(invalidationListener);
    }
    @Override
    public void removeListener(InvalidationListener invalidationListener)
    {
        elements.addListener(invalidationListener);
    }
    public void addListener(ChildUpdateListener childUpdateListener)
    {
        childUpdateListenerSet.add(childUpdateListener);
    }
    public void removeListener(ChildUpdateListener childUpdateListener)
    {
        childUpdateListenerSet.remove(childUpdateListener);
    }
}
