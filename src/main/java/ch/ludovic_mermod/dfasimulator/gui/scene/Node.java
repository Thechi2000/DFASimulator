package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.Link;
import ch.ludovic_mermod.dfasimulator.logic.State;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class Node extends StackPane
{
    private final Position pos;
    private final ContextMenu menu;
    private final GraphPane graphPane;
    private final State state;
    private MenuItem deleteMenuItem;

    public Node(String name, GraphPane graphPane)
    {
        state = new State(name, this);
        this.graphPane = graphPane;

        setAlignment(Pos.CENTER);

        menu = createContextMenu();
        pos = new Position();

        Circle innerCircle = new Circle();
        innerCircle.radiusProperty().bind(Constants.Node.Circle.radius);
        state.initialProperty().addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        Constants.Node.Circle.currentColor.addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        Constants.Node.Circle.initialColor.addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        Constants.Node.Circle.color.addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        graphPane.currentStateProperty().addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        updateCircleColor(innerCircle);

        Circle outerCircle = new Circle();
        outerCircle.radiusProperty().bind(IntegerBinding.integerExpression(Constants.Node.Circle.radius).add(10));
        outerCircle.setStrokeWidth(5);
        outerCircle.setStroke(Color.BLACK);
        outerCircle.setFill(Color.TRANSPARENT);
        outerCircle.visibleProperty().bind(state.acceptingProperty());

        Text text = new Text();
        text.fontProperty().bind(Constants.Node.Text.font);
        text.textProperty().bind(state.nameProperty());
        text.setTextAlignment(TextAlignment.CENTER);

        getChildren().addAll(outerCircle, innerCircle, text);
        addEventHandlers();
        setOnContextMenuRequested(event ->
        {
            menu.show(this, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    public static Node fromJSONObject(JsonObject object, GraphPane graphPane)
    {
        Node node = new Node(object.get("name").getAsString(), graphPane);
        node.state.initialProperty().set(object.get("initial").getAsBoolean());
        node.state.acceptingProperty().set(object.get("accepting").getAsBoolean());
        node.relocate(object.get("x_coord").getAsDouble(), object.get("y_coord").getAsDouble());
        return node;
    }
    public JsonElement toJSONObject()
    {
        JsonObject object = new JsonObject();
        object.addProperty("name", state.nameProperty().get());
        object.addProperty("initial", state.initialProperty().get());
        object.addProperty("accepting", state.acceptingProperty().get());
        object.addProperty("x_coord", getLayoutX());
        object.addProperty("y_coord", getLayoutY());
        return object;
    }

    protected StringProperty nameProperty()
    {
        return state.nameProperty();
    }
    public ListProperty<Link> outgoingLinksProperty()
    {
        return state.outgoingLinksProperty();
    }
    public BooleanProperty initialProperty()
    {
        return state.initialProperty();
    }
    public BooleanProperty acceptingProperty()
    {
        return state.acceptingProperty();
    }
    public String getName()
    {
        return state.nameProperty().get();
    }
    public State getState()
    {
        return state;
    }

    protected void bindSimulationPane(GraphPane graphPane)
    {
        deleteMenuItem.disableProperty().bind(graphPane.getSimulationProperty());
    }

    private void updateCircleColor(Circle c)
    {
        c.setFill(graphPane.currentStateProperty().get() == state
                ? Constants.Node.Circle.currentColor.get()
                : state.initialProperty().get()
                ? Constants.Node.Circle.initialColor.get()
                : Constants.Node.Circle.color.get());
    }

    private void addEventHandlers()
    {
        setOnMouseEntered(event ->
                setCursor(switch (graphPane.getTool())
                        {
                            case EDIT -> Cursor.DEFAULT;
                            case DRAG, LINK -> Cursor.HAND;
                        }));
        setOnMouseExited(event -> setCursor(Cursor.DEFAULT));

        setOnMousePressed(event ->
        {
            menu.hide();
            if (event.isPrimaryButtonDown())
                switch (graphPane.getTool())
                {
                    case EDIT:
                        graphPane.getMainPane().bindEditPane(this);
                        break;

                    case LINK:
                        break;

                    case DRAG:
                        setCursor(Cursor.CLOSED_HAND);

                        //When a press event occurs, the location coordinates of the event are cached
                        pos.x = event.getX();
                        pos.y = event.getY();
                        break;
                }
        });
        setOnMouseReleased(event -> setCursor(Cursor.DEFAULT));

        setOnMouseDragged(event ->
        {
            if (event.isPrimaryButtonDown() && graphPane.getTool() == GraphPane.Tool.DRAG)
            {
                double distanceX = event.getX() - pos.x;
                double distanceY = event.getY() - pos.y;

                double x = getLayoutX() + distanceX;
                double y = getLayoutY() + distanceY;

                relocate(x, y);
            }
        });


        setOnDragDetected(event ->
        {
            if (event.isPrimaryButtonDown() && graphPane.getTool() == GraphPane.Tool.LINK)
            {
                Dragboard db = startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString(state.nameProperty().get());
                db.setContent(content);
            }

            event.consume();
        });
        setOnDragOver(event ->
        {
            if (event.getGestureSource() != this && event.getDragboard().hasString())
                event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        });
        setOnDragDropped(event ->
        {
            if (getParent() instanceof GraphPane && event.getGestureSource() instanceof Node)
                ((GraphPane) getParent()).createLink(event.getDragboard().getString(), state.nameProperty().get());
        });
    }

    private ContextMenu createContextMenu()
    {
        ContextMenu menu = new ContextMenu();

        deleteMenuItem = new MenuItem();
        Strings.bind("delete", deleteMenuItem.textProperty());
        deleteMenuItem.setOnAction(event -> ((GraphPane) getParent()).deleteNode(this));
        //delete.disableProperty().bind(graphPane.getSimulationProperty());
        menu.getItems().add(deleteMenuItem);

        return menu;
    }

    private static class Position
    {
        double x;
        double y;
    }

}
