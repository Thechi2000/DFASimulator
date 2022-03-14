package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.CustomBindings;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.logic.Simulation;
import ch.ludovic_mermod.dfasimulator.logic.FiniteAutomaton;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List;

public class MainPane extends BorderPane
{
    private final FiniteAutomaton finiteAutomaton;
    private final IOManager ioManager;
    private final GraphPane graphPane;

    private final ObjectProperty<EditPane> editPaneProperty;
    private final MenuBar menuBar;
    private final ConsolePane consolePane;
    private final SimulationPane simulationPane;
    private final SplitPane rightSplitPane;
    private Simulation simulation;

    public MainPane()
    {
        finiteAutomaton = new FiniteAutomaton(this);
        ioManager = new IOManager(this);
        graphPane = new GraphPane(this);
        simulation = new Simulation(this);

        editPaneProperty = new SimpleObjectProperty<>(null);
        menuBar = new MenuBar();
        consolePane = new ConsolePane();
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

        fillGraphPane();
        //ioManager.open("default.json");

        setRight(rightSplitPane);
        setTop(menuBar);
        setBottom(consolePane);
        setCenter(graphPane);

        getScene().getWindow().setOnCloseRequest(request ->
        {
            if (!ioManager.close())
                request.consume();
        });
        Strings.bindFormat("window.title%s %s", primaryStage.titleProperty(), ioManager.filenameProperty(), CustomBindings.ternary(ioManager.isSavedProperty(), "", "*"));
    }

    public FiniteAutomaton getFiniteAutomaton()
    {
        return finiteAutomaton;
    }
    public IOManager getIoManager()
    {
        return ioManager;
    }
    public GraphPane getGraphPane()
    {
        return graphPane;
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
    public SimulationPane getSimulationStatePane()
    {
        return simulationPane;
    }

    private void fillGraphPane()
    {
        finiteAutomaton.alphabet().addAll(List.of('0', '1'));

        var stateNode1 = new State(finiteAutomaton);
        stateNode1.nameProperty().set("source");
        stateNode1.getNode().relocate(50, 50);
        stateNode1.isAcceptingProperty().set(true);

        var stateNode2 = new State(finiteAutomaton);
        stateNode2.nameProperty().set("target");
        stateNode2.getNode().relocate(300, 50);
        finiteAutomaton.initialStateProperty().set(stateNode2);

        var stateNode3 = new State(finiteAutomaton);
        stateNode3.nameProperty().set("other");
        stateNode3.getNode().relocate(50, 300);


        stateNode1.transitionMap().setValue('0', stateNode2);
        stateNode1.transitionMap().setValue('1', stateNode2);

        stateNode2.transitionMap().setValue('0', stateNode3);
        stateNode2.transitionMap().setValue('1', stateNode3);

        stateNode3.transitionMap().setValue('0', stateNode1);
        stateNode3.transitionMap().setValue('1', stateNode1);


        finiteAutomaton.addState(stateNode1);
        finiteAutomaton.addState(stateNode2);
        finiteAutomaton.addState(stateNode3);

        finiteAutomaton.alphabet().add('2');

        ioManager.saveAs("default.json");
    }

    /*public void bindEditPane(Link link)
    {
        bindEditPane(new EdgeEditPane(simulation, link));
    }*/
    public void bindEditPane(State state)
    {
        bindEditPane(new NodeEditPane(finiteAutomaton, state));
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
