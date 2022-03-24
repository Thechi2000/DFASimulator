package ch.ludovic_mermod.dfasimulator;

import ch.ludovic_mermod.dfasimulator.gui.MainPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main extends Application
{
    public static final Logger logger = Logger.getLogger(Main.class.getPackageName());
    public static void log(Level level, String msg, Object... params)
    {
        logger.log(level, msg, params);
    }


    public static void main(String[] args)
    {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> logger.log(Level.SEVERE, "Thread " + t.getName(), e));
        SimpleDateFormat format = new SimpleDateFormat("d_M_HHmmss");
        FileHandler handler = null;

        try
        {
            File dir = new File("logs");
            if (!dir.exists() && !dir.mkdir())
                return;

            File file = new File("logs/" + format.format(Calendar.getInstance().getTime()) + ".log");
            if (file.exists() || file.createNewFile())
                handler = new FileHandler(file.getAbsolutePath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        MainPane mainPane = new MainPane();

        primaryStage.setScene(new Scene(mainPane, 800, 600));
        primaryStage.show();

        mainPane.create(primaryStage);
    }
}
