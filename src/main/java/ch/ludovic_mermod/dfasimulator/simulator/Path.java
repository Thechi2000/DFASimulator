package ch.ludovic_mermod.dfasimulator.simulator;

import java.util.Set;

public class Path
{
    private Set<Character> alphabet;
    private State from, to;

    public Path(State from, State to, Set<Character> alphabet)
    {
        this.alphabet = alphabet;
        this.from = from;
        this.to = to;
    }

    public Set<Character> getAlphabet()
    {
        return alphabet;
    }
    public void setAlphabet(Set<Character> alphabet)
    {
        this.alphabet = alphabet;
    }

    public State getFrom()
    {
        return from;
    }
    public State getTo()
    {
        return to;
    }
}
