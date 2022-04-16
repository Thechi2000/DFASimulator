package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.constants.Resources;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.gui.pane_manager.Element;
import ch.ludovic_mermod.dfasimulator.gui.pane_manager.Item;
import ch.ludovic_mermod.dfasimulator.gui.pane_manager.Leaf;
import ch.ludovic_mermod.dfasimulator.gui.pane_manager.PaneManager;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.logic.FiniteAutomaton;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.logic.Simulation;
import ch.ludovic_mermod.dfasimulator.logic.State;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainPane extends BorderPane
{
    private final FiniteAutomaton finiteAutomaton;
    private final IOManager       ioManager;
    private final GraphPane       graphPane;

    private final ObjectProperty<EditPane> editPaneProperty;
    private final MenuBar                  menuBar;
    private final ConsolePane              consolePane;
    private final SimulationPane           simulationPane;
    private final SplitPane                rightSplitPane;
    private final Simulation               simulation;

    public MainPane()
    {
        editPaneProperty = new SimpleObjectProperty<>(null);
        menuBar = new MenuBar();
        consolePane = new ConsolePane();
        simulationPane = new SimulationPane();
        rightSplitPane = new SplitPane();

        finiteAutomaton = new FiniteAutomaton();
        graphPane = new GraphPane();
        ioManager = new IOManager(this);
        simulation = new Simulation(this);

        Item.register(new Item(consolePane, "Console", "console"));
        Item.register(new Item(new ScrollPane(graphPane), "Graph", "graph"));
        Item.register(new Item(simulationPane, "Simulation", "simulation"));
        Item.register(new Item(new TestPane(this), "Test", "test"));
        Item.register(new Item(new TablePane(finiteAutomaton), "Table", "table"));
    }

    public void create(Stage primaryStage)
    {
        menuBar.create(this);
        consolePane.create(this);
        graphPane.create(this);
        simulationPane.create(this);
        finiteAutomaton.create(this);

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

        ioManager.open(Resources.get("default.json"));

        setTop(menuBar);
        Strings.bindFormat("window.title", primaryStage.titleProperty(), ioManager.filenameProperty(), CustomBindings.ternary(ioManager.isSavedProperty(), "", "*"));

        PaneManager.INSTANCE.load(primaryStage, this);
        centerProperty().bind(PaneManager.INSTANCE.getMainLayout().getContentBinding());

        primaryStage.setOnCloseRequest(request ->
        {
            PaneManager.INSTANCE.save();
            if (!ioManager.close())
                request.consume();
        });
    }

    public Element getLayout()
    {
        return PaneManager.INSTANCE.getMainLayout();
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


        stateNode1.transitionMap().setValue('0', new ArrayList<>(List.of(stateNode2)));
        stateNode1.transitionMap().setValue('1', new ArrayList<>(List.of(stateNode2)));

        stateNode2.transitionMap().setValue('0', new ArrayList<>(List.of(stateNode3)));
        stateNode2.transitionMap().setValue('1', new ArrayList<>(List.of(stateNode3)));

        stateNode3.transitionMap().setValue('0', new ArrayList<>(List.of(stateNode1)));
        stateNode3.transitionMap().setValue('1', new ArrayList<>(List.of(stateNode1)));


        finiteAutomaton.addState(stateNode1);
        finiteAutomaton.addState(stateNode2);
        finiteAutomaton.addState(stateNode3);

        finiteAutomaton.alphabet().add('2');

        ioManager.saveAs("default.json");
    }

    public void bindEditPane(State state)
    {
        editPaneProperty.set(new EditPane(finiteAutomaton, state));
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
