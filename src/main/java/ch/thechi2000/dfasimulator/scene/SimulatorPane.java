package ch.thechi2000.dfasimulator.scene;

import javafx.scene.layout.Region;

public class SimulatorPane extends Region
{
    public void addState(StateNode stateNode)
    {
        getChildren().add(stateNode);
    }

    public void addLink(Link link)
    {
        getChildren().add(link);
    }
}
