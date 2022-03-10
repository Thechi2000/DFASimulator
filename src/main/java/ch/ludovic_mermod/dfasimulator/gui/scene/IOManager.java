package ch.ludovic_mermod.dfasimulator.gui.scene;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class IOManager
{
    private final GraphPane graphPane;
    private final StringProperty filenameProperty;

    public IOManager(GraphPane graphPane)
    {
        this.graphPane = graphPane;
        filenameProperty = new SimpleStringProperty();
    }

    public void save()
    {
        if (filenameProperty.isEmpty().get() || filenameProperty.get().isEmpty())
        {
            String str = graphPane.getMainPane().getSimulatorMenuBar().chooseSaveFile();
            if (str == null) return;
            filenameProperty.set(str);
        }

        File file = new File(filenameProperty.get());
        try
        {
            if (!file.exists()) file.createNewFile();
            try (FileOutputStream o = new FileOutputStream(file))
            {
                o.write(graphPane.toJSONObject().toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void saveAs(String filename)
    {
        filenameProperty.set(filename);
        save();
    }
    public void open(String filename)
    {
        filenameProperty.set(filename);

        try
        {
            JsonObject object = JsonParser.parseReader(new FileReader(filenameProperty.get())).getAsJsonObject();
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

        filenameProperty.set(null);
    }
}
