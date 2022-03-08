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
    private final SimulationSettingsPane simulationSettingsPane;
    private final ObjectProperty<Pane> editPaneProperty;
    private final SimulatorMenuBar simulatorMenuBar;
    private final ConsolePane consolePane;
    private final GraphPane graphPane;
    private final SplitPane rightPane;

    public MainPane()
    {
        editPaneProperty = new SimpleObjectProperty<>(null);
        simulationSettingsPane = new SimulationSettingsPane();
        simulatorMenuBar = new SimulatorMenuBar();
        consolePane = new ConsolePane();
        graphPane = new GraphPane();
        rightPane = new SplitPane();

        simulationSettingsPane.create(this);
        simulatorMenuBar.create(this);
        consolePane.create(this);
        graphPane.create(this);

        editPaneProperty.addListener((o, ov, nv) ->
        {
            if (nv != null)
            {
                if (ov == null)
                    rightPane.getItems().add(0, nv);
                else
                    rightPane.getItems().set(0, nv);
            }
        });
        rightPane.setOrientation(Orientation.VERTICAL);
        rightPane.getItems().add(simulationSettingsPane);

        fillGraphPane();

        setRight(rightPane);
        setTop(simulatorMenuBar);
        setBottom(consolePane);
        setCenter(graphPane);
    }

    public SimulationSettingsPane getSimulationSettingsPane()
    {
        return simulationSettingsPane;
    }
    public ObjectProperty<Pane> editPaneProperty()
    {
        return editPaneProperty;
    }
    public SimulatorMenuBar getSimulatorMenuBar()
    {
        return simulatorMenuBar;
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

    private void fillGraphPane()
    {
        var stateNode1 = new StateNode("source", graphPane);
        stateNode1.relocate(50, 50);
        stateNode1.acceptingProperty().set(true);

        var stateNode2 = new StateNode("target", graphPane);
        stateNode2.relocate(300, 50);

        var stateNode3 = new StateNode("other", graphPane);
        stateNode3.relocate(50, 300);

        var link1 = new Link(stateNode1, stateNode2, Set.of('0', '1'));
        var link2 = new Link(stateNode1, stateNode3, Set.of('1'));

        graphPane.addState(stateNode1);
        graphPane.addState(stateNode2);
        graphPane.addState(stateNode3);
        graphPane.addLink(link1);
        graphPane.addLink(link2);
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
