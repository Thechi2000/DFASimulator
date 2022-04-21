package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.constants.settings.Settings;
import ch.ludovic_mermod.dfasimulator.gui.components.Edge;
import ch.ludovic_mermod.dfasimulator.gui.components.GraphItem;
import ch.ludovic_mermod.dfasimulator.gui.components.Node;
import ch.ludovic_mermod.dfasimulator.gui.components.SelfEdge;
import ch.ludovic_mermod.dfasimulator.json.JSONArray;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.logic.Simulation;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

/**
 * Pane to display a FiniteAutomaton
 */
public class GraphPane extends ScrollPane
{
    public static final    String     FONT_SIZE       = "graph.font_size";
    public static final    String     JSON_NODES      = "nodes";
    protected static final String     JSON_SELF_EDGES = "self_edges";
    protected static final String     JSON_EDGES      = "edges";
    private final          JSONObject object;

    private final ObservableSet<Edge>       edges;
    private final ObservableSet<SelfEdge>   selfEdges;
    private final ObservableSet<Node>       nodes;
    private final ObjectProperty<GraphItem> focusedItem;

    private MainPane   mainPane;
    private Simulation simulation;
    private Tool       tool;

    private Point2D     menuPosition;
    private ContextMenu menu;

    private final Pane pane;

    /**
     * Constructs an empty GraphPane
     */
    public GraphPane()
    {
        edges = FXCollections.observableSet(new HashSet<>());
        selfEdges = FXCollections.observableSet(new HashSet<>());
        nodes = FXCollections.observableSet(new HashSet<>());
        pane = new Pane();

        tool = Tool.EDIT;
        focusedItem = new SimpleObjectProperty<>();

        object = new JSONObject();
        object.add(JSON_EDGES, JSONArray.fromObservableSet(edges, Edge::getJSONObject));
        object.add(JSON_SELF_EDGES, JSONArray.fromObservableSet(selfEdges, SelfEdge::getJSONObject));
        object.add(JSON_NODES, new JSONArray());

        setContent(pane);

        pane.minWidthProperty().bind(widthProperty());
        pane.minHeightProperty().bind(heightProperty());
        pane.getStyleClass().add("background");
    }

    public void create(MainPane mainPane)
    {
        this.mainPane = mainPane;
        simulation = mainPane.getSimulation();
        menu = createContextMenu();

        mainPane.getFiniteAutomaton().states().addListener((ListChangeListener<? super State>) change -> {
            change.next();
            if (change.wasAdded())
                change.getAddedSubList().forEach(this::addState);

            if (change.wasRemoved())
                change.getRemoved().forEach(this::removeState);
        });

        pane.setOnMousePressed(event -> {
            for (var g : pane.getChildren().stream().filter(n -> n instanceof GraphItem).map(n -> ((GraphItem) n)).toList())
            {
                g.onMousePressed(event);
                if (event.isConsumed()) return;
            }

            menu.hide();
            focusedItem.set(null);
        });
        pane.setOnContextMenuRequested(event ->
        {
            menuPosition = new Point2D(event.getX(), event.getY());
            menu.show(pane, event.getScreenX(), event.getScreenY());
        });
    }

    public ReadOnlyStringProperty remainingInputProperty()
    {
        return simulation.remainingInputProperty();
    }
    public ReadOnlyBooleanProperty isSimulatingProperty()
    {
        return simulation.isSimulatingProperty();
    }
    public ReadOnlyStringProperty initialInputProperty()
    {
        return simulation.initialInputProperty();
    }
    public ReadOnlyBooleanProperty getSimulationProperty()
    {
        return simulation.isSimulatingProperty();
    }

    public Tool getTool()
    {
        return tool;
    }
    public void setTool(Tool tool)
    {
        this.tool = tool;
    }

