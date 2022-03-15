package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.gui.scene.components.Edge;
import ch.ludovic_mermod.dfasimulator.logic.Simulation;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

import java.util.HashSet;
import java.util.Set;

public class GraphPane extends Region
{
    private final Set<Edge> edges;

    private       ContextMenu menu;
    private final MainPane    mainPane;

    private Point2D    menuPosition;
    private Tool       tool;
    private Simulation simulation;

    public GraphPane(MainPane mainPane)
    {
        this.mainPane = mainPane;
        edges = new HashSet<>();
        tool = Tool.EDIT;
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
        getChildren().add(state.getNode());
    }
    public void removeState(State state)
    {
        var l = edges.stream().filter(e -> state.equals(e.source()) || state.equals(e.target()) && getChildren().remove(e)).toList();
        getChildren().removeAll(l);
        l.forEach(edges::remove);

        getChildren().remove(state.getNode());
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

    public enum Tool
    {
        EDIT,
        DRAG,
        LINK
    }

}
