package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.json.JSONArray;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.json.JSONPrimitive;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

import java.util.Arrays;

import static ch.ludovic_mermod.dfasimulator.gui.pane_manager.Direction.*;

public class Leaf extends Element
{
    public static final String JSON_ITEMS = "items";

    private static final double DRAG_HITBOX_FACTOR = 0.2;
    public static final  int    TAB_HEIGHT         = 30;

    private final StackPane stackPane;
    private final Pane      overlayPane;
    private final TabPane   tabPane;

    private Leaf()
    {
        overlayPane = new Pane();
        tabPane = new TabPane();
        stackPane = new StackPane(tabPane, overlayPane);
        overlayPane.setMouseTransparent(true);

        tabPane.getTabs().addListener((ListChangeListener<? super Tab>) change -> {
            change.next();
            if (change.wasRemoved())
                change.getRemoved().forEach(t -> PaneManager.INSTANCE.remove(((Item) t.getUserData())));
        });

        addHandlers();
        stackPane.setOnDragOver(e -> {
            if (e.getDragboard().hasString() && tabPane.getTabs().stream().map(t -> ((Item) t.getUserData())).noneMatch(i -> i.id().equals(e.getDragboard().getString())))
            {
                e.acceptTransferModes(TransferMode.ANY);
                overlayPane.getChildren().clear();
                for (var o : Direction.values())
                {
                    var p = getSideHitbox(o);
                    p.setFill(Paint.valueOf("FF000488"));
                    if (p.contains(e.getX(), e.getY()))
                        overlayPane.getChildren().add(p);
                }
            }
            e.consume();
        });
        stackPane.setOnDragExited(e -> overlayPane.getChildren().clear());
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

    private MoveTo mt(Point2D to) {return new MoveTo(to.getX(), to.getY());}
    private Polygon polygon(Point2D... points)
    {
        Polygon polygon = new Polygon();
        Arrays.stream(points).forEach(p -> polygon.getPoints().addAll(p.getX(), p.getY()));
        return polygon;
    }

    private Polygon getSideHitbox(Direction direction)
    {
        double x = 0, y = TAB_HEIGHT,
                w = getContent().getLayoutBounds().getWidth(), h = getContent().getLayoutBounds().getHeight(),
                f = DRAG_HITBOX_FACTOR, of = 1 - f;

        Point2D tl = new Point2D(x, y),
                tr = new Point2D(x + w, y),
                bl = new Point2D(x, h),
                br = new Point2D(w, h),

                itl = new Point2D(x + f * w, y + f * h),
                itr = new Point2D(x + of * w, y + f * h),
                ibl = new Point2D(f * w, of * h),
                ibr = new Point2D(of * w, of * h);

        return switch (direction)
                {
                    case UP -> polygon(tl, tr, itr, itl);
                    case RIGHT -> polygon(tr, br, ibr, itr);
                    case DOWN -> polygon(bl, br, ibr, ibl);
                    case LEFT -> polygon(tl, bl, ibl, itl);
                };
    }
    private Polygon getTabsHitbox()
    {
        double x = 0, y = 0, w = getContent().getLayoutBounds().getWidth(), h = TAB_HEIGHT;
        return polygon(new Point2D(x, y), new Point2D(x + w, y), new Point2D(x + w, y + h), new Point2D(x, y + h));
    }

    @Override
    public JSONObject getJSONObject()
    {
        var object = new JSONObject();
        object.addProperty("type", "leaf");

        JSONArray array = new JSONArray();
        object.add(JSON_ITEMS, array);

        tabPane.getTabs().forEach(t -> array.add(((Item) t.getUserData()).id()));
        return object;
    }

    @Override
    protected void removeChild(Element child)
    {
        tabPane.getTabs().removeIf(t -> t.getContent() == child.getContent());
    }

    @Override
    protected void update(Element oldValue, Element newValue)
    {
        throw new UnsupportedOperationException();
    }
    @Override
    public void add(Item i, double x, double y)
    {
        if (!getContent().contains(x, y)) return;

        System.out.printf("%s, %s\n", x, y);

        if (getTabsHitbox().contains(x, y))
            addTab(i);
        else if (getSideHitbox(LEFT).contains(x, y))
            parent.update(this, new Fork(new Leaf(i), this, Orientation.HORIZONTAL));
        else if (getSideHitbox(RIGHT).contains(x, y))
            parent.update(this, new Fork(this, new Leaf(i), Orientation.HORIZONTAL));
        else if (getSideHitbox(UP).contains(x, y))
            parent.update(this, new Fork(new Leaf(i), this, Orientation.VERTICAL));
        else if (getSideHitbox(DOWN).contains(x, y))
            parent.update(this, new Fork(this, new Leaf(i), Orientation.VERTICAL));
        else
            PaneManager.INSTANCE.moveToWindow(i);
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
        return stackPane;
    }
}
