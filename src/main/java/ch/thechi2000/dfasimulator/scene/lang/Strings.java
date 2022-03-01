package ch.thechi2000.dfasimulator.scene.lang;

import ch.thechi2000.dfasimulator.Main;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;
import java.util.TreeMap;

public class Strings
{
    private static final Map<String, StringProperty> map;

    static
    {
        map = new TreeMap<>();

        set("delete", "Delete");
        set("create", "Create");
    }

    public static StringProperty get(String id)
    {
        if (!map.containsKey(id))
        {
            Main.logger.log(System.Logger.Level.WARNING, String.format("Could not find translated string for \"%s\"", id));
            set(id, id);
        }

        return map.get(id);
    }

    public static void bind(String id, StringProperty target)
    {
        target.set(get(id).get());
        target.bind(get(id));
    }

    public static void set(String id, String value)
    {
        if (map.containsKey(id)) map.get(id).set(value);
        else map.put(id, new SimpleStringProperty(value));
    }
}
