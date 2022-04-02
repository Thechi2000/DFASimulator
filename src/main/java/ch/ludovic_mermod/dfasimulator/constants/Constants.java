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

/**
 * Static grouping of all Constants for the application.
 * Each one is indexed with a unique String id.
 */
public class Constants
{
    public static final  Pattern                       DOUBLE_PATTERN = Pattern.compile("(\\d+\\.?\\d*|\\d*\\.\\d+)d?");
    private static final PropertiesMap<String, Object> values;
    private static       JSONObject                    json;

    static
    {
        values = new PropertiesMap<>();
        loadFromFile(Resources.get("settings.json"));
        //values.addListener((p, k, o, n) -> getSetting (k).);
    }

    private Constants() {}
    /**
     * @param id (String) id of the setting to get
     * @return the JSONObject representing the queried setting
     */
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

    /**
     * Load all settings from a file
     *
     * @param filename (String) the file to load from
     */
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

    /**
     * Save all settings to a file
     *
     * @param filename (String) the file to save to
     */
    public static void saveToFile(String filename)
    {
        json.saveToFile(filename);
    }

    private static void addEntries(JSONObject object, String objectPath) throws IOManager.CorruptedFileException
    {
        if (object.has(".type"))
        {
            String type = object.get(".type").getAsString();
            ObjectProperty<Object> obs = switch (type)
                    {
                        case "double" -> new SimpleObjectProperty<>(object.get(".value").getAsDouble());
                        case "boolean" -> new SimpleObjectProperty<>(object.get(".value").getAsJSONPrimitive().getAsBoolean());
                        case "color" -> new SimpleObjectProperty<>(Color.valueOf(object.get(".value").getAsString()));
                        case "font" -> new SimpleObjectProperty<>(new Font(object.get(".name").getAsString(), object.get(".size").getAsDouble()));
                        default -> throw new IOManager.CorruptedFileException("Unknown type \"%s\" at \"%s\"", object.get(".value").getAsString(), object);
                    };
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

    /**
     * Get a setting as a Boolean setting
     *
     * @param id (String) id of the setting
     * @return the ObjectProperty for the setting
     */
    public static ObjectProperty<Boolean> getBoolean(String id)
    {
        return get(id, false);
    }
    /**
     * Get a setting as a Double setting
     *
     * @param id (String) id of the setting
     * @return the ObjectProperty for the setting
     */
    public static ObjectProperty<Double> getDouble(String id)
    {
        return get(id, 0d);
    }
    /**
     * Get a setting as a Color setting
     *
     * @param id (String) id of the setting
     * @return the ObjectProperty for the setting
     */
    public static ObjectProperty<Color> getColor(String id)
    {
        return get(id, BLACK);
    }
    /**
     * Get a setting as a Font setting
     *
     * @param id (String) id of the setting
     * @return the ObjectProperty for the setting
     */
    public static ObjectProperty<Font> getFont(String id)
    {
        return get(id, Font.getDefault());
    }
    /**
     * Get a setting as a T setting
     *
     * @param <T> type of the setting value
     * @param id  (String) id of the setting
     * @return the ObjectProperty for the setting
     */
    public static <T> ObjectProperty<T> get(String id, T defaultValue)
    {
        if (!values.containsKey(id))
        {
            Main.log(Level.WARNING, "Could not find property \"%s\" of type %s", id, defaultValue.getClass().getName());

            SimpleObjectProperty<Object> prop = new SimpleObjectProperty<>(defaultValue);
            values.put(id, prop);

            var sett = getSetting(id);
            sett.addProperty(".type", switch (defaultValue.getClass().getSimpleName())
                    {
                        case "Double" -> "double";
                        case "Boolean" -> "boolean";
                        case "Color" -> "color";
                        case "Font" -> "font";
                        default -> "string";
                    });
        }

        var value = values.get(id);
        if (defaultValue.getClass().isAssignableFrom(value.get().getClass())) return (ObjectProperty<T>) value;
        else throw new IllegalArgumentException(String.format("Property \"%s\" is not of type \"%s\"", id, defaultValue.getClass().getName()));
    }

    /**
     * Get a setting value as a Boolean
     *
     * @param id (String) id of the setting
     * @return the value of the setting
     */
    public static Boolean getBooleanValue(String id)
    {
        return getValue(id, false);
    }
    /**
     * Get a setting value as a Double
     *
     * @param id (String) id of the setting
     * @return the value of the setting
     */
    public static Double getDoubleValue(String id)
    {
        return getValue(id, 0d);
    }
    /**
     * Get a setting value as a Color
     *
     * @param id (String) id of the setting
     * @return the value of the setting
     */
    public static Color getColorValue(String id)
    {
        return getValue(id, BLACK);
    }
    /**
     * Get a setting value as a Font
     *
     * @param id (String) id of the setting
     * @return the value of the setting
     */
    public static Font getFontValue(String id)
    {
        return getValue(id, Font.getDefault());
    }
    /**
     * Get a setting value as a T
     *
     * @param <T> type of the setting value
     * @param id  (String) id of the setting
     * @return the value of the setting
     */
    public static <T> T getValue(String id, T defaultValue)
    {
        return get(id, defaultValue).getValue();
    }

    /**
     * Set the value of a setting to a given value
     *
     * @param id    (String) the id of the setting to set
     * @param value (T) the value to give
     * @param <T>   the type of the value
     */
    public static <T> void setValue(String id, T value)
    {
        values.get(id).set(value);
    }

    /**
     * @return (Set < String) a Set containing all available settings' ids
     */
    public static Set<String> keySet()
    {
        return values.keySet();
    }
}