package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.MainPane;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hold the information for simulating an automaton with an input
 */
public class Simulation
{
    private final FiniteAutomaton finiteAutomaton;
    private final MainPane        mainPane;

    private final SetProperty<Pair<State, State>> currentLinksProperty;
    private final SetProperty<State>              currentStatesProperty;

    private final StringProperty  remainingInputProperty;
    private final BooleanProperty isSimulatingProperty;

    private final StringProperty  initialInputProperty;
    private final BooleanProperty resultProperty;
    private final BooleanProperty simulationEndedProperty;

    /**
     * Construct a Simulation for a given MainPane
     */
    public Simulation(MainPane mainPane)
    {
        finiteAutomaton = mainPane.getFiniteAutomaton();
        this.mainPane = mainPane;

        currentStatesProperty = new SimpleSetProperty<>(FXCollections.observableSet(new HashSet<>()));
        currentLinksProperty = new SimpleSetProperty<>(FXCollections.observableSet(new HashSet<>()));
        remainingInputProperty = new SimpleStringProperty();
        isSimulatingProperty = new SimpleBooleanProperty(false);

        initialInputProperty = new SimpleStringProperty("");
        resultProperty = new SimpleBooleanProperty(false);
        simulationEndedProperty = new SimpleBooleanProperty(false);

        simulationEndedProperty.addListener((o, ov, nv) -> updateResult());
        currentStatesProperty.addListener((o, ov, nv) -> updateResult());
    }

    private void updateResult()
    {
        if (currentStatesProperty.get() != null)
            resultProperty.set(currentStatesProperty.get().stream().anyMatch(State::isAccepting) && simulationEndedProperty.get());
    }

    public ReadOnlySetProperty<State> currentStateProperty()
    {
        return currentStatesProperty;
    }
    public ReadOnlySetProperty<Pair<State, State>> lastStateProperty()
    {
        return currentLinksProperty;
    }
    public ReadOnlyStringProperty remainingInputProperty()
    {
        return remainingInputProperty;
    }
    public ReadOnlyBooleanProperty isSimulatingProperty()
    {
        return isSimulatingProperty;
    }

    public ReadOnlyStringProperty initialInputProperty()
    {
        return initialInputProperty;
    }
    public ReadOnlyBooleanProperty resultProperty()
    {
        return resultProperty;
    }
    public ReadOnlyBooleanProperty simulationEndedProperty()
    {
        return simulationEndedProperty;
    }

    /**
     * Check whether the automaton is a valid DFA
     *
     * @return a list containing all design errors (if empty, the DFA is valid)
     */
    public List<Error> checkDFA()
    {
        List<Error> errors = new ArrayList<>();
        Set<Character> alphabet = finiteAutomaton.alphabet();

        for (State state : finiteAutomaton.states())
        {
            List<Character> elements = state.transitionMap().entrySet().stream().filter(e -> e.getValue().get() != null).map(Map.Entry::getKey).toList();

            if (elements.size() != alphabet.size())
            {
                if (elements.size() < alphabet.size())
                {
                    var missingElements = new TreeSet<>(alphabet);
                    elements.forEach(missingElements::remove);
                    errors.add(new Error(ErrorCode.NODE_DOES_NOT_MATCH_ALPHABET, new Object[]{state, false, missingElements}));
                }
                else
                    errors.add(new Error(ErrorCode.NODE_DOES_NOT_MATCH_ALPHABET, new Object[]{state, true}));
            }
        }

        errors.addAll(checkInitialState());
        return errors;
    }

    /**
     * Check whether the automaton is a valid DFA
     *
     * @return a list containing all design errors (if empty, the DFA is valid)
     */
    public List<Error> checkNFA()
    {
        return checkInitialState();
    }

    public List<Error> checkInitialState()
    {
        return finiteAutomaton.initialStateProperty().get() == null ? List.of(new Error(ErrorCode.NO_INITIAL_STATE, null)) : List.of();
    }

    /**
     * Check whether the automaton is a valid DFA and the input matches the alphabet
     *
     * @param input the input for the simulation
     * @return a list containing all design errors (if empty, the DFA is valid)
     */
    public List<Error> checkDFA(String input)
    {
        Set<Character> alphabet = finiteAutomaton.alphabet();
        List<Error> errors = checkDFA();

        if (input.chars().anyMatch(c -> !alphabet.contains((char) c)))
            errors.add(new Error(ErrorCode.STRING_DOES_NOT_MATCH_ALPHABET, new Object[]{alphabet.stream().filter(c -> !alphabet.contains(c)).collect(Collectors.toSet())}));

        return errors;
    }

