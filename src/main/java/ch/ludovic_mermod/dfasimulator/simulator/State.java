package ch.ludovic_mermod.dfasimulator.simulator;

import java.util.ArrayList;
import java.util.List;

public class State
{
    private final List<Path> paths = new ArrayList<>();
    private String name;

    public State(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public int pathsCount()
    {
        return paths.size();
    }
    public Path getPath(int index)
    {
        return paths.get(index);
    }
}
