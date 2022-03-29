package ch.ludovic_mermod.dfasimulator.utils;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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

/**
 * Static class grouping miscellaneous methods
 */
public class Utils
{
    private Utils() {}

    /**
     * Creates a collector for a HashMap
     *
     * @param <K> type of the keys
     * @param <V> type of the values
     * @return a collector for a HashMap
     */
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

    /**
     * Creates a collector for a TreeMap
     *
     * @param <K> type of the keys
     * @param <V> type of the values
     * @return a collector for a TreeMap
     */
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

    /**
     * Creates a StringConverter
     *
     * @param toString   function to convert T to String
     * @param fromString function to convert String to T
     * @param nullString String to be considered as a null value
     * @param <T>        type of the converted
     * @return a StringConverter
     */
    public static <T> StringConverter<T> stringConverter(Function<T, String> toString, Function<String, T> fromString, String nullString)
    {
        return new StringConverter<T>()
        {
            @Override
            public String toString(T t)
            {
                return t == null ? nullString : toString.apply(t);
            }
            @Override
            public T fromString(String s)
            {
                return s.equals(nullString) ? null : fromString.apply(s);
            }
        };
    }

    /**
     * Create, open and return a new Stage
     *
     * @param content content of the Stage
     * @param width   width of the Scene
     * @param height  height of the Scene
     * @return the newly created Stage
     */
    public static Stage openNewStage(Parent content, double width, double height)
    {
        Stage s = new Stage();
        s.setScene(new Scene(content, width, height));
        s.show();
        return s;
    }
    /**
     * Create, open and return a new Stage
     *
     * @param content content of the Stage
     * @return the newly created Stage
     */
    public static Stage openNewStage(Parent content)
    {
        Stage s = new Stage();
        s.setScene(new Scene(content));
        Platform.runLater(s::show);
        return s;
    }
}
