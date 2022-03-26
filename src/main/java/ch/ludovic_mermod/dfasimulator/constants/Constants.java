package ch.ludovic_mermod.dfasimulator.constants;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.utils.PropertiesMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static javafx.scene.paint.Color.BLACK;

public class Constants
{
    public static final Font FONT = new Font(Font.getDefault().getName(), 20);

    public static final Pattern BOOL_PATTERN   = Pattern.compile("(true|false)");
    public static final Pattern DOUBLE_PATTERN = Pattern.compile("(\\d+\\.?\\d*|\\d*\\.\\d+)d?");
    public static final Pattern COLOR_PATTERN  = Pattern.compile("0x[\\da-fA-F]{8}");

    private static final PropertiesMap<String, Object> values;

    private static JSONObject json;

    static
    {
        values = new PropertiesMap<>();
        loadFromFile("settings.json");
        //values.addListener((p, k, o, n) -> getSetting (k).);
    }

    public static JSONObject getSetting(String id)
    {
        List<String> path = Arrays.asList(id.split("\\."));
        var setting = getSetting(json, path);

        if (setting == null)
            throw new IllegalArgumentException(String.format("Could not find setting \"%s\"", id));
        else
            return setting;
    }
    private static JSONObject getSetting(JSONObject object, List<String> path)
    {
        if (path.size() == 0)
            return object;
        else if (!object.hasObject(path.get(0)))
            object.add(path.get(0), new JSONObject());

        return getSetting(object.getAsJSONObject(path.get(0)), path.subList(1, path.size()));
    }

    public static void loadFromFile(String filename)
    {
        var readJSON = JSONElement.readFromFile(filename);
        json = readJSON.isJSONObject() ? readJSON.getAsJSONObject() : new JSONObject();
        values.clear();

        try
        {
            addEntries(json, "");
        }
        catch (IOManager.CorruptedFileException e)
        {
            Main.logger.log(Level.SEVERE, "Could not parse settings file", e);
        }
    }
    public static void saveToFile(String filename)
    {
        json.saveToFile(filename);
    }

    private static void addEntries(JSONObject object, String objectPath) throws IOManager.CorruptedFileException
    {
        if (object.has(".value") && object.has(".type"))
        {
            String type = object.get(".type").getAsString();
            JSONElement valueElement = object.get(".value");
            ObjectProperty<Object> obs = switch (type)
                    {
                        case "double" -> new SimpleObjectProperty<>(valueElement.getAsDouble());
                        case "boolean" -> new SimpleObjectProperty<>(valueElement.getAsBoolean());
                        case "color" -> new SimpleObjectProperty<>(Color.valueOf(valueElement.getAsString()));
                        default -> throw new IOManager.CorruptedFileException("Unknown type \"%s\" at \"%s\"", valueElement.getAsString(), object);
                    };

            object.addProperty(".value", obs);
            values.put(objectPath, obs);
        }
        else
        {
            if (!objectPath.isBlank()) objectPath = objectPath + ".";
            for (var e : object.entrySet())
            {
                if (!e.getValue().isJSONObject()) throw new IOManager.CorruptedFileException("Could not convert \"%s\" to JSONObject", objectPath + e.getKey());
                addEntries(e.getValue().getAsJSONObject(), objectPath + e.getKey());
            }
        }
    }

    public static ObjectProperty<Boolean> getBoolean(String key)
    {
        return get(key, false);
    }
    public static ObjectProperty<Double> getDouble(String key)
    {
        return get(key, 0d);
    }
    public static ObjectProperty<Color> getColor(String key)
    {
        return get(key, BLACK);
    }
    public static <T> ObjectProperty<T> get(String key, T defaultValue)
    {
        if (!values.containsKey(key))
        {
            Main.log(Level.WARNING, "Could not find property \"%s\" of type %s", key, defaultValue.getClass().getName());

            SimpleObjectProperty<Object> prop = new SimpleObjectProperty<>(defaultValue);
            values.put(key, prop);

            var sett = getSetting(key);
            sett.addProperty(".value", prop);
            sett.addProperty(".type", switch (defaultValue.getClass().getSimpleName())
                    {
                        case "Double" -> "double";
                        case "Boolean" -> "boolean";
                        case "Color" -> "color";
                        default -> "string";
                    });
        }

        var value = values.get(key);
        if (defaultValue.getClass().isAssignableFrom(value.get().getClass())) return (ObjectProperty<T>) value;
        else throw new IllegalArgumentException("Property \"%s\" is not of type \"%s\"");
    }

    public static Boolean getBooleanValue(String key)
    {
        return getValue(key, false);
    }
    public static Double getDoubleValue(String key)
    {
        return getValue(key, 0d);
    }
    public static Color getColorValue(String key)
    {
        return getValue(key, BLACK);
    }
    public static <T> T getValue(String key, T defaultValue)
    {
        return get(key, defaultValue).getValue();
    }

    public static <T> void setValue(String key, T value)
    {
        values.get(key).set(value);
    }

    public static Set<String> keySet()
    {
        return values.keySet();
    }
}