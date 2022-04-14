package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import javafx.scene.Node;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record Item(Node node, String name, String id)
{
    private static final Map<String, Item> itemMap = new TreeMap<>();
    public static void register(Item item)
    {
        itemMap.put(item.id, item);
    }
    public static Item get(String id)
    {
        return itemMap.get(id);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Item) o).id);
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}
