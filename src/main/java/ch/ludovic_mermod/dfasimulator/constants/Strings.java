package ch.ludovic_mermod.dfasimulator.constants;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import ch.ludovic_mermod.dfasimulator.utils.PropertiesMap;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Strings
{
    protected static final Pattern                       DEPENDENCY_PATTERN = Pattern.compile("\\$\\(([a-z_.]+)\\)");
    private static final   PropertiesMap<String, String> map;

    static
    {
        map = new PropertiesMap<>();
        loadLocale(Locale.ENGLISH);
    }

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

    public static void bindFormat(String id, StringProperty target, Object... objects)
    {
        target.bind(CustomBindings.format(get(id), objects));
    }

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

    public static void loadLocale(Locale locale)
    {
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle("lang", locale);
            bundle.keySet().forEach(k -> set(k, bundle.getString(k)));
        }
        catch (MissingResourceException e)
        {
            Main.logger.log(Level.SEVERE, "While loading lang bundle", e);
        }
    }
}
