package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.json.JSONElement;
import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class IOManager
{
    private final Simulation simulation;
    private final StringProperty filenameProperty, filepathProperty;
    private JSONElement savedFile;

    public IOManager(Simulation simulation)
    {
        this.simulation = simulation;
        filenameProperty = new SimpleStringProperty();
        filepathProperty = new SimpleStringProperty();

        filepathProperty.addListener((o, ov, nv) -> filenameProperty.set(new File(filepathProperty.get()).getName()));
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
            if (!file.exists() && file.createNewFile())
                try (FileOutputStream o = new FileOutputStream(file))
                {
                    o.write((savedFile = simulation.getJSONObject()).toString().getBytes(StandardCharsets.UTF_8));
                }
            else
                Main.logger.log(System.Logger.Level.ERROR, "Could not create file " + file.getAbsolutePath());
        }
        catch (IOException e)
        {
            Main.logger.log(System.Logger.Level.ERROR, "Could not save DFA", e);
        }
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

            savedFile = object;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    public void openNew()
    {
        savedFile = new JSONObject();
        simulation.clear();
        filepathProperty.set(null);
    }

    public boolean isSaved()
    {
        return savedFile.equals(simulation.getJSONObject());
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
