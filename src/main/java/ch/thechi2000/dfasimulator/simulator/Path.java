package ch.thechi2000.dfasimulator.simulator;

import java.util.List;

public class Path
{
    public Path(State from, State to, List<String> alphabet)
    {
        this.alphabet = alphabet;
        this.from = from;
        this.to = to;
    }

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

    private final List<String> alphabet;
    private final State from;
    private final State to;
}
