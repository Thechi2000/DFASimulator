package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.json.JSONable;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.input.TransferMode;

import java.util.Objects;
import java.util.logging.Level;

public abstract class Element implements JSONable
{
    protected Element parent;

    private static Element recursiveLoad(JSONObject object) throws IOManager.CorruptedFileException
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

                return new Fork(Objects.requireNonNull(recursiveLoad(object.getAsJSONObject(Fork.JSON_FIRST))),
                        Objects.requireNonNull(recursiveLoad(object.getAsJSONObject(Fork.JSON_SECOND))),
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
        object.checkHasBoolean(PaneManager.JSON_IS_MAIN);
        return new Sentinel(recursiveLoad(object), object.get(PaneManager.JSON_IS_MAIN).getAsBoolean());
    }

    protected void addHandlers()
    {
        getContent().setOnDragOver(e -> {
            if (e.getDragboard().hasString())
                e.acceptTransferModes(TransferMode.ANY);
        });

        getContent().setOnDragDropped(e -> {
            try
            {
                Item i = Item.get(e.getDragboard().getString());

                PaneManager.INSTANCE.remove(i);
                add(i, e.getX(), e.getY());

                e.consume();
            }
            catch (Exception err)
            {
                Main.logger.log(Level.SEVERE, "Error during drop event", err);
            }
        });
    }


    protected void removeFromParent()
    {
        if (parent != null) parent.removeChild(this);
    }
    protected abstract void removeChild(Element child);
    protected abstract void update(Element oldValue, Element newValue);
    public abstract void add(Item i, double x, double y);
    public abstract Element remove(Item i);
    public abstract Parent getContent();
}
