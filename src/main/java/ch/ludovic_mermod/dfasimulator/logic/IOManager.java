package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.gui.scene.MainPane;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.beans.property.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Timer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IOManager
{
    protected static final Timer TIMER = new Timer();
    private final MainPane mainPane;
    private final StringProperty filenameProperty, filepathProperty;
    private final BooleanProperty isSavedProperty;
    private final FiniteAutomaton finiteAutomaton;
    private JSONElement savedFile;

    public IOManager(MainPane mainPane)
    {
        this.mainPane = mainPane;
        filenameProperty = new SimpleStringProperty();
        filepathProperty = new SimpleStringProperty();
        isSavedProperty = new SimpleBooleanProperty();

        filepathProperty.addListener((o, ov, nv) -> filenameProperty.set(new File(filepathProperty.get()).getName()));

        mainPane.getFiniteAutomaton().getJSONObject().getAsJSONObject().addListener((JSONElement.ChildUpdateListener) update -> updateSavedProperty());
        finiteAutomaton = mainPane.getFiniteAutomaton();
    }

    public void save()
    {
        if (filepathProperty.isEmpty().get() || filepathProperty.get().isEmpty())
        {
            String str = mainPane.getGraphPane().getMainPane().getSimulatorMenuBar().chooseSaveFile();
            if (str == null) return;
            filepathProperty.set(str);
        }

        File file = new File(filepathProperty.get());
        try
        {
            if (file.exists() || file.createNewFile())
                try (FileOutputStream o = new FileOutputStream(file))
                {
                    final JSONElement copy = finiteAutomaton.getJSONObject().deepCopy();
                    savedFile = copy;
                    o.write((copy).toString().getBytes(StandardCharsets.UTF_8));
                }
            else
                Main.log(Level.SEVERE, "Could not create file " + file.getAbsolutePath());
        }
        catch (IOException e)
        {
            Main.log(Level.SEVERE, "Could not save DFA", e);
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
            finiteAutomaton.clear();

            nodesArray.forEach(e -> finiteAutomaton.addState(State.fromJSONObject(e.getAsJSONObject(), finiteAutomaton)));
            nodesArray.forEach(e -> finiteAutomaton.getState(e.getAsJSONObject().get("name").getAsString()).loadTransitionMap(e.getAsJSONObject().get("transitionMap").getAsJSONObject()));

            savedFile = finiteAutomaton.getJSONObject().deepCopy();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        updateSavedProperty();
    }
    public void openNew()
    {
        finiteAutomaton.clear();
        filepathProperty.set(null);
        savedFile = finiteAutomaton.getJSONObject().deepCopy();

        updateSavedProperty();
    }

    private void updateSavedProperty()
    {
        isSavedProperty.set(isSaved());
    }

    public boolean isSaved()
    {
        return savedFile == null || savedFile.equals(finiteAutomaton.getJSONObject());
    }
    public ReadOnlyBooleanProperty isSavedProperty()
    {
        return isSavedProperty;
    }

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
