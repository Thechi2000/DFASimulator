package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Controls;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class SimulatorMenuBar extends MenuBar
{

    public void create(MainPane mainPane)
    {
        var graphPane = mainPane.getGraphPane();

        Menu toolsMenu = new Menu();
        Strings.bind("tools", toolsMenu.textProperty());
        getMenus().add(toolsMenu);

        MenuItem edit = new MenuItem();
        Strings.bind("edit", edit.textProperty());
        edit.setOnAction(event -> graphPane.setTool(GraphPane.Tool.EDIT));
        edit.acceleratorProperty().bind(Controls.editTool);
        toolsMenu.getItems().add(edit);

        MenuItem drag = new MenuItem();
        Strings.bind("drag", drag.textProperty());
        drag.setOnAction(event -> graphPane.setTool(GraphPane.Tool.DRAG));
        drag.acceleratorProperty().bind(Controls.dragTool);
        toolsMenu.getItems().add(drag);

        MenuItem link = new MenuItem();
        Strings.bind("link", link.textProperty());
        link.setOnAction(event -> graphPane.setTool(GraphPane.Tool.LINK));
        link.acceleratorProperty().bind(Controls.linkTool);
        toolsMenu.getItems().add(link);

        MenuItem test = new MenuItem("test");
        test.setOnAction(event -> mainPane.getGraphPane().compileDFA());
        toolsMenu.getItems().add(test);
    }
}
