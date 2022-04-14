package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.json.JSONArray;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.json.JSONable;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.TransferMode;

import java.util.Objects;

public abstract class Element implements JSONable
{
    protected Element parent;

    private static Element p_load(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasString("type");
        switch (object.get("type").getAsString())
        {
            case "fork":
            {
                object.checkHasObject(Fork.JSON_FIRST);
                object.checkHasObject(Fork.JSON_SECOND);
                object.checkHasString(Fork.JSON_ORIENTATION);
                object.checkHasNumber(Fork.JSON_DIVIDER);

                return new Fork(Objects.requireNonNull(p_load(object.getAsJSONObject(Fork.JSON_FIRST))),
                        Objects.requireNonNull(p_load(object.getAsJSONObject(Fork.JSON_SECOND))),
                        Orientation.valueOf(object.get(Fork.JSON_ORIENTATION).getAsString()),
                        object.get(Fork.JSON_DIVIDER).getAsNumber().doubleValue());
            }
            case "leaf":
            {
                object.checkHasArray(Leaf.JSON_ITEMS);
                return new Leaf(object.getAsJSONArray(Leaf.JSON_ITEMS).stream().map(e -> e.getAsJSONPrimitive().getAsString()).toArray(String[]::new));
            }
            default:
                return null;
        }
    }
    public static Sentinel load(JSONObject object) throws IOManager.CorruptedFileException
    {
        return new Sentinel(p_load(object));
    }

    protected void addHandlers()
    {
        getContent().setOnDragOver(e -> {
            if (e.getDragboard().hasString())
                e.acceptTransferModes(TransferMode.ANY);
        });

        getContent().setOnDragDropped(e -> {
            System.out.println("Drag dropped " + e.getDragboard().getString());
            Item i = Item.get(e.getDragboard().getString());

            PaneManager.INSTANCE.remove(i);
            ascendingAdd(i, e.getX(), e.getY());

            e.consume();
        });
    }

    private void ascendingAdd(Item i, double x, double y)
    {
        if (parent == null) add(i, x, y);
        else parent.ascendingAdd(i, x, y);
    }

    public abstract Element add(Item i, double x, double y);
    public abstract Element remove(Item i);
    public abstract Parent getContent();
}
