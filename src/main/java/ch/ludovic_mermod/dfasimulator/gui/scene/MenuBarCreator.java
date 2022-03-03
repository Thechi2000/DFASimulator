package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Controls;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class MenuBarCreator
{
    protected static javafx.scene.control.MenuBar createMenuBar(SimulationPane simulationPane)
    {
        javafx.scene.control.MenuBar menuBar = new javafx.scene.control.MenuBar();

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
}
