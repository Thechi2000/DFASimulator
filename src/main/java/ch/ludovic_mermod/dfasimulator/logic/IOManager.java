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

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Manages the open/save actions for the current automaton
 */
public class IOManager
{
    private final MainPane        mainPane;
    private final FiniteAutomaton finiteAutomaton;

    private final StringProperty filenameProperty, filepathProperty;
    private final BooleanProperty isSavedProperty;

    private final JSONObject  currentFile;
    private       JSONElement savedFile;

    /**
     * Constructs an IOManager for the given MainPane
     */
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

    /**
     * Save the automaton to the current file
     */
    public void save()
    {
        Strings.save();
        if (filepathProperty.isEmpty().get() || filepathProperty.get().isEmpty())
        {
            String str = mainPane.getGraphPane().getMainPane().getSimulatorMenuBar().chooseSaveFile();
            if (str == null) return;
            filepathProperty.set(str);
        }

        currentFile.saveToFile(filepathProperty.get());
        savedFile = currentFile.deepCopy();

        updateSavedProperty();
    }
    /**
     * Changes the current file and save the automaton to it
     * @param filename the path of the file
     */
    public void saveAs(String filename)
    {
        filepathProperty.set(filename);
        save();
    }
    /**
     * Open the given file
     * @param filename the file to oepn
     */
    public void open(String filename)
    {
        filepathProperty.set(filename);
        if(!close()) return;

        try
        {
            var readJSON = JSONElement.readFromFile(filename);
            JSONObject object = readJSON.isJSONObject() ? readJSON.getAsJSONObject() : new JSONObject();

            object.checkHasObject("graph");
            object.checkHasObject("automaton");

            finiteAutomaton.loadJSON(object.getAsJSONObject("automaton"));
            mainPane.getGraphPane().loadJSON(object.getAsJSONObject("graph"));

            savedFile = currentFile.deepCopy();
        }
        catch (CorruptedFileException | JsonParseException e)
        {
            Main.logger.log(Level.SEVERE, "While reading " + filename, e);
            finiteAutomaton.clear();
        }

        updateSavedProperty();
    }
    /**
     * Open a new file
     */
    public void openNew()
    {
        finiteAutomaton.clear();
        filepathProperty.set(null);
        savedFile = currentFile.deepCopy();

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

    /**
     * Close the current file
     * If the file is not saved, the user is prompted for saving the file
     * @return whether the closure was successful (i.e. whether the file is saved)
     */
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
