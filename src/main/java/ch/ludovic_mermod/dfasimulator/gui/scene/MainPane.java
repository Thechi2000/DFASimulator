package ch.ludovic_mermod.dfasimulator.gui.scene;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.Set;

public class MainPane extends BorderPane
{
    private final SimulationPane simulationPane;
    private final ObjectProperty<Pane> editPaneProperty;

    public MainPane()
    {
        editPaneProperty = new SimpleObjectProperty<>(new Pane());

        simulationPane = createSimulatorPane();
        MenuBar menuBar = MenuBarCreator.createMenuBar(simulationPane);

        setCenter(simulationPane);
        setTop(menuBar);
        rightProperty().bind(editPaneProperty);
    }

    private SimulationPane createSimulatorPane()
    {
        var stateNode1 = new StateNode("source");
        stateNode1.relocate(50, 50);

        var stateNode2 = new StateNode("target");
        stateNode2.relocate(300, 50);

        var link = new Link(stateNode1, stateNode2, Set.of('0', '1'));

        SimulationPane simulationPane = new SimulationPane();
        simulationPane.addState(stateNode1);
        simulationPane.addState(stateNode2);
        simulationPane.addLink(link);

        return simulationPane;
    }

    protected void bindEditPane(Link link)
    {
        editPaneProperty.set(EditPaneCreator.createLinkEditPane(simulationPane, link));
    }
    protected void bindEditPane(StateNode stateNode)
    {
        editPaneProperty.set(EditPaneCreator.createNodeEditPane(simulationPane, stateNode));
    }

    protected void removeEditPane()
    {
        editPaneProperty.set(null);
    }
}
