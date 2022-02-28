package ch.thechi2000.dfasimulator.simulator;

import java.util.List;

public class Path
{
    public List<String> getAlphabet()
    {
        return alphabet;
    }
    public State getFrom()
    {
        return from;
    }
    public State getTo()
    {
        return to;
    }

    private List<String> alphabet;
    private State from, to;
}
