package ch.thechi2000.dfasimulator;

import ch.thechi2000.dfasimulator.scene.Link;
import ch.thechi2000.dfasimulator.scene.SimulatorPane;
import ch.thechi2000.dfasimulator.scene.StateNode;
import ch.thechi2000.dfasimulator.simulator.Path;
import ch.thechi2000.dfasimulator.simulator.State;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

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
        State state1 = new State("source");
        var stateNode1 = new StateNode(state1);
        stateNode1.relocate(50, 50);

        State state2 = new State("target");
        var stateNode2 = new StateNode(state2);
        stateNode2.relocate(300, 50);

        var link = new Link(stateNode1, stateNode2, new Path(state1, state2, List.of("0", "1")));

        SimulatorPane simulatorPane = new SimulatorPane();
        simulatorPane.addState(stateNode1);
        simulatorPane.addState(stateNode2);
        simulatorPane.addLink(link);

        primaryStage.setScene(new Scene(simulatorPane, 800, 600));
        primaryStage.show();
    }
}
