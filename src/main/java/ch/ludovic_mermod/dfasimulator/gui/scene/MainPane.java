package ch.ludovic_mermod.dfasimulator.gui.scene;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.Set;

public class MainPane extends BorderPane
{
    private final SimulatorMenuBar simulatorMenuBar;
    private final ConsolePane consolePane;
    private final GraphPane graphPane;
    private final ObjectProperty<Pane> editPaneProperty;

    public MainPane()
    {
        editPaneProperty = new SimpleObjectProperty<>(new Pane());
        graphPane = new GraphPane();
        simulatorMenuBar = new SimulatorMenuBar();
        consolePane = new ConsolePane();

        graphPane.create(this);
        simulatorMenuBar.create(this);
        consolePane.create(this);

        createSimulatorPane();

        setCenter(graphPane);
        setTop(simulatorMenuBar);
        rightProperty().bind(editPaneProperty);
    }
    public SimulatorMenuBar getSimulatorMenuBar()
    {
        return simulatorMenuBar;
    }
    public ConsolePane getConsolePane()
    {
        return consolePane;
    }
    public GraphPane getGraphPane()
    {
        return graphPane;
    }
    public Pane getEditPane()
    {
        return editPaneProperty.get();
    }
    public ObjectProperty<Pane> editPaneProperty()
    {
        return editPaneProperty;
    }
    private void createSimulatorPane()
    {
        var stateNode1 = new StateNode("source");
        stateNode1.relocate(50, 50);

        var stateNode2 = new StateNode("target");
        stateNode2.relocate(300, 50);

        var link = new Link(stateNode1, stateNode2, Set.of('0', '1'));

        graphPane.addState(stateNode1);
        graphPane.addState(stateNode2);
        graphPane.addLink(link);
    }

    protected void bindEditPane(Link link)
    {
        editPaneProperty.set(EditPaneCreator.createLinkEditPane(graphPane, link));
    }
    protected void bindEditPane(StateNode stateNode)
    {
        editPaneProperty.set(EditPaneCreator.createNodeEditPane(graphPane, stateNode));
    }

    protected void removeEditPane()
    {
        editPaneProperty.set(null);
    }
}
