package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.CustomBindings;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.Link;
import ch.ludovic_mermod.dfasimulator.logic.Simulation;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Set;

public class MainPane extends BorderPane
{
    private final Simulation simulation;

    private final ObjectProperty<EditPane> editPaneProperty;
    private final MenuBar menuBar;
    private final ConsolePane consolePane;
    private final GraphPane graphPane;
    private final SimulationPane simulationPane;
    private final SplitPane rightSplitPane;

    public MainPane()
    {
        simulation = new Simulation();

        editPaneProperty = new SimpleObjectProperty<>(null);
        menuBar = new MenuBar();
        consolePane = new ConsolePane();
        graphPane = simulation.getGraphPane();
        simulationPane = new SimulationPane();
        rightSplitPane = new SplitPane();
    }

    public void create(Stage primaryStage)
    {
        menuBar.create(this);
        consolePane.create(this);
        graphPane.create(this);
        simulationPane.create(this);

        editPaneProperty.addListener((o, ov, nv) ->
        {
            if (nv != null)
            {
                if (ov == null)
                    rightSplitPane.getItems().add(0, nv);
                else
                    rightSplitPane.getItems().set(0, nv);
            }
        });
        rightSplitPane.setOrientation(Orientation.VERTICAL);
        rightSplitPane.getItems().addAll(simulationPane);

        //fillGraphPane();
        simulation.ioManager().open("default.json");

        setRight(rightSplitPane);
        setTop(menuBar);
        setBottom(consolePane);
        setCenter(graphPane);

        getScene().getWindow().setOnCloseRequest(request -> {
            if(!simulation.ioManager().close())
                request.consume();
        });
        Strings.bindFormat("window.title%s %s", primaryStage.titleProperty(), simulation.ioManager().filenameProperty(), CustomBindings.ternary(simulation.ioManager().isSavedProperty(), "", "*"));
    }

    public ObjectProperty<EditPane> editPaneProperty()
    {
        return editPaneProperty;
    }
    public MenuBar getSimulatorMenuBar()
    {
        return menuBar;
    }
    public Pane getEditPane()
    {
        return editPaneProperty.get();
    }
    public ConsolePane getConsolePane()
    {
        return consolePane;
    }
    public GraphPane getGraphPane()
    {
        return graphPane;
    }
    public SimulationPane getSimulationStatePane()
    {
        return simulationPane;
    }

    private void fillGraphPane()
    {
        var stateNode1 = new State("source", simulation);
        stateNode1.getNode().relocate(50, 50);
        stateNode1.acceptingProperty().set(true);

        var stateNode2 = new State("target", simulation);
        stateNode2.getNode().relocate(300, 50);
        stateNode2.initialProperty().set(true);

        var stateNode3 = new State("other", simulation);
        stateNode3.getNode().relocate(50, 300);

        var link1 = new Link(stateNode1, stateNode2, Set.of('0', '1'), simulation);
        var link2 = new Link(stateNode2, stateNode3, Set.of('0', '1'), simulation);
        var link3 = new Link(stateNode3, stateNode1, Set.of('0', '1'), simulation);

        simulation.addState(stateNode1);
        simulation.addState(stateNode2);
        simulation.addState(stateNode3);
        simulation.addLink(link1);
        simulation.addLink(link2);
        simulation.addLink(link3);

        simulation.ioManager().saveAs("default.json");
    }

    public void bindEditPane(Link link)
    {
        bindEditPane(new EdgeEditPane(simulation, link));
    }
    public void bindEditPane(State state)
    {
        bindEditPane(new NodeEditPane(simulation, state));
    }
    private void bindEditPane(EditPane pane)
    {
        if (editPaneProperty.get() != null) editPaneProperty.get().unbind();
        editPaneProperty.set(pane);
    }

    protected void removeEditPane()
    {
        editPaneProperty.set(null);
    }
    public Simulation getSimulation()
    {
        return simulation;
    }
}
