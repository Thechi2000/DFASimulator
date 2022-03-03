package ch.ludovic_mermod.dfasimulator.gui.lang;

import ch.ludovic_mermod.dfasimulator.Main;
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

        set("tools", "Tools");
        set("edit", "Edit");
        set("drag", "Drag");
        set("link", "Link");

        set("editpane.link.alphabet_text", "Alphabet: ");
        set("editpane.link.alphabet_prompt", "alphabet");
        set("editpane.link.nodes_linking", "to");
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
            Main.logger.log(System.Logger.Level.WARNING, String.format("Could not find translated string for \"%s\"", id));
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
