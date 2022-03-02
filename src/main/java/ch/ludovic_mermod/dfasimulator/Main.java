package ch.ludovic_mermod.dfasimulator;

import ch.ludovic_mermod.dfasimulator.gui.Controls;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.gui.scene.Link;
import ch.ludovic_mermod.dfasimulator.gui.scene.SimulationPane;
import ch.ludovic_mermod.dfasimulator.gui.scene.StateNode;
import ch.ludovic_mermod.dfasimulator.simulator.Path;
import ch.ludovic_mermod.dfasimulator.simulator.State;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Set;

public class Main extends Application
{
    public static final System.Logger logger = new Logger();
    private SimulationPane simulationPane;
    private MenuBar menuBar;
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        createSimulatorPane();
        createMenuBar();

        BorderPane pane = new BorderPane();
        pane.setCenter(simulationPane);
        pane.setTop(menuBar);


        primaryStage.setScene(new Scene(pane, 800, 600));
        primaryStage.show();
    }

    private void createSimulatorPane()
    {
        State state1 = new State("source");
        var stateNode1 = new StateNode(state1);
        stateNode1.relocate(50, 50);

        State state2 = new State("target");
        var stateNode2 = new StateNode(state2);
        stateNode2.relocate(300, 50);

        var link = new Link(stateNode1, stateNode2, new Path(state1, state2, Set.of('0', '1')));

        simulationPane = new SimulationPane();
        simulationPane.addState(stateNode1);
        simulationPane.addState(stateNode2);
        simulationPane.addLink(link);
    }

    private void createMenuBar()
    {
        menuBar = new MenuBar();

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
    }
}
