package ch.ludovic_mermod.dfasimulator.gui.scene;

public class EditPane
{
    private final LinkEditPane linkEditPane;
    private final NodeEditPane nodeEditPane;
    public EditPane(SimulationPane simulationPane)
    {
        nodeEditPane = new NodeEditPane();
        linkEditPane = new LinkEditPane(simulationPane);
    }

}
