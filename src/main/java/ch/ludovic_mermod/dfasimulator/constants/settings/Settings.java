package ch.ludovic_mermod.dfasimulator.constants.settings;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.constants.Resources;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

public class Settings
{
    private static final Map<String, Setting> settingsMap = new TreeMap<>();

    public static Set<String> keySet() {return settingsMap.keySet();}

    static
    {
        load(Resources.get("settings.properties"));
    }

    public static void load(String filename)
    {
        Properties settingsProperties = new Properties();

        try (Reader reader = new FileReader(filename))
        {
            settingsProperties.load(reader);
        }
        catch (IOException e)
        {
            Main.logger.log(Level.SEVERE, String.format("Could not read settings from file %s", filename), e);
        }

        settingsProperties.forEach((k, v) -> {
            JSONElement element = JSONElement.parse(v.toString());

            if (!element.isJSONObject())
            {
                Main.log(Level.WARNING, "Invalid JSONElement in settings file " + v);
                return;
            }

            try
            {
                settingsMap.put(k.toString(), Setting.loadFromJSON(element.getAsJSONObject()));
            }
            catch (IOManager.CorruptedFileException e)
            {
                Main.logger.log(Level.WARNING, "Could not convert " + v + " to setting", e);
            }
        });
    }

    public static void save(String filename)
    {
        Properties properties = new Properties();
        settingsMap.forEach((k, v) -> {
            if (v != null) properties.put(k, v.getJSONObject().toString());
        });

        try (Writer writer = new FileWriter(filename))
        {
            properties.store(writer, "");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Setting getSetting(String id)
    {
        return settingsMap.getOrDefault(id, null);
    }
    public static Setting getSetting(String id, Setting.Type type)
    {
        var setting = settingsMap.computeIfAbsent(id, k -> {
            Main.log(Level.WARNING, "Could not find property \"%s\" of type %s", id, type);
            return type.factory.get();
        });

        if (setting.type != type) throw new IllegalArgumentException(String.format("Incompatible type for settings %s, queried %s actual %s", id, type, setting.type));
        return setting;
    }

    /**
     * Get a setting as a setting
     *
     * @param id (String) id of the setting
     * @return the ObservableValue for the setting
     */
    public static ObservableValue<Object> get(String id, Setting.Type type)
    {
        return getSetting(id, type).getValueBinding();
    }

    /**
     * Get a setting as a Boolean setting
     *
     * @param id (String) id of the setting
     * @return the ObservableValue for the setting
     */
    public static ObservableValue<Boolean> getBoolean(String id)
    {
        return getAs(id, Setting.Type.BOOLEAN);
    }
    /**
     * Get a setting as a Double setting
     *
     * @param id (String) id of the setting
     * @return the ObservableValue for the setting
     */
    public static ObservableValue<Double> getDouble(String id)
    {
        return getAs(id, Setting.Type.DOUBLE);
    }
    /**
     * Get a setting as a Color setting
     *
     * @param id (String) id of the setting
     * @return the ObservableValue for the setting
     */
    public static ObservableValue<Color> getColor(String id)
    {
        return getAs(id, Setting.Type.COLOR);
    }
    /**
     * Get a setting as a Font setting
     *
     * @param id (String) id of the setting
     * @return the ObservableValue for the setting
     */
    public static ObservableValue<Font> getFont(String id)
    {
        return getAs(id, Setting.Type.FONT);
    }

    /**
     * Get the ObservableValue bound to a setting
     *
     * @param <T>  class of the value
     * @param id   id of the setting
     * @param type type of value
     * @return the ObservableValue bound to the setting
     */
    public static <T> ObservableValue<T> getAs(String id, Setting.Type type)
    {
        var set = getSetting(id, type);
        return CustomBindings.create(() -> ((T) set.getValueBinding().getValue()), set.getValueBinding());
    }

    /**
     * Get a setting value as a Boolean
     *
     * @param id (String) id of the setting
     * @return the value of the setting
     */
    public static Boolean getBooleanValue(String id)
    {
        return getValue(id, Setting.Type.BOOLEAN);
    }
    /**
     * Get a setting value as a Double
     *
     * @param id (String) id of the setting
     * @return the value of the setting
     */
    public static Double getDoubleValue(String id)
    {
        return getValue(id, Setting.Type.DOUBLE);
    }
    /**
     * Get a setting value as a Color
     *
     * @param id (String) id of the setting
     * @return the value of the setting
     */
    public static Color getColorValue(String id)
    {
        return getValue(id, Setting.Type.COLOR);
    }
    /**
     * Get a setting value as a Font
     *
     * @param id (String) id of the setting
     * @return the value of the setting
     */
    public static Font getFontValue(String id)
    {
        return getValue(id, Setting.Type.FONT);
    }

    /**
     * Get a setting value as a T
     *
     * @param <T> type of the setting value
     * @param id  (String) id of the setting
     * @return the value of the setting
     */
    public static <T> T getValue(String id, Setting.Type type)
    {
        return (T) getAs(id, type).getValue();
    }
}
