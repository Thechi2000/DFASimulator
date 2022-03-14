package ch.ludovic_mermod.dfasimulator;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PropertiesMap<K, V> implements ObservableMap<K, ObjectProperty<V>>
{
    private final ObservableMap<K, ObjectProperty<V>> map;
    private final Set<PropertyChangeListener<K, V>> listeners;

    public PropertiesMap()
    {
        this(FXCollections.observableHashMap());
    }
    public PropertiesMap(ObservableMap<K, ObjectProperty<V>> map)
    {
        this.map = map;
        listeners = new HashSet<>();

        map.addListener((MapChangeListener<K, ObjectProperty<V>>) change ->
        {
            if (change.wasAdded())
                listeners.forEach(l -> l.onValueChange(map.get(change.getKey()), change.getKey(), null, map.get(change.getKey()).getValue()));
        });
    }

    public ObjectProperty<V> computeIfAbsent(K key, V def)
    {
        if (map.containsKey(key)) return map.get(key);
        else return map.put(key, new SimpleObjectProperty<>(def));
    }

    public ObjectProperty<V> setValue(K key, V value)
    {
        if (map.containsKey(key))
        {
            V oldValue = map.get(key).getValue();
            map.get(key).set(value);
            listeners.forEach(l -> l.onValueChange(map.get(key), key, oldValue, value));
        }
        else put(key, new SimpleObjectProperty<>(value));
        return map.get(key);
    }
    public V getValue(K key)
    {
        return computeIfAbsent(key, (V) null).get();
    }

    public void addListener(PropertyChangeListener<K, V> listener)
    {
        listeners.add(listener);
    }
    public void removeListener(PropertyChangeListener<K, V> listener)
    {
        listeners.remove(listener);
    }

    @Override
    public void addListener(MapChangeListener<? super K, ? super ObjectProperty<V>> mapChangeListener)
    {
        map.addListener(mapChangeListener);
    }
    @Override
    public void removeListener(MapChangeListener<? super K, ? super ObjectProperty<V>> mapChangeListener)
    {
        map.removeListener(mapChangeListener);
    }
    @Override
    public int size()
    {
        return map.size();
    }
    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }
    @Override
    public boolean containsKey(Object key)
    {
        return map.containsKey(key);
    }
    @Override
    public boolean containsValue(Object value)
    {
        return map.containsValue(value);
    }
    @Override
    public ObjectProperty<V> get(Object key)
    {
        return map.get(key);
    }
    @Override
    public ObjectProperty<V> put(K key, ObjectProperty<V> value)
    {
        value.addListener((o, ov, nv) -> listeners.forEach(l -> l.onValueChange(value, key, ov, nv)));
        return map.put(key, value);
    }
    @Override
    public ObjectProperty<V> remove(Object key)
    {
        return map.remove(key);
    }
    @Override
    public void putAll(Map<? extends K, ? extends ObjectProperty<V>> m)
    {
        map.putAll(m);
    }
    @Override
    public void clear()
    {
        map.clear();
    }
    @Override
    public Set<K> keySet()
    {
        return map.keySet();
    }
    @Override
    public Collection<ObjectProperty<V>> values()
    {
        return map.values();
    }
    @Override
    public Set<Entry<K, ObjectProperty<V>>> entrySet()
    {
        return map.entrySet();
    }
    @Override
    public void addListener(InvalidationListener invalidationListener)
    {
        map.addListener(invalidationListener);
    }
    @Override
    public void removeListener(InvalidationListener invalidationListener)
    {
        map.removeListener(invalidationListener);
    }

    @FunctionalInterface
    public interface PropertyChangeListener<K, V>
    {
        void onValueChange(ObjectProperty<V> property, K key, V oldValue, V newValue);
    }
}
