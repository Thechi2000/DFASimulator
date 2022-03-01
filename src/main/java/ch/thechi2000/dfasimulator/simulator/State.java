package ch.thechi2000.dfasimulator.simulator;

import java.util.ArrayList;
import java.util.List;

public class State
{
    private final String name;
    private final List<Path> paths = new ArrayList<>();
    public State(String name)
    {
        this.name = name;
    }
    public String getName()
    {
        return name;
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
