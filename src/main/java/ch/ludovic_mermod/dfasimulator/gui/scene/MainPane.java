package ch.ludovic_mermod.dfasimulator.gui.scene;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.Set;

public class MainPane extends BorderPane
{
    private final ObjectProperty<EditPane> editPaneProperty;
    private final MenuBar menuBar;
    private final ConsolePane consolePane;
    private final GraphPane graphPane;
    private final SimulationPane simulationPane;
    private final SplitPane rightSplitPane;

    public MainPane()
    {
        editPaneProperty = new SimpleObjectProperty<>(null);
        menuBar = new MenuBar();
        consolePane = new ConsolePane();
        graphPane = new GraphPane();
        simulationPane = new SimulationPane();
        rightSplitPane = new SplitPane();

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

        setRight(rightSplitPane);
        setTop(menuBar);
        setBottom(consolePane);
        setCenter(graphPane);
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
        var stateNode1 = new StateNode("source", graphPane);
        stateNode1.relocate(50, 50);
        stateNode1.acceptingProperty().set(true);

        var stateNode2 = new StateNode("target", graphPane);
        stateNode2.relocate(300, 50);
        stateNode2.initialProperty().set(true);

        var stateNode3 = new StateNode("other", graphPane);
        stateNode3.relocate(50, 300);

        var link1 = new Link(stateNode1, stateNode2, Set.of('0', '1'));
        var link2 = new Link(stateNode2, stateNode3, Set.of('0', '1'));
        var link3 = new Link(stateNode3, stateNode1, Set.of('0', '1'));

        graphPane.addState(stateNode1);
        graphPane.addState(stateNode2);
        graphPane.addState(stateNode3);
        graphPane.addLink(link1);
        graphPane.addLink(link2);
        graphPane.addLink(link3);
    }

    protected void bindEditPane(Link link)
    {
        bindEditPane(new LinkEditPane(graphPane, link));
    }
    protected void bindEditPane(StateNode stateNode)
    {
        bindEditPane(new NodeEditPane(graphPane, stateNode));
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
}
