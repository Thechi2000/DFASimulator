package ch.ludovic_mermod.dfasimulator.utils;

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
    private final Set<PropertyChangeListener<K, V>> propertyChangeListeners;
    private final Set<PropertyRemoveListener<K, V>> propertyRemoveListeners;

    public PropertiesMap()
    {
        this(FXCollections.observableHashMap());
    }
    public PropertiesMap(ObservableMap<K, ObjectProperty<V>> map)
    {
        this.map = map;
        propertyChangeListeners = new HashSet<>();
        propertyRemoveListeners = new HashSet<>();

        map.addListener((MapChangeListener<K, ObjectProperty<V>>) change ->
        {
            if (change.wasAdded())
                propertyChangeListeners.forEach(l -> l.onValueChange(map.get(change.getKey()), change.getKey(), null, map.get(change.getKey()).getValue()));
        });
    }

    public ObjectProperty<V> computeIfAbsent(Object key, V def)
    {
        if (map.containsKey(key)) return map.get(key);
        else
        {
            final SimpleObjectProperty<V> prop = new SimpleObjectProperty<>(def);
            put(((K) key), prop);
            return prop;
        }
    }

    public ObjectProperty<V> setValue(K key, V value)
    {
        if (map.containsKey(key))
        {
            V oldValue = map.get(key).getValue();
            map.get(key).set(value);
            propertyChangeListeners.forEach(l -> l.onValueChange(map.get(key), key, oldValue, value));
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
        propertyChangeListeners.add(listener);
    }
    public void removeListener(PropertyChangeListener<K, V> listener)
    {
        propertyChangeListeners.remove(listener);
    }
    public void addListener(PropertyRemoveListener<K, V> listener)
    {
        propertyRemoveListeners.add(listener);
    }
    public void removeListener(PropertyRemoveListener<K, V> listener)
    {
        propertyRemoveListeners.remove(listener);
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
        return computeIfAbsent(key, null);
    }
    @Override
    public ObjectProperty<V> put(K key, ObjectProperty<V> value)
    {
        value.addListener((o, ov, nv) -> propertyChangeListeners.forEach(l -> l.onValueChange(value, key, ov, nv)));
        return map.put(key, value);
    }
    @Override
    public ObjectProperty<V> remove(Object key)
    {
        if (!map.containsKey(key)) return null;
        final ObjectProperty<V> value = map.get(key);
        var ret = map.remove(key);

        propertyRemoveListeners.forEach(l -> l.onPropertyRemove((K) key, value));

        return ret;
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

    @FunctionalInterface
    public interface PropertyRemoveListener<K, V>
    {
        void onPropertyRemove(K key, ObjectProperty<V> value);
    }
}
