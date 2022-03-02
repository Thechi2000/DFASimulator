package ch.ludovic_mermod.dfasimulator;

import ch.ludovic_mermod.dfasimulator.gui.Controls;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.gui.scene.Link;
import ch.ludovic_mermod.dfasimulator.gui.scene.MainPane;
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
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setScene(new Scene(new MainPane(), 800, 600));
        primaryStage.show();
    }
}
