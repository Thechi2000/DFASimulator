package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.constants.settings.Settings;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ResourceBundle;

/**
 * Pane to log user destined information about application execution
 */
public class ConsolePane extends ScrollPane implements System.Logger
{
    private TextFlow textFlow;

    public void create(MainPane mainPane)
    {
        setContent(textFlow = new TextFlow());

        textFlow.minWidthProperty().bind(widthProperty());
        textFlow.minHeightProperty().bind(heightProperty());
        textFlow.getStyleClass().add("background");
    }

    @Override
    public String getName()
    {
        return "displayed_console";
    }
    @Override
    public boolean isLoggable(Level level)
    {
        return true;
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown)
    {
        textFlow.getChildren().add(new Text(String.format("[%s] %s\n", level.name(), msg)));
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String format, Object... params)
    {
        Text text = new Text(String.format("[%s] %s\n", level.name(), String.format(format, params)));
        text.getStyleClass().add(level.name().toLowerCase());
        textFlow.getChildren().add(text);
    }

    public void clear()
    {
        textFlow.getChildren().clear();
    }
}
