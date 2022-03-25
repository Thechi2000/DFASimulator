package ch.ludovic_mermod.dfasimulator.constants;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import ch.ludovic_mermod.dfasimulator.utils.PropertiesMap;
import javafx.beans.binding.Binding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static javafx.scene.paint.Color.BLACK;

public class Constants
{
    public static final Font FONT = new Font(Font.getDefault().getName(), 20);

    private static final PropertiesMap<String, Object> values;
    private static final Properties                    properties;

    private static final Pattern BOOL_PATTERN   = Pattern.compile("(true|false)");
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("(\\d+\\.?\\d*|\\d*\\.\\d+)d?");
    private static final Pattern COLOR_PATTERN  = Pattern.compile("0x[\\da-fA-F]{8}");

    static
    {
        values = new PropertiesMap<>();
        properties = new Properties();
        loadFromFile("settings.json");
        values.addListener((p, k, o, n) -> properties.put(k, n.toString()));
    }

    public static void loadFromFile(String filename)
    {
        try (FileReader fileReader = new FileReader(filename))
        {
            properties.load(fileReader);
        }
        catch (IOException e)
        {
            Main.logger.log(Level.WARNING, "Could not load settings from file \"%s\"", filename);
        }

        properties.keySet().stream().map(Object::toString).forEach(k -> {
            String value = properties.getProperty(k);
            ObjectProperty<Object> obs = null;

            if (value.matches(BOOL_PATTERN.pattern()))
                obs = new SimpleObjectProperty<>(Boolean.valueOf(value));
            else if (value.matches(COLOR_PATTERN.pattern()))
                obs = new SimpleObjectProperty<>(Color.valueOf(value));
            else if (value.matches(DOUBLE_PATTERN.pattern()))
                obs = new SimpleObjectProperty<>(Double.parseDouble(value));
            else
                Main.log(Level.WARNING, "Could not parse property \"%s:%s\" in \"%s\"", k, value, filename);

            if (obs != null)
            {
                obs.addListener((o, ov, nv) -> properties.put(k, nv.toString()));
                values.put(k, obs);
            }
        });
    }
    public static void saveToFile(String filename)
    {
        try (FileWriter fileWriter = new FileWriter(filename))
        {
            properties.store(fileWriter, LocalDateTime.now().toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static Binding<Boolean> getBoolean(String key)
    {
        return get(key, false);
    }
    public static Binding<Double> getDouble(String key)
    {
        return get(key, 0d);
    }
    public static Binding<Color> getColor(String key)
    {
        return get(key, BLACK);
    }

    public static <T> Binding<T> get(String key, T defaultValue)
    {
        if (!values.containsKey(key))
        {
            Main.log(Level.WARNING, "Could not find property \"%s\" of type %s", key, defaultValue.getClass().getName());
            values.setValue(key, defaultValue);
        }

        var value = values.get(key);
        if (defaultValue.getClass().isAssignableFrom(value.get().getClass())) return CustomBindings.create(() -> (T) value.get(), value);
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
        if (!values.containsKey(key))
        {
            Main.log(Level.WARNING, "Could not find property \"%s\" of type %s", key, defaultValue.getClass().getName());
            values.setValue(key, defaultValue);
        }

        var value = values.get(key);

        if (defaultValue instanceof Double && value.get() instanceof Double)
            return (T) value.get();

        if (defaultValue.getClass().isAssignableFrom(value.get().getClass())) return (T) value.get();
        else throw new IllegalArgumentException(String.format("Property \"%s\" is not of type \"%s\"", defaultValue.getClass().getName(), value.get().getClass().getName()));
    }
}