    private void addState(State state)
    {
        mainPane.getFiniteAutomaton().states()
                .stream()
                .filter(s -> !s.equals(state))
                .forEach(s ->
                {
                    final Edge e1 = new Edge(state, s, this);
                    edges.add(e1);
                    pane.getChildren().add(e1);

                    final Edge e2 = new Edge(s, state, this);
                    edges.add(e2);
                    pane.getChildren().add(e2);
                });

        SelfEdge selfEdge = new SelfEdge(state, this);
        selfEdges.add(selfEdge);
        object.getAsJSONArray(JSON_NODES).add(state.getNode().getJSONObject());
        pane.getChildren().add(selfEdge);
        pane.getChildren().add(state.getNode());
        nodes.add(state.getNode());
    }
    private void removeState(State state)
    {
        var edgesToRemove = edges.stream().filter(e -> state.equals(e.source()) || state.equals(e.target()) && pane.getChildren().remove(e)).toList();
        pane.getChildren().removeAll(edgesToRemove);
        edgesToRemove.forEach(edges::remove);

        final List<SelfEdge> selfEdgesToRemove = this.selfEdges.stream().filter(s -> s.state().equals(state)).toList();
        pane.getChildren().removeAll(selfEdgesToRemove);
        selfEdgesToRemove.forEach(this.selfEdges::remove);

        object.getAsJSONArray(JSON_NODES).remove(state.getNode().getJSONObject());
        pane.getChildren().remove(state.getNode());
        nodes.remove(state.getNode());
    }

    public JSONObject getJSONObject()
    {
        return object;
    }

    public void loadJSON(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasArray(JSON_EDGES);
        object.checkHasArray(JSON_SELF_EDGES);
        object.checkHasArray(JSON_NODES);

        for (JSONElement element : object.getAsJSONArray(JSON_NODES))
        {
            JSONObject obj;
            if (!element.isJSONObject()) throw new IOManager.CorruptedFileException("Could not parse \"%s\" into a node", element);
            obj = element.getAsJSONObject();
            obj.checkHasString(Node.JSON_STATE);

            Node node = nodes.stream().filter(n -> n.getState().name().equals(obj.get(Node.JSON_STATE).getAsString())).findAny().orElse(null);
            if (node == null)
            {
                Main.log(Level.WARNING, "Tried to parse unknown node at \"%s\"", obj.get(SelfEdge.JSON_STATE));
                return;
            }

            node.loadFromJSONObject(obj);
        }

        for (JSONElement element : object.getAsJSONArray(JSON_EDGES))
        {
            JSONObject obj;
            if (!element.isJSONObject()) throw new IOManager.CorruptedFileException("Could not parse \"%s\" into an edge", element);
            obj = element.getAsJSONObject();
            obj.checkHasString(Edge.JSON_SOURCE);
            obj.checkHasString(Edge.JSON_TARGET);

            Edge edge = edges.stream().filter(e -> e.getSourceName().equals(obj.get(Edge.JSON_SOURCE).getAsString()) && e.getTargetName().equals(obj.get(Edge.JSON_TARGET).getAsString())).findAny().orElse(null);
            if (edge == null)
            {
                Main.log(Level.WARNING, "Tried to parse unknown edge between \"%s\" and \"%s\"", obj.get(Edge.JSON_SOURCE), obj.get(Edge.JSON_TARGET));
                return;
            }

            edge.loadJSONObject(obj);
        }

        for (JSONElement element : object.getAsJSONArray(JSON_SELF_EDGES))
        {
            JSONObject obj;
            if (!element.isJSONObject()) throw new IOManager.CorruptedFileException("Could not parse \"%s\" into a self edge", element);
            obj = element.getAsJSONObject();
            obj.checkHasString(SelfEdge.JSON_STATE);

            SelfEdge edge = selfEdges.stream().filter(e -> e.state().name().equals(obj.get(SelfEdge.JSON_STATE).getAsString())).findAny().orElse(null);
            if (edge == null)
            {
                Main.log(Level.WARNING, "Tried to parse unknown self edge at \"%s\"", obj.get(SelfEdge.JSON_STATE));
                return;
            }

            edge.loadJSONObject(obj);
        }
    }

    public void grantFocus(GraphItem item)
    {
        focusedItem.set(item);
    }

    private ContextMenu createContextMenu()
    {
        ContextMenu menu = new ContextMenu();

        MenuItem create = new MenuItem();
        Strings.bind("create", create.textProperty());
        create.setOnAction(event -> mainPane.getFiniteAutomaton().addState(menuPosition.getX(), menuPosition.getY()));
        create.disableProperty().bind(simulation.isSimulatingProperty());
        menu.getItems().add(create);

        return menu;
    }

    public MainPane getMainPane()
    {
        return mainPane;
    }
    public ObservableList<javafx.scene.Node> children()
    {
        return pane.getChildren();
    }
    public ObservableSet<Edge> edges()
    {
        return edges;
    }
    public ObservableSet<SelfEdge> selfEdges()
    {
        return selfEdges;
    }
    public ReadOnlyObjectProperty<GraphItem> focusedItemProperty()
    {
        return focusedItem;
    }

    public enum Tool
    {
        EDIT,
        DRAG,
        LINK
    }
}
