package ch.ludovic_mermod.dfasimulator;

import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Utils
{
    public static <K, V> Collector<Map.Entry<K, V>, HashMap<K, V>, Map<K, V>> toMap()
    {
        return new Collector<>()
        {
            @Override
            public Supplier<HashMap<K, V>> supplier()
            {
                return HashMap::new;
            }
            @Override
            public BiConsumer<HashMap<K, V>, Map.Entry<K, V>> accumulator()
            {
                return (m, e) -> m.put(e.getKey(), e.getValue());
            }
            @Override
            public BinaryOperator<HashMap<K, V>> combiner()
            {
                return (m1, m2) ->
                {
                    m1.putAll(m2);
                    return m1;
                };
            }
            @Override
            public Function<HashMap<K, V>, Map<K, V>> finisher()
            {
                return Map::copyOf;
            }
            @Override
            public Set<Characteristics> characteristics()
            {
                return Set.of(Characteristics.UNORDERED);
            }
        };
    }

    public static <K, V> Collector<Map.Entry<K, V>, TreeMap<K, V>, TreeMap<K, V>> toTreeMap()
    {
        return new Collector<>()
        {
            @Override
            public Supplier<TreeMap<K, V>> supplier()
            {
                return TreeMap::new;
            }
            @Override
            public BiConsumer<TreeMap<K, V>, Map.Entry<K, V>> accumulator()
            {
                return (m, e) -> m.put(e.getKey(), e.getValue());
            }
            @Override
            public BinaryOperator<TreeMap<K, V>> combiner()
            {
                return (m1, m2) ->
                {
                    m1.putAll(m2);
                    return m1;
                };
            }
            @Override
            public Function<TreeMap<K, V>, TreeMap<K, V>> finisher()
            {
                return Function.identity();
            }
            @Override
            public Set<Characteristics> characteristics()
            {
                return Set.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH);
            }
        };
    }

    public static <T> StringConverter<T> stringConverter(Function<T, String> toString, Function<String, T> fromString)
    {
        return new StringConverter<T>()
        {
            @Override
            public String toString(T t)
            {
                return toString.apply(t);
            }
            @Override
            public T fromString(String s)
            {
                return fromString.apply(s);
            }
        };
    }
}
