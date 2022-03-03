package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Controls;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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
        MenuBar menuBar = createMenuBar();

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

    private MenuBar createMenuBar()
    {
        MenuBar menuBar = new MenuBar();

        Menu toolsMenu = new Menu();
        Strings.bind("tools", toolsMenu.textProperty());
        menuBar.getMenus().add(toolsMenu);

        MenuItem edit = new MenuItem();
        Strings.bind("edit", edit.textProperty());
        edit.setOnAction(event -> simulationPane.setTool(SimulationPane.Tool.EDIT));
        edit.acceleratorProperty().bind(Controls.editTool);
        toolsMenu.getItems().add(edit);

        MenuItem drag = new MenuItem();
        Strings.bind("drag", drag.textProperty());
        drag.setOnAction(event -> simulationPane.setTool(SimulationPane.Tool.DRAG));
        drag.acceleratorProperty().bind(Controls.dragTool);
        toolsMenu.getItems().add(drag);

        MenuItem link = new MenuItem();
        Strings.bind("link", link.textProperty());
        link.setOnAction(event -> simulationPane.setTool(SimulationPane.Tool.LINK));
        link.acceleratorProperty().bind(Controls.linkTool);
        toolsMenu.getItems().add(link);

        return menuBar;
    }

    protected void bindEditPane(Link link)
    {
        editPaneProperty.set(new LinkEditPane(simulationPane, link));
    }
    protected void bindEditPane(StateNode stateNode)
    {
        editPaneProperty.set(new NodeEditPane(simulationPane, stateNode));
    }
    protected void removeEditPane()
    {
        editPaneProperty.set(null);
    }
}
