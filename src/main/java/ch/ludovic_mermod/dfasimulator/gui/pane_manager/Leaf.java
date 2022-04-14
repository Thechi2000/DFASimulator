package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.json.JSONArray;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.json.JSONPrimitive;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.Arrays;

public class Leaf extends    Element
{
    public static final String JSON_ITEMS = "items";

    private final TabPane    tabPane;
    private final JSONObject object;

    private Leaf()
    {
        tabPane = new TabPane();

        object = new JSONObject();
        object.addProperty("type", "leaf");
        object.add(JSON_ITEMS, new JSONArray());
        tabPane.getTabs().addListener((ListChangeListener<? super Tab>) change -> {
            change.next();
            object.getAsJSONArray(JSON_ITEMS).clear();
            object.getAsJSONArray(JSON_ITEMS).addAll(change.getList().stream().map(t -> new JSONPrimitive(((Item) t.getUserData()).id())).toList());

            if (change.wasRemoved())
                change.getRemoved().forEach(t -> PaneManager.INSTANCE.remove(((Item) t.getUserData())));
        });

        addHandlers();
    }
    public Leaf(Item... item)
    {
        this();
        Arrays.stream(item).forEach(this::addTab);
    }
    public Leaf(String... ids)
    {
        this();
        Arrays.stream(ids).forEach(id -> this.addTab(Item.get(id)));
    }

    public void addTab(Item item)
    {
        Tab tab = new Tab("", item.node());

        Text text = new Text(item.name());
        text.setOnDragDetected(e -> {
            System.out.printf("Drag detected for %s\n", item.name());
            Dragboard db = text.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(item.id());
            db.setContent(clipboardContent);

            e.consume();
        });
        tab.setGraphic(text);

        tab.setUserData(item);
        tabPane.getTabs().add(tab);
    }

    @Override
    public JSONObject getJSONObject()
    {
        return object;
    }

    @Override
    public Element add(Item i, double x, double y)
    {
        if (!getContent().contains(x, y)) return this;

        double relX = (x - tabPane.getLayoutX()) / tabPane.getWidth(),
                relY = (y - tabPane.getLayoutY()) / tabPane.getHeight();

        if (relX <= 0.2)
            return new Fork(new Leaf(i), this, Orientation.HORIZONTAL);
        else if (relX >= 0.8)
            return new Fork(this, new Leaf(i), Orientation.HORIZONTAL);
        else if (relY <= 0.2)
            return new Fork(new Leaf(i), this, Orientation.VERTICAL);
        else if (relY >= 0.8)
            return new Fork(this, new Leaf(i), Orientation.VERTICAL);

        addTab(i);
        return this;
    }
    @Override
    public Element remove(Item i)
    {
        tabPane.getTabs().removeIf(t -> i.equals(t.getUserData()));
        return tabPane.getTabs().isEmpty() ? null : this;
    }

    @Override
    public Parent getContent()
    {
        return tabPane;
    }
}
