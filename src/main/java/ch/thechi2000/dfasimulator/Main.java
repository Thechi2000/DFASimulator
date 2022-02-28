package ch.thechi2000.dfasimulator;

import ch.thechi2000.dfasimulator.scene.Link;
import ch.thechi2000.dfasimulator.scene.StateNode;
import ch.thechi2000.dfasimulator.simulator.State;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application
{

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        var stateNode1 = new StateNode(new State("source"));
        stateNode1.relocate(50, 50);

        var stateNode2 = new StateNode(new State("target"));
        stateNode2.relocate(200, 50);

        var link = new Link(stateNode1, stateNode2);

        primaryStage.setScene(new Scene(new Pane(stateNode1, stateNode2, link), 800, 600));
        primaryStage.show();
    }
}
