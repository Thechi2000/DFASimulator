package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.constants.Resources;
import ch.ludovic_mermod.dfasimulator.gui.MainPane;
import ch.ludovic_mermod.dfasimulator.json.*;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Iterator;
import java.util.logging.Level;

public class PaneManager
{
    public static final  String JSON_LAYOUT      = "layout";
    private static final String JSON_MAIN_LAYOUT = "main";
    private static final String JSON_OTHERS      = "others";

    private final ListProperty<Pair<Sentinel, Stage>> stages;

    private Sentinel mainLayout;

    public static final PaneManager INSTANCE = new PaneManager();

    private PaneManager()
    {
        stages = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    public void load(Stage primaryStage, MainPane mainPane)
    {
        stages.clear();

       /* mainLayout = new SimpleObjectProperty<>(new Fork(
                new Fork(
                        new Leaf("graph"),
                        new Leaf("console"),
                        Orientation.VERTICAL
                ),
                new Leaf("simulation"),
                Orientation.HORIZONTAL
        ));
        stages.add(new Pair<>(mainLayout, primaryStage));
        primaryStage.setScene(new Scene(mainPane, 800, 600));
        primaryStage.show();*/

        try
        {
            final JSONElement element = JSONElement.readFromFile(Resources.get("session.json"));
            mainLayout = element.isJSONObject() && element.getAsJSONObject().hasObject(JSON_LAYOUT) && element.getAsJSONObject().getAsJSONObject(JSON_LAYOUT).hasObject(JSON_MAIN_LAYOUT)
                         ? Element.load(element.getAsJSONObject().getAsJSONObject(JSON_LAYOUT).getAsJSONObject(JSON_MAIN_LAYOUT)) : null;

            if (mainLayout == null) mainLayout = new Sentinel(new Leaf(new Item[0]));
            stages.add(new Pair<>(mainLayout, primaryStage));
            primaryStage.setScene(new Scene(mainPane, 800, 600));
            primaryStage.show();

            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();

            object.add(JSON_MAIN_LAYOUT, mainLayout.getJSONObject());

            object.add(JSON_OTHERS, array);

            if (element.isJSONObject() && element.getAsJSONObject().hasObject(JSON_LAYOUT) && element.getAsJSONObject().getAsJSONObject(JSON_LAYOUT).hasArray(JSON_OTHERS))
                for (JSONElement elem : element.getAsJSONObject().getAsJSONObject(JSON_LAYOUT).getAsJSONArray(JSON_OTHERS))
                {
                    Sentinel e = Element.load(elem.getAsJSONObject());
                    if (e == null) continue;

                    Stage stage = new Stage();
                    stage.setScene(new Scene(e.getContent(), 800, 600));
                    stage.getScene().rootProperty().bind(e.getContentBinding());

                    stages.add(new Pair<>(e, stage));
                }
        }
        catch (IOManager.CorruptedFileException e)
        {
            Main.logger.log(Level.WARNING, "Could not parse session file", e);
        }
    }

    public void save()
    {
        JSONObject layoutObject = new JSONObject();
        layoutObject.add(JSON_MAIN_LAYOUT, mainLayout == null ? JSONNull.INSTANCE : mainLayout.getJSONObject());

        JSONArray array = new JSONArray();
        stages.forEach(p -> array.add(p.getKey() == null ? JSONNull.INSTANCE : p.getKey().getJSONObject()));
        layoutObject.add(JSON_OTHERS, array);

        JSONObject object = new JSONObject();
        object.add(JSON_LAYOUT, layoutObject);
        object.saveToFile(Resources.get("new_session.json"));
    }

    public Sentinel getMainLayout()
    {
        return mainLayout;
    }

    public void moveToWindow(Item i)
    {
        remove(i);

        Stage stage = new Stage();
        Sentinel sentinel = new Sentinel(new Leaf(i));

        stage.setScene(new Scene(sentinel.getContent()));
        stage.getScene().rootProperty().bind(sentinel.getContentBinding());
        stage.show();

        stages.add(new Pair<>(sentinel, stage));
    }

    protected void remove(Item i)
    {
        // Must use indexed looping, or a ConcurrentModificationException will be thrown
        for (int j = 0; j < stages.size(); j++)
        {
            Pair<Sentinel, Stage> p = stages.get(j);
            p.getKey().remove(i);

            if (p.getKey().isEmpty())
                p.getValue().close();
        }

        stages.removeIf(p -> p.getKey().isEmpty());
    }
}
