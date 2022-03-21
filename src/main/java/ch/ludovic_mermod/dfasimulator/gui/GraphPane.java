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
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

import java.util.HashSet;
import java.util.logging.Level;

public class GraphPane extends Region
{
    private final ObservableSet<Edge>            edges;
    private final ObservableMap<State, SelfEdge> selfEdges;
    private final ObjectProperty<GraphItem>      focusedItem;

    private final MainPane   mainPane;
    private       Simulation simulation;
    private       Tool       tool;

    private Point2D     menuPosition;
    private ContextMenu menu;


    public GraphPane(MainPane mainPane)
    {
        this.mainPane = mainPane;
        edges = FXCollections.observableSet(new HashSet<>());
        selfEdges = FXCollections.observableHashMap();
        tool = Tool.EDIT;
        focusedItem = new SimpleObjectProperty<>();
    }

    public void create(MainPane mainPane)
    {
        simulation = mainPane.getSimulation();
        menu = createContextMenu();

        setOnMousePressed(event -> menu.hide());
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
        selfEdges.put(state, selfEdge);
        getChildren().add(selfEdge);
        getChildren().add(state.getNode());
    }
    public void removeState(State state)
    {
        var l = edges.stream().filter(e -> state.equals(e.source()) || state.equals(e.target()) && getChildren().remove(e)).toList();
        getChildren().removeAll(l);
        l.forEach(edges::remove);

        getChildren().remove(selfEdges.get(state));
        selfEdges.remove(state);

        getChildren().remove(state.getNode());
    }

    public void loadEdges(JSONArray array) throws IOManager.CorruptedFileException
    {
        for (JSONElement element : array)
        {
            JSONObject o;
            if (!element.isJSONObject()) throw new IOManager.CorruptedFileException("Could not parse \"%s\" into an edge", element);

            o = element.getAsJSONObject();
            o.checkHasString("source");
            o.checkHasString("target");
            o.checkHasNumber("control_x");
            o.checkHasNumber("control_y");


            Edge edge = edges.stream().filter(e -> e.getSourceName().equals(o.get("source").getAsString()) && e.getTargetName().equals(o.get("target").getAsString())).findAny().orElse(null);

            if (edge == null)
            {
                Main.log(Level.WARNING, "Tried to parse unknown edge between \"%s\" and \"%s\"", o.get("source"), o.get("target"));
                return;
            }

            edge.setControlPoint(o.get("control_x").getAsDouble(), o.get("control_y").getAsDouble());
        }
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
