package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.json.JSONable;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import javafx.geometry.Orientation;
import javafx.scene.Node;

import java.util.Objects;

public interface Element extends JSONable
{
    static Element load(JSONObject object) throws IOManager.CorruptedFileException
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

                return new Fork(Objects.requireNonNull(load(object.getAsJSONObject(Fork.JSON_FIRST))),
                        Objects.requireNonNull(load(object.getAsJSONObject(Fork.JSON_SECOND))),
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

    Element add(Item i, double x, double y);
    Element remove(Item i);
    Node getContent();
}
