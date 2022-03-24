package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.gui.MainPane;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import com.google.gson.JsonParseException;
import javafx.beans.property.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IOManager
{
    private final MainPane        mainPane;
    private final FiniteAutomaton finiteAutomaton;

    private final StringProperty filenameProperty, filepathProperty;
    private final BooleanProperty isSavedProperty;

    private final JSONObject  currentFile;
    private       JSONElement savedFile;

    public IOManager(MainPane mainPane)
    {
        this.mainPane = mainPane;
        filenameProperty = new SimpleStringProperty();
        filepathProperty = new SimpleStringProperty();
        isSavedProperty = new SimpleBooleanProperty();

        filepathProperty.addListener((o, ov, nv) -> filenameProperty.set(nv == null ? "new" : new File(filepathProperty.get()).getName()));
        finiteAutomaton = mainPane.getFiniteAutomaton();

        currentFile = new JSONObject();
        currentFile.add("graph", mainPane.getGraphPane().getJSONObject());
        currentFile.add("automaton", mainPane.getFiniteAutomaton().getJSONObject());
        currentFile.addListener((JSONElement.ChildUpdateListener) update -> updateSavedProperty());
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
                    savedFile = currentFile.deepCopy();
                    o.write((savedFile).toString().getBytes(StandardCharsets.UTF_8));
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

            object.checkHasObject("graph");
            object.checkHasObject("automaton");

            finiteAutomaton.loadJSON(object.getAsJSONObject("automaton"));
            mainPane.getGraphPane().loadJSON(object.getAsJSONObject("graph"));

            savedFile = finiteAutomaton.getJSONObject().deepCopy();
        }
        catch (FileNotFoundException | CorruptedFileException | JsonParseException e)
        {
            Main.logger.log(Level.SEVERE, "While reading " + filename, e);
            finiteAutomaton.clear();
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
        return savedFile == null || savedFile.toString().equals(currentFile.toString());
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

    public static class CorruptedFileException extends Exception
    {
        public CorruptedFileException(String format, Object... objects)
        {
            super(String.format(format, objects));
        }
    }
}
