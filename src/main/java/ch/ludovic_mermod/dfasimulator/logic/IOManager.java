package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.beans.property.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class IOManager
{
    protected static final Timer TIMER = new Timer();
    private final Simulation simulation;
    private final StringProperty filenameProperty, filepathProperty;
    private final BooleanProperty isSavedProperty;
    private JSONElement savedFile;

    public IOManager(Simulation simulation)
    {
        this.simulation = simulation;
        filenameProperty = new SimpleStringProperty();
        filepathProperty = new SimpleStringProperty();
        isSavedProperty = new SimpleBooleanProperty();

        filepathProperty.addListener((o, ov, nv) -> filenameProperty.set(new File(filepathProperty.get()).getName()));

        simulation.getJSONObject().getAsJSONObject().addListener((JSONElement.ChildUpdateListener) update -> updateSavedProperty());
        /*TIMER.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                updateSavedProperty();
            }
        }, 0, 1000);*/
    }

    public void save()
    {
        if (filepathProperty.isEmpty().get() || filepathProperty.get().isEmpty())
        {
            String str = simulation.getGraphPane().getMainPane().getSimulatorMenuBar().chooseSaveFile();
            if (str == null) return;
            filepathProperty.set(str);
        }

        File file = new File(filepathProperty.get());
        try
        {
            if (file.exists() || file.createNewFile())
                try (FileOutputStream o = new FileOutputStream(file))
                {
                    final JSONElement copy = simulation.getJSONObject().deepCopy();
                    savedFile = copy;
                    o.write((copy).toString().getBytes(StandardCharsets.UTF_8));
                }
            else
                Main.logger.log(System.Logger.Level.ERROR, "Could not create file " + file.getAbsolutePath());
        }
        catch (IOException e)
        {
            Main.logger.log(System.Logger.Level.ERROR, "Could not save DFA", e);
        }

        updateSavedProperty();
    }
    public void saveAs(String filename)
    {
        filepathProperty.set(filename);
        save();
    }
    public void open(String filename)
    {
        filepathProperty.set(filename);

        try
        {
            JSONObject object = JSONElement.parse(new BufferedReader(new FileReader(filepathProperty.get())).lines().collect(Collectors.joining("\n"))).getAsJSONObject();
            var nodesArray = object.get("states").getAsJSONArray();
            var edgesArray = object.get("links").getAsJSONArray();

            simulation.clear();

            nodesArray.forEach(e -> simulation.addState(State.fromJSONObject(e.getAsJSONObject(), simulation)));
            edgesArray.forEach(e -> simulation.addLink(Link.fromJSONObject(e.getAsJSONObject(), simulation)));

            savedFile = simulation.getJSONObject().deepCopy();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        updateSavedProperty();
    }
    public void openNew()
    {
        simulation.clear();
        filepathProperty.set(null);
        savedFile = simulation.getJSONObject().deepCopy();

        updateSavedProperty();
    }

    private void updateSavedProperty()
    {
        isSavedProperty.set(isSaved());
    }

    public boolean isSaved()
    {
        return savedFile == null || savedFile.equals(simulation.getJSONObject());
    }
    public ReadOnlyBooleanProperty isSavedProperty(){return isSavedProperty;}

    public boolean close()
    {
        if (isSaved()) return true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, Strings.format("alert.save_on_exit", filenameProperty.get()), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

        final Optional<ButtonType> answer = alert.showAndWait();
        if (answer.isEmpty() || answer.get() == ButtonType.CANCEL)
            return false;
        else if (answer.get() == ButtonType.YES)
            save();
        return true;
    }

    public ReadOnlyStringProperty filepathProperty()
    {
        return filepathProperty;
    }
    public ReadOnlyStringProperty filenameProperty()
    {
        return filenameProperty;
    }
}