    /**
     * Lists all design errors and prints them to the ConsolePane
     *
     * @return whether the DFA is valid
     */
    public boolean compileDFA()
    {
        var errors = checkDFA();
        printErrors(errors);
        return errors.isEmpty();
    }

    public boolean compileNFA()
    {
        var errors = checkNFA();
        printErrors(errors);
        return errors.isEmpty();
    }

    private void printErrors(Collection<Error> errors)
    {
        mainPane.getConsolePane().clear();
        errors.forEach(e ->
        {
            switch (e.code())
            {
                case TOO_MANY_INITIAL_STATES -> mainPane.getConsolePane().log(System.Logger.Level.ERROR, "Error: Too many initial states { %s }", Arrays.stream(e.data()).map(o -> ((State) o).name()).collect(Collectors.joining(", ")));

                case NO_INITIAL_STATE -> mainPane.getConsolePane().log(System.Logger.Level.ERROR, "No initial state");

                case NODE_DOES_NOT_MATCH_ALPHABET -> {
                    if (((boolean) e.data()[1]))
                        mainPane.getConsolePane().log(System.Logger.Level.ERROR, "Error: State \"%s\" has too many outputs", ((State) e.data()[0]).name());
                    else
                        mainPane.getConsolePane().log(System.Logger.Level.ERROR, "Error: State \"%s\" is missing outputs { %s }",
                                ((State) e.data()[0]).name(),
                                ((Set<Character>) e.data()[2]).stream()
                                        .map(Object::toString)
                                        .collect(Collectors.joining(", ")));
                }

                default -> throw new IllegalStateException("Unexpected value: " + e.code());
            }
        });

        if (errors.size() == 0)
            mainPane.getConsolePane().log(System.Logger.Level.INFO, "Automaton is valid");
    }

    /**
     * Start a simulation
     *
     * @param input the input of the simulation
     */
    public void startSimulation(String input)
    {
        if (!compileNFA()) return;
        currentLinksProperty.clear();
        currentStatesProperty.clear();

        isSimulatingProperty.set(true);
        simulationEndedProperty.set(false);
        currentLinksProperty.set(null);
        currentStatesProperty.add(finiteAutomaton.initialState());
        remainingInputProperty.set(input);
        initialInputProperty.set(input);
    }

    /**
     * Analyse next character in the input String
     */
    public void nextSimulationStep()
    {
        if (remainingInputProperty.get().length() == 0)
        {
            currentLinksProperty.set(FXCollections.observableSet(new HashSet<>()));
            currentStatesProperty.set(FXCollections.observableSet(new HashSet<>()));
            isSimulatingProperty.set(false);
            return;
        }

        String input = remainingInputProperty.get();
        char c = input.charAt(0);
        remainingInputProperty.set(input.substring(1));

        ObservableSet<State> newStates = FXCollections.observableSet(new HashSet<>());
        ObservableSet<Pair<State, State>> newLinks = FXCollections.observableSet(new HashSet<>());
        currentStatesProperty.forEach(source -> {
            if (source.transitionMap().getValue(c) != null)
                source.transitionMap().getValue(c).forEach(target -> {
                    newStates.add(target);
                    newLinks.add(new Pair<>(source, target));
                });
        });
        currentStatesProperty.set(newStates);
        currentLinksProperty.set(newLinks);

        if (remainingInputProperty.get().length() == 0)
            simulationEndedProperty.set(true);
    }

    /**
     * Analyse all remaining characters one by one
     */
    public void finish()
    {
        if (!isSimulatingProperty.get()) return;

        nextSimulationStep();
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                finish();
            }
        }, 1000);
    }

    /**
     * Test whether an input is accepted
     *
     * @param input the input to test
     * @return whether the input is accepted
     */
    public boolean test(String input)
    {
        if (!compileDFA()) return false;

        State currentState = finiteAutomaton.initialState();
        for (char c : input.toCharArray()) currentState = currentState.transitionMap().getValue(c).get(0);

        return currentState.isAccepting();
    }

    public enum ErrorCode
    {
        TOO_MANY_INITIAL_STATES,
        NO_INITIAL_STATE,
        NODE_DOES_NOT_MATCH_ALPHABET,
        STRING_DOES_NOT_MATCH_ALPHABET
    }

    /**
     * Store an error
     *
     * @param code the ErrorCode
     * @param data data to explicit the error, depends on the ErrorCode
     */
    public record Error(ErrorCode code, Object[] data)
    {
    }
}
