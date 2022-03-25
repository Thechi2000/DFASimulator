package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.gui.components.Edge;
import ch.ludovic_mermod.dfasimulator.gui.components.GraphItem;
import ch.ludovic_mermod.dfasimulator.gui.components.SelfEdge;
import ch.ludovic_mermod.dfasimulator.json.JSONArray;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.logic.Simulation;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

public class GraphPane extends Region
{
    protected static final String     JSON_SELF_EDGES = "self_edges";
    protected static final String     JSON_EDGES      = "edges";
    public static final String FONT_SIZE = "graph.font_size";
    private final          JSONObject object;

    private final ObservableSet<Edge>       edges;
    private final ObservableSet<SelfEdge>   selfEdges;
    private final ObjectProperty<GraphItem> focusedItem;

    private final MainPane   mainPane;
    private       Simulation simulation;
    private       Tool       tool;

    private Point2D     menuPosition;
    private ContextMenu menu;


    public GraphPane(MainPane mainPane)
    {
        this.mainPane = mainPane;
        edges = FXCollections.observableSet(new HashSet<>());
        selfEdges = FXCollections.observableSet(new HashSet<>());
        tool = Tool.EDIT;
        focusedItem = new SimpleObjectProperty<>();

        object = new JSONObject();
        object.add(JSON_EDGES, JSONArray.fromObservableSet(edges, Edge::getJSONObject));
        object.add(JSON_SELF_EDGES, JSONArray.fromObservableSet(selfEdges, SelfEdge::getJSONObject));
    }

    public void create(MainPane mainPane)
    {
        simulation = mainPane.getSimulation();
        menu = createContextMenu();

        setOnMousePressed(event -> {
            for (var g : getChildren().stream().filter(n -> n instanceof GraphItem).map(n -> ((GraphItem) n)).toList())
            {
                g.onMousePressed(event);
                if (event.isConsumed()) return;
            }

            menu.hide();
            focusedItem.set(null);
        });
        setOnContextMenuRequested(event ->
        {
            menuPosition = new Point2D(event.getX(), event.getY());
            menu.show(this, event.getScreenX(), event.getScreenY());
        });
    }

    public ReadOnlyObjectProperty<State> currentStateProperty()
    {
        return simulation.currentStateProperty();
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
    public ReadOnlyBooleanProperty resultProperty()
    {
        return simulation.resultProperty();
    }
    public ReadOnlyBooleanProperty simulationEndedProperty()
    {
        return simulation.simulationEndedProperty();
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

    public void addState(State state)
    {
        mainPane.getFiniteAutomaton().states()
                .stream()
                .filter(s -> !s.equals(state))
                .forEach(s ->
                {
                    final Edge e1 = new Edge(state, s, this);
                    edges.add(e1);
                    getChildren().add(e1);

                    final Edge e2 = new Edge(s, state, this);
                    edges.add(e2);
                    getChildren().add(e2);
                });

        SelfEdge selfEdge = new SelfEdge(state, this);
        selfEdges.add(selfEdge);
        getChildren().add(selfEdge);
        getChildren().add(state.getNode());
    }
    public void removeState(State state)
    {
        var edgesToRemove = edges.stream().filter(e -> state.equals(e.source()) || state.equals(e.target()) && getChildren().remove(e)).toList();
        getChildren().removeAll(edgesToRemove);
        edgesToRemove.forEach(edges::remove);

        final List<SelfEdge> selfEdgesToRemove = this.selfEdges.stream().filter(s -> s.state().equals(state)).toList();
        getChildren().removeAll(selfEdgesToRemove);
        selfEdgesToRemove.forEach(this.selfEdges::remove);

        getChildren().remove(state.getNode());
    }

    public JSONObject getJSONObject()
    {
        return object;
    }
    public void loadJSON(JSONObject object) throws IOManager.CorruptedFileException
    {
        object.checkHasArray(JSON_EDGES);
        object.checkHasArray(JSON_SELF_EDGES);

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
            if (!element.isJSONObject()) throw new IOManager.CorruptedFileException("Could not parse \"%s\" into an edge", element);
            obj = element.getAsJSONObject();
            obj.checkHasString(SelfEdge.JSON_STATE);

            SelfEdge edge = selfEdges.stream().filter(e -> e.state().name().equals(obj.get(SelfEdge.JSON_STATE).getAsString())).findAny().orElse(null);
            if (edge == null)
            {
                Main.log(Level.WARNING, "Tried to parse unknown edge at \"%s\"", obj.get(SelfEdge.JSON_STATE));
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
        create.setOnAction(event -> mainPane.getFiniteAutomaton().createNode(menuPosition.getX(), menuPosition.getY()));
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
        return getChildren();
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
