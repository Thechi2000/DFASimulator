package ch.ludovic_mermod.dfasimulator.logic;

import java.util.*;
import java.util.stream.Collectors;

public class DFA
{
    public final State initialState;
    public final Set<State> states;
    public final Set<State> acceptingStates;
    public final Set<Character> alphabet;
    public final Map<State, Map<Character, State>> transitionMap;

    public DFA(String initialState, Set<String> states, Set<String> acceptingStates, Set<Character> alphabet, Map<String, Map<Character, String>> transitionMap)
    {
        this.initialState = new State(initialState);
        this.states = Set.copyOf(states.stream().map(State::new).collect(Collectors.toSet()));
        this.acceptingStates = Set.copyOf(acceptingStates.stream().map(State::new).collect(Collectors.toSet()));
        this.alphabet = Set.copyOf(alphabet);
        this.transitionMap = copyTransitionMap(transitionMap);
    }

    private Map<State, Map<Character, State>> copyTransitionMap(Map<String, Map<Character, String>> transitionMap)
    {
        return Map.copyOf(transitionMap
                .entrySet()
                .stream()
                .map(e -> Map.entry(
                        e.getKey(),
                        e.getValue()
                                .entrySet().stream()
                                .map(entry -> Map.entry(entry.getKey(), new State(entry.getValue())))
                                .collect(TreeMap<Character, State>::new, (m, entry) -> m.put(entry.getKey(), entry.getValue()), TreeMap::putAll)))
                .collect(() -> new TreeMap<>(Comparator.comparing((State s) -> s.name)), (m, e) -> m.put(new State(e.getKey()), e.getValue()), TreeMap::putAll));
    }

    public boolean isAccepted(String input)
    {
        if (!input.chars().allMatch(i -> alphabet.contains((char) i))) return false;

        State state = initialState;

        for (var c : input.toCharArray())
            state = transitionMap.get(state).get(c);

        return acceptingStates.contains(state);
    }

    public State state(String name)
    {
        return new State(name);
    }

    public class State
    {
        private final String name;

        public State(String name)
        {
            this.name = name;
        }

        public String name()
        {
            return name;
        }
        public boolean isInitial()
        {
            return name.equals(initialState.name);
        }
        public boolean isAccepting()
        {
            return acceptingStates.stream().anyMatch(s -> s.name.equals(name));
        }
        public DFA dfa()
        {
            return DFA.this;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name);
        }
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return Objects.equals(name, state.name);
        }
    }
}
