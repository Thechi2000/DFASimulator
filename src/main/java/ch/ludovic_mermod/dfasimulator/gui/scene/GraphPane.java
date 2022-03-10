package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.Link;
import ch.ludovic_mermod.dfasimulator.logic.Simulation;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

public class GraphPane extends Region
{
    private final ListProperty<Node> nodes;
    private final ListProperty<Edge> edges;

    private final Simulation simulation;

    private ContextMenu menu;
    private MainPane mainPane;

    private Point2D menuPosition;
    private Tool tool;

    public GraphPane(Simulation simulation)
    {
        this.simulation = simulation;
        nodes = new SimpleListProperty<>(FXCollections.observableArrayList());
        edges = new SimpleListProperty<>(FXCollections.observableArrayList());

        tool = Tool.EDIT;
    }

    public void create(MainPane mainPane)
    {
        this.mainPane = mainPane;
        menu = createContextMenu();

        setOnMousePressed(event -> menu.hide());
        setOnContextMenuRequested(event ->
        {
            menuPosition = new Point2D(event.getX(), event.getY());
            menu.show(this, event.getScreenX(), event.getScreenY());
        });
    }

    public ReadOnlyListProperty<Node> getNodes()
    {
        return nodes;
    }
    public ReadOnlyListProperty<Edge> getEdges()
    {
        return edges;
    }

    public ReadOnlyObjectProperty<State> currentStateProperty()
    {
        return simulation.currentStateProperty();
    }
    public ReadOnlyObjectProperty<Link> lastUsedLinkProperty()
    {
        return simulation.lastUsedLinkProperty();
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

    public Tool getTool()
    {
        return tool;
    }
    public void setTool(Tool tool)
    {
        this.tool = tool;
    }

    public ReadOnlyBooleanProperty getSimulationProperty()
    {
        return simulation.isSimulatingProperty();
    }

    protected boolean hasNode(String name)
    {
        return nodes.stream().anyMatch(n -> n.getName().equals(name));
    }
    protected Node getNode(String name)
    {
        return nodes.stream().filter(n -> n.getName().equals(name)).findAny().orElse(null);
    }

    private ContextMenu createContextMenu()
    {
        ContextMenu menu = new ContextMenu();

        MenuItem create = new MenuItem();
        Strings.bind("create", create.textProperty());
        create.setOnAction(event -> simulation.createNode(menuPosition.getX(), menuPosition.getY()));
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

    public Simulation getSimulation()
    {
        return simulation;
    }

    public enum Tool
    {
        EDIT,
        DRAG,
        LINK
    }

}
