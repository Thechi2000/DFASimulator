package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.Main;
import ch.ludovic_mermod.dfasimulator.gui.scene.Edge;
import ch.ludovic_mermod.dfasimulator.gui.scene.GraphPane;
import ch.ludovic_mermod.dfasimulator.gui.scene.Node;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class IOManager
{
    private final GraphPane graphPane;
    private final StringProperty filenameProperty, filepathProperty;

    public IOManager(GraphPane graphPane)
    {
        this.graphPane = graphPane;
        filenameProperty = new SimpleStringProperty();
        filepathProperty = new SimpleStringProperty();

        filepathProperty.addListener((o, ov, nv) -> filenameProperty.set(new File(filepathProperty.get()).getName()));
    }

    public void save()
    {
        if (filepathProperty.isEmpty().get() || filepathProperty.get().isEmpty())
        {
            String str = graphPane.getMainPane().getSimulatorMenuBar().chooseSaveFile();
            if (str == null) return;
            filepathProperty.set(str);
        }

        File file = new File(filepathProperty.get());
        try
        {
            if (!file.exists() && file.createNewFile())
                try (FileOutputStream o = new FileOutputStream(file))
                {
                    o.write(graphPane.toJSONObject().toString().getBytes(StandardCharsets.UTF_8));
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
            JsonObject object = JsonParser.parseReader(new FileReader(filepathProperty.get())).getAsJsonObject();
            var nodesArray = object.get("nodes").getAsJsonArray();
            var edgesArray = object.get("edges").getAsJsonArray();

            graphPane.getNodes().clear();
            graphPane.getEdges().clear();
            graphPane.children().clear();

            nodesArray.forEach(e -> graphPane.addState(Node.fromJSONObject(e.getAsJsonObject(), graphPane)));
            edgesArray.forEach(e -> graphPane.addLink(Edge.fromJSONObject(e.getAsJsonObject(), graphPane)));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    public void openNew()
    {
        graphPane.getNodes().clear();
        graphPane.getEdges().clear();
        graphPane.children().clear();

        filepathProperty.set(null);
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
