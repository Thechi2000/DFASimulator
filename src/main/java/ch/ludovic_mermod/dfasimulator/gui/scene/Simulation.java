package ch.ludovic_mermod.dfasimulator.gui.scene;

import javafx.beans.property.*;

import java.util.*;
import java.util.stream.Collectors;

public class Simulation
{
    private final ObjectProperty<Node> currentStateProperty;
    private final ObjectProperty<Edge> lastUsedLinkProperty;
    private final StringProperty remainingInputProperty;
    private final BooleanProperty isSimulatingProperty;

    private final StringProperty initialInputProperty;
    private final BooleanProperty resultProperty;
    private final BooleanProperty simulationEndedProperty;

    private final GraphPane graphPane;

    public Simulation(GraphPane graphPane)
    {
        this.graphPane = graphPane;
        currentStateProperty = new SimpleObjectProperty<>();
        lastUsedLinkProperty = new SimpleObjectProperty<>();
        remainingInputProperty = new SimpleStringProperty();
        isSimulatingProperty = new SimpleBooleanProperty(false);

        initialInputProperty = new SimpleStringProperty("");
        resultProperty = new SimpleBooleanProperty(false);
        simulationEndedProperty = new SimpleBooleanProperty(false);
    }

    public boolean isValidDFA()
    {
        return checkDFA().isEmpty();
    }

    public List<Error> checkDFA()
    {
        List<Error> errors = new ArrayList<>();
        Set<Character> alphabet = getAlphabet();

        for (Node node : graphPane.getNodes())
        {
            List<Character> elements = node.outgoingLinksProperty().stream().map(l -> l.alphabetProperty().get()).collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);

            if (elements.size() != alphabet.size())
            {
                if (elements.size() < alphabet.size())
                {
                    var missingElements = new TreeSet<>(alphabet);
                    elements.forEach(missingElements::remove);
                    errors.add(new Error(ErrorCode.NODE_DOES_NOT_MATCH_ALPHABET, new Object[]{node, false, missingElements}));
                }
                else
                    errors.add(new Error(ErrorCode.NODE_DOES_NOT_MATCH_ALPHABET, new Object[]{node, true}));
            }
        }


        var initialNodes = graphPane.getNodes().stream().filter(n -> n.initialProperty().get()).toList();
        if (initialNodes.size() == 0)
            errors.add(new Error(ErrorCode.NO_INITIAL_STATE, null));
        else if (initialNodes.size() > 1)
            errors.add(new Error(ErrorCode.TOO_MANY_INITIAL_STATES, initialNodes.toArray()));

        return errors;
    }

    public List<Error> checkDFA(String input)
    {
        Set<Character> alphabet = getAlphabet();
        List<Error> errors = checkDFA();

        if (alphabet.stream().anyMatch(c -> !alphabet.contains(c)))
            errors.add(new Error(ErrorCode.STRING_DOES_NOT_MATCH_ALPHABET, new Object[]{alphabet.stream().filter(c -> !alphabet.contains(c)).collect(Collectors.toSet())}));

        return errors;
    }

    public boolean compileDFA()
    {
        var errors = checkDFA();
        graphPane.getMainPane().getConsolePane().clear();
        errors.forEach(e ->
        {
            switch (e.code())
            {
                case TOO_MANY_INITIAL_STATES -> graphPane.getMainPane().getConsolePane().log(System.Logger.Level.ERROR, "Error: Too many initial states { %s }", Arrays.stream(e.data()).map(o -> ((Node) o).getName()).collect(Collectors.joining(", ")));

                case NO_INITIAL_STATE -> graphPane.getMainPane().getConsolePane().log(System.Logger.Level.ERROR, "No initial state");

                case NODE_DOES_NOT_MATCH_ALPHABET -> {
                    if (((boolean) e.data()[1]))
                        graphPane.getMainPane().getConsolePane().log(System.Logger.Level.ERROR, "Error: Node \"%s\" has too many outputs", ((Node) e.data()[0]).getName());
                    else
                        graphPane.getMainPane().getConsolePane().log(System.Logger.Level.ERROR, "Error: Node \"%s\" is missing outputs { %s }",
                                ((Node) e.data()[0]).getName(),
                                ((Set<Character>) e.data()[2]).stream()
                                        .map(Object::toString)
                                        .collect(Collectors.joining(", ")));
                }

                default -> throw new IllegalStateException("Unexpected value: " + e.code());
            }
        });

        if (errors.size() == 0)
            graphPane.getMainPane().getConsolePane().log(System.Logger.Level.INFO, "DFA is valid");

        return errors.size() == 0;
    }

    public void startSimulation(String input)
    {
        if (!compileDFA()) return;

        isSimulatingProperty.set(true);
        simulationEndedProperty.set(false);
        currentStateProperty.set(getInitialState());
        remainingInputProperty.set(input);
        lastUsedLinkProperty.set(null);
        initialInputProperty.set(input);
    }

    public void nextSimulationStep()
    {
        if (remainingInputProperty.get().length() == 0)
        {
            currentStateProperty.set(null);
            lastUsedLinkProperty.set(null);
            isSimulatingProperty.set(false);
            return;
        }

        String input = remainingInputProperty.get();
        char c = input.charAt(0);
        remainingInputProperty.set(input.substring(1));

        for (var l : currentStateProperty.get().outgoingLinksProperty().get())
            if (l.alphabetProperty().contains(c))
            {
                currentStateProperty.set(l.getTarget().get());
                break;
            }

        if (remainingInputProperty.get().length() == 0)
            simulationEndedProperty.set(true);
    }

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

    private Node getInitialState()
    {
        return graphPane.getNodes().stream().filter(n -> n.initialProperty().get()).findAny().orElse(null);
    }
    public Set<Character> getAlphabet()
    {
        return graphPane.getEdges().stream().map(l -> l.alphabetProperty().get()).collect(TreeSet::new, TreeSet::addAll, TreeSet::addAll);
    }

    public ReadOnlyObjectProperty<Node> currentStateProperty()
    {
        return currentStateProperty;
    }
    public ReadOnlyObjectProperty<Edge> lastUsedLinkProperty()
    {
        return lastUsedLinkProperty;
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

    public enum ErrorCode
    {
        TOO_MANY_INITIAL_STATES,
        NO_INITIAL_STATE,
        NODE_DOES_NOT_MATCH_ALPHABET,
        STRING_DOES_NOT_MATCH_ALPHABET
    }

    public record Error(ErrorCode code, Object[] data)
    {
    }
}
