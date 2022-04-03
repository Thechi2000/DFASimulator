package ch.ludovic_mermod.dfasimulator.constants;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import ch.ludovic_mermod.dfasimulator.utils.PropertiesMap;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Static grouping of all translatable Strings of the application.
 * Each one is indexed with a unique String id.
 */
public class Strings
{
    protected static final Pattern                       DEPENDENCY_PATTERN = Pattern.compile("\\$\\(([a-z_.]+)\\)");
    private static final   PropertiesMap<String, String> map;
    private static final   Properties                    properties;
    private static         Locale                        currentLocale      = null;

    static
    {
        map = new PropertiesMap<>();
        properties = new Properties();
        map.addListener((p, k, o, n) -> {
            final String value = k.equals(n) ? "" : n;
            properties.setProperty(k, value);
        });
        map.addListener((k, p) -> properties.remove(k));
        loadLocale(Locale.ENGLISH);
    }

    private Strings() {}
    /**
     * The StringProperty representing the id
     *
     * @param id the StringProperty queried
     * @return the StringProperty associated with the given id
     */
    public static StringBinding get(String id)
    {
        if (!map.containsKey(id))
        {
            Main.log(Level.WARNING, String.format("Could not find translated string for \"%s\"", id.replace("%s", "%%s")));
            set(id, id);
        }

        List<StringBinding> dependencies = DEPENDENCY_PATTERN.matcher(id).results().map(r -> get(r.group(1))).toList();

        //return CustomBindings.format(CustomBindings.create(() -> map.get(id).get().replaceAll("\\$\\(([a-z_.]+)\\)", "%s"), map.get(id)), false, dependencies.toArray());
        return new StringBinding()
        {
            {
                bind(dependencies.toArray(new StringBinding[0]));
            }

            @Override
            protected String computeValue()
            {
                AtomicReference<String> result = new AtomicReference<>(map.getValue(id));
                DEPENDENCY_PATTERN.matcher(map.getValue(id)).results().forEach(r -> result.set(result.get().replace(r.group(0), Strings.get(r.group(1)).get())));
                return result.get();
            }
        };
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

    /**
     * Bind a StringProperty to a String produced by formatting the value of id with the objects
     * If an object is a property, its value is used in formatting (instead of the whole property) the target is updated when the property is
     *
     * @param id      (String) id of the String used as format
     * @param target  (StringProperty) target to bind to the formatted String
     * @param objects (Objects[]) objects to use for formatting
     */
    public static void bindFormat(String id, StringProperty target, Object... objects)
    {
        target.bind(CustomBindings.format(get(id), objects));
    }

    /**
     * Format the value of id with the objects
     *
     * @param id      (String) id of the String used as format
     * @param objects (Objects[]) objects to use for formatting
     * @return the formatted String
     */
    public static String format(String id, Object... objects)
    {
        String format = get(id).get();

        int occ = (format.length() - format.replace("%s", "").length()) / 2;
        if (objects.length != occ)
            Main.log(Level.WARNING, "Invalid parameters count in \"%s\", expected: %d actual: %d", id, objects.length, occ);

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
        map.setValue(id, value);
    }

    /**
     * Load translations for a locale
     *
     * @param locale (Locale) locale to load
     */
    public static void loadLocale(Locale locale)
    {
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle("lang", locale);
            bundle.keySet().forEach(k -> set(k, bundle.getString(k)));
            currentLocale = locale;
        }
        catch (MissingResourceException e)
        {
            Main.logger.log(Level.SEVERE, "While loading lang bundle", e);
        }
    }

    /**
     * Save all Strings to the current locale file
     */
    public static void save()
    {
        try (FileWriter writer = new FileWriter(Resources.get("lang_" + currentLocale.toLanguageTag() + ".properties")))
        {
            properties.store(writer, LocalDateTime.now().toString());
        }
        catch (IOException e)
        {
            Main.logger.log(Level.SEVERE, "Could not save language properties", e);
        }
    }
}
