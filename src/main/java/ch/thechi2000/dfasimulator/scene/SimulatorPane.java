package ch.thechi2000.dfasimulator.scene;

import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SimulatorPane extends Region
{
    private final Map<String, StateNode> nodes = new TreeMap<>();
    private final List<Link> links = new ArrayList<>();

    public void addState(StateNode stateNode)
    {
        getChildren().add(stateNode);
        nodes.put(stateNode.getState().getName(), stateNode);
    }

    public void addLink(Link link)
    {
        getChildren().add(link);
        links.add(link);
    }

    protected void createLink(String from, String to)
    {
        System.out.println(from + " and " + to + " were linked");
        Link lnk = new Link(nodes.get(from), nodes.get(to));
        addLink(lnk);
    }
}
