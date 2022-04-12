package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

public class Fork implements Element
{
    public static final String     JSON_FIRST       = "first";
    public static final String     JSON_SECOND      = "second";
    public static final String     JSON_TYPE        = "type";
    public static final String     JSON_DIVIDER     = "divider";
    public static final String     JSON_ORIENTATION = "orientation";
    private final       SplitPane  splitPane;
    private final       JSONObject jsonObject;

    private Element first;
    private Element second;

    public Fork(Element first, Element second, Orientation orientation)
    {
        this(first, second, orientation, 0);
    }
    public Fork(Element first, Element second, Orientation orientation, double divider)
    {
        this.first = first;
        this.second = second;
        splitPane = new SplitPane(first.getContent(), second.getContent());
        splitPane.setOrientation(orientation);
        if(divider != 0) splitPane.getDividers().get(0).setPosition(divider);

        jsonObject = new JSONObject();
        jsonObject.add(JSON_FIRST, first.getJSONObject());
        jsonObject.add(JSON_SECOND, second.getJSONObject());
        jsonObject.addProperty(JSON_TYPE, "fork");
        jsonObject.addProperty(JSON_DIVIDER, splitPane.getDividers().get(0).positionProperty());
        jsonObject.addProperty(JSON_ORIENTATION, String.valueOf(orientation));
    }

    @Override
    public Element add(Item i, double x, double y)
    {
        if (first.getContent().contains(x, y))
            first = first.add(i, x, y);
        else if (second.getContent().contains(x, y))
            second = second.add(i, x, y);

        updateContent();
        return this;
    }

    @Override
    public Element remove(Item i)
    {
        first = first.remove(i);
        second = second.remove(i);

        if (first == null && second == null)
            return null;
        else if (first == null)
            return second;
        else if (second == null)
            return first;

        updateContent();
        return this;
    }
    @Override
    public Node getContent()
    {
        return splitPane;
    }
    @Override
    public JSONObject getJSONObject()
    {
        return jsonObject;
    }
    private void updateContent()
    {
        splitPane.getItems().setAll(first.getContent(), second.getContent());
    }
}
