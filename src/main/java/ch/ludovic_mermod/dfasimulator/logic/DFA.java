package ch.ludovic_mermod.dfasimulator.logic;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public record DFA(String initialState, Set<String> states,
                  Set<String> acceptingStates, Set<Character> alphabet,
                  Map<String, Map<Character, String>> transitionMap)
{
    public DFA(String initialState, Set<String> states, Set<String> acceptingStates, Set<Character> alphabet, Map<String, Map<Character, String>> transitionMap)
    {
        this.initialState = initialState;
        this.states = Set.copyOf(states);
        this.acceptingStates = Set.copyOf(acceptingStates);
        this.alphabet = Set.copyOf(alphabet);
        this.transitionMap = copyTransitionMap(transitionMap);
    }

    private static Map<String, Map<Character, String>> copyTransitionMap(Map<String, Map<Character, String>> transitionMap)
    {
        return Map.copyOf(transitionMap.entrySet().stream().map(e -> Map.entry(e.getKey(), Map.copyOf(e.getValue()))).collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), TreeMap::putAll));
    }

    public boolean isAccepted(String input)
    {
        if (!input.chars().allMatch(i -> alphabet.contains((char) i))) return false;

        String state = initialState;

        for (var c : input.toCharArray())
            state = transitionMap.get(state).get(c);

        return acceptingStates.contains(state);
    }
}
