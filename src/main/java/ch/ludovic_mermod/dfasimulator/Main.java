package ch.ludovic_mermod.dfasimulator;

import ch.ludovic_mermod.dfasimulator.gui.scene.MainPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
