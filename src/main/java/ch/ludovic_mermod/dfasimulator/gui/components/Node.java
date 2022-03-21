package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.gui.GraphPane;
import ch.ludovic_mermod.dfasimulator.logic.State;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class Node extends GraphItem
{
    private final Position pos;
    private final State    state;

    private final Circle innerCircle;
    private final Circle outerCircle;

    private ContextMenu menu;
    private MenuItem    deleteMenuItem;


    public Node(State state, GraphPane graphPane)
    {
        super(graphPane);

        this.state = state;

        menu = createContextMenu();
        pos = new Position();

        innerCircle = new Circle();
        innerCircle.radiusProperty().bind(Constants.NODE_INNER_CIRCLE_RADIUS);
        initialBinding().addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        Constants.NODE_CURRENT_COLOR.addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        Constants.NODE_INITIAL_COLOR.addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        Constants.NODE_BASE_COLOR.addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        graphPane.currentStateProperty().addListener((o, ov, nv) -> updateCircleColor(innerCircle));
        updateCircleColor(innerCircle);

        outerCircle = new Circle();
        outerCircle.radiusProperty().bind(IntegerBinding.integerExpression(Constants.NODE_OUTER_CIRCLE_RADIUS));
        outerCircle.setStrokeWidth(5);
        outerCircle.setStroke(Color.BLACK);
        outerCircle.setFill(Color.TRANSPARENT);
        outerCircle.visibleProperty().bind(state.isAcceptingProperty());

        Text text = new Text();
        text.fontProperty().bind(Constants.NODE_NAME_FONT);
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

    protected StringProperty nameProperty()
    {
        return state.nameProperty();
    }
    public BooleanBinding initialBinding()
    {
        return state.isInitialBinding();
    }
    public BooleanProperty acceptingProperty()
    {
        return state.isAcceptingProperty();
    }
    public String getName()
    {
        return state.nameProperty().get();
    }
    public State getState()
    {
        return state;
    }

    public DoubleProperty centerXProperty() {return layoutXProperty();}
    public DoubleProperty centerYProperty() {return layoutYProperty();}
    public ReadOnlyDoubleProperty radiusProperty() {return innerCircle.radiusProperty();}

    protected void bindSimulationPane(GraphPane graphPane)
    {
        deleteMenuItem.disableProperty().bind(graphPane.getSimulationProperty());
    }

    private void updateCircleColor(Circle c)
    {
        c.setFill(graphPane.currentStateProperty().get() == state
                  ? Constants.NODE_CURRENT_COLOR.get()
                  : state.isInitialBinding().get()
                    ? Constants.NODE_INITIAL_COLOR.get()
                    : Constants.NODE_BASE_COLOR.get());
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

        setOnMousePressed(event -> {
            menu.hide();
            if (event.isPrimaryButtonDown())
                switch (graphPane.getTool())
                {
                    case EDIT:
                        graphPane.getMainPane().bindEditPane(state);
                        break;

                    case LINK:
                        break;

                    case DRAG:
                        setCursor(Cursor.CLOSED_HAND);

                        //When a press event occurs, the location coordinates of the event are cached
                        pos.x = event.getX() + getWidth() / 2;
                        pos.y = event.getY() + getHeight() / 2;
                        break;
                }
        });
        setOnMouseReleased(event -> setCursor(Cursor.DEFAULT));

        setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown() && graphPane.getTool() == GraphPane.Tool.DRAG)
            {
                double distanceX = event.getX() - pos.x;
                double distanceY = event.getY() - pos.y;

                double x = getLayoutX() + distanceX;
                double y = getLayoutY() + distanceY;

                relocate(x, y);
            }
        });
    }

    public double getWidth()
    {
        return getBoundsInParent().getWidth();
    }
    public double getHeight()
    {
        return getBoundsInParent().getHeight();
    }

    public DoubleBinding widthBinding()
    {
        return CustomBindings.doubleBinding(this::getWidth, boundsInParentProperty());
    }
    public DoubleBinding heightBinding()
    {
        return CustomBindings.doubleBinding(this::getHeight, boundsInParentProperty());
    }

    private ContextMenu createContextMenu()
    {
        ContextMenu menu = new ContextMenu();

        deleteMenuItem = new MenuItem();
        Strings.bind("delete", deleteMenuItem.textProperty());
        deleteMenuItem.setOnAction(event -> graphPane.getMainPane().getFiniteAutomaton().removeState(getState()));
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
