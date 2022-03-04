package ch.ludovic_mermod.dfasimulator.gui.scene;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ConsolePane extends VBox
{
    private TextFlow textFlow;

    void print(String str)
    {
        textFlow.getChildren().add(new Text(str));
    }

    public void create(MainPane mainPane)
    {
        textFlow = new TextFlow();
        getChildren().add(textFlow);
    }
}
