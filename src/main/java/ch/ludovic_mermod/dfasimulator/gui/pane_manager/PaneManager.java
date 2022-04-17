package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.constants.Resources;
import ch.ludovic_mermod.dfasimulator.gui.MainPane;
import ch.ludovic_mermod.dfasimulator.json.*;
import ch.ludovic_mermod.dfasimulator.logic.IOManager;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.logging.Level;

public class PaneManager
{
    public static final String JSON_LAYOUT      = "layout";
    public static final String JSON_MAIN_LAYOUT = "main";
    public static final String JSON_IS_MAIN     = "is_main";
    public static final String JSON_WINDOWS     = "windows";

    private final ListProperty<Pair<Sentinel, Stage>> stages;

    private Sentinel mainLayout;

    public static final PaneManager INSTANCE = new PaneManager();

    private PaneManager()
    {
        stages = new SimpleListProperty<>(FXCollections.observableArrayList());
        stages.addListener((ListChangeListener<? super Pair<Sentinel, Stage>>) change -> {
            change.next();
            change.getAddedSubList().stream().filter(p -> !p.getKey().isMain()).forEach(p -> p.getValue().setOnCloseRequest(e -> close(p.getValue())));
        });
    }

    private void close(Stage value)
    {
        stages.removeIf(p -> p.getValue() == value);
    }

    public void load(Stage primaryStage, MainPane mainPane)
    {
        stages.clear();

        try
        {
            final JSONElement element = JSONElement.readFromFile(Resources.get("session.json"));

            if (element.isJSONObject() && element.getAsJSONObject().hasObject(JSON_LAYOUT) && element.getAsJSONObject().getAsJSONObject(JSON_LAYOUT).hasArray(JSON_WINDOWS))
                for (JSONElement elem : element.getAsJSONObject().getAsJSONObject(JSON_LAYOUT).getAsJSONArray(JSON_WINDOWS))
                {
                    JSONObject obj = elem.getAsJSONObject();
                    obj.checkHasBoolean(JSON_IS_MAIN);
                    boolean isMain = obj.get(JSON_IS_MAIN).getAsBoolean();

                    Sentinel e = Element.load(obj);
                    if (isMain) mainLayout = e;

                    Stage stage = isMain ? primaryStage : new Stage();
                    stage.setScene(new Scene(isMain ? mainPane : e.getContent(), 800, 600));
                    stage.show();

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

        JSONArray array = new JSONArray();
        stages.forEach(p -> array.add(p.getKey() == null ? JSONNull.INSTANCE : p.getKey().getJSONObject()));
        layoutObject.add(JSON_WINDOWS, array);

        JSONObject object = new JSONObject();
        object.add(JSON_LAYOUT, layoutObject);
        object.saveToFile(Resources.get("session.json"));
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
