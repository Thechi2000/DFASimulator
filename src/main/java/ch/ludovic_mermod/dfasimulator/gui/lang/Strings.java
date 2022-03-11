package ch.ludovic_mermod.dfasimulator.gui.lang;

import ch.ludovic_mermod.dfasimulator.Main;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Strings
{
    private static final Map<String, StringProperty> map;

    static
    {
        map = new TreeMap<>();
    }

    /**
     * The StringProperty representing the id
     *
     * @param id the StringProperty queried
     * @return the StringProperty associated with the given id
     */
    public static StringProperty get(String id)
    {
        if (!map.containsKey(id))
        {
            Main.logger.log(System.Logger.Level.WARNING, String.format("Could not find translated string for \"%s\"", id.replace("%s", "%%s")));
            set(id, id);
        }

        return map.get(id);
    }

    /**
     * Bind the target to the property designated by id
     *
     * @param id     the StringProperty id to bind to
     * @param target the StringProperty to bind
     */
    public static void bind(String id, StringProperty target)
    {
        target.set(get(id).get());
        target.bind(get(id));
    }

    public static void bindFormat(String id, StringProperty target, Object... objects)
    {
        StringProperty format = get(id);

        int occ = (format.get().length() - format.get().replace("%s", "").length()) / 2;
        if (objects.length != occ)
            Main.logger.log(System.Logger.Level.WARNING, "Invalid parameters count in \"%s\", expected: %d actual: %d", id, objects.length, occ);

        Set<ObservableValue<?>> properties = Arrays.stream(objects)
                .filter(o -> o instanceof ObservableValue<?>)
                .map(o -> (ObservableValue<?>) o)
                .collect(Collectors.toSet());

        properties.add(format);

        target.set(String.format(format.get(), Arrays.stream(objects).map(o -> o instanceof ObservableValue<?> ? ((ObservableValue<?>) o).getValue() : o).toArray()));
        properties.forEach(p -> p.addListener((obs, ov, nv) -> target.set(String.format(format.get(), Arrays.stream(objects).map(o -> o instanceof ObservableValue<?> ? ((ObservableValue<?>) o).getValue() : o).toArray()))));
    }

    public static String format(String id, Object... objects)
    {
        String format = get(id).get();

        int occ = (format.length() - format.replace("%s", "").length()) / 2;
        if (objects.length != occ)
            Main.logger.log(System.Logger.Level.WARNING, "Invalid parameters count in \"%s\", expected: %d actual: %d", id, objects.length, occ);

        return String.format(format, objects);
    }

    /**
     * Set the StringProperty associated with id to the given value
     *
     * @param id    the StringProperty to modify
     * @param value the value to put
     */
    public static void set(String id, String value)
    {
        if (map.containsKey(id)) map.get(id).set(value);
        else map.put(id, new SimpleStringProperty(value));
    }
}
