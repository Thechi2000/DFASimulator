package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.scene.GraphPane;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.*;
import java.util.stream.Collectors;

public class Simulation
{
    private final IOManager ioManager;

    private final SetProperty<State> states;
    private final SetProperty<Link> links;

    private final ObjectProperty<State> currentStateProperty;
    private final ObjectProperty<Link> lastUsedLinkProperty;
    private final StringProperty remainingInputProperty;
    private final BooleanProperty isSimulatingProperty;

    private final StringProperty initialInputProperty;
    private final BooleanProperty resultProperty;
    private final BooleanProperty simulationEndedProperty;

    private final GraphPane graphPane;

    public Simulation()
    {
        ioManager = new IOManager(this);

        states = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>(Comparator.comparing(State::getName))));
        links = new SimpleSetProperty<>(FXCollections.observableSet(new HashSet<>()));

        this.graphPane = new GraphPane(this);
        currentStateProperty = new SimpleObjectProperty<>();
        lastUsedLinkProperty = new SimpleObjectProperty<>();
        remainingInputProperty = new SimpleStringProperty();
        isSimulatingProperty = new SimpleBooleanProperty(false);

        initialInputProperty = new SimpleStringProperty("");
        resultProperty = new SimpleBooleanProperty(false);
        simulationEndedProperty = new SimpleBooleanProperty(false);
    }

    public ReadOnlyObjectProperty<State> currentStateProperty()
    {
        return currentStateProperty;
    }
    public ReadOnlyObjectProperty<Link> lastUsedLinkProperty()
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


    /**
     * Create and add a link between two StateNodes
     *
     * @param from the name of the source state
     * @param to   the name of the target state
     */
    public void createLink(String from, String to)
    {
        if (!hasState(from) || !hasState(to))
        {
            System.out.println("Could not link " + from + " and " + to);
            System.out.println("{" + hasState(from) + ", " + hasState(to) + "}");
            return;
        }

        Link link = new Link(getState(from), getState(to), Set.of(), this);
        addLink(link);
    }
    /**
     * Create and add a node at the given coordinates
     *
     * @param x coordinate
     * @param y coordinate
     */
    public void createNode(double x, double y)
    {
        int i = 0;
        if (hasState("new"))
            do
            {
                ++i;
            }
            while (hasState("new" + i));

        State state = new State("new" + (i == 0 ? "" : Integer.toString(i)), this);
        state.getNode().relocate(x, y);
        addState(state);
    }

    public void addState(State state)
    {
        graphPane.children().add(state.getNode());
        states.add(state);
    }
    public void addLink(Link link)
    {
        link.source().get().addLink(link);
        graphPane.children().add(link.getEdge());
        links.add(link);
    }

    public void deleteLink(Link link)
    {
        link.source().get().removeLink(link);
        links.remove(link);
        graphPane.children().remove(link.getEdge());
    }
    public void deleteState(State state)
    {
        graphPane.children().remove(state.getNode());
        states.remove(state);

        String name = state.getName();
        links.stream().filter(l -> l.source().get().getName().equals(name) || l.target().get().getName().equals(name)).forEach(l ->
        {
            graphPane.children().remove(l.getEdge());
            links.remove(l);
        });
    }
    public void clear()
    {
        states.clear();
        links.clear();
        graphPane.children().clear();
    }


    public JsonElement toJSONObject()
    {
        JsonObject object = new JsonObject();
        JsonArray nodesArray = new JsonArray(),
                linksArray = new JsonArray();

        states.stream().map(State::toJSONObject).forEach(nodesArray::add);
        links.stream().map(Link::toJSONObject).forEach(linksArray::add);

        object.add("nodes", nodesArray);
        object.add("edges", linksArray);
        return object;
    }

    private boolean hasState(String name)
    {
        return states.stream().anyMatch(s -> s.getName().equals(name));
    }
    State getState(String name)
    {
        return states.stream().filter(s -> s.getName().equals(name)).findAny().orElseThrow();
    }

    public boolean isValidDFA()
    {
        return checkDFA().isEmpty();
    }

    public List<Error> checkDFA()
    {
        List<Error> errors = new ArrayList<>();
        Set<Character> alphabet = getAlphabet();

        for (State state : states)
        {
            List<Character> elements = state.outgoingLinksProperty().stream().map(l -> l.alphabetProperty().get()).collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);

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


        var initialStates = states.stream().filter(n -> n.initialProperty().get()).toList();
        if (initialStates.size() == 0)
            errors.add(new Error(ErrorCode.NO_INITIAL_STATE, null));
        else if (initialStates.size() > 1)
            errors.add(new Error(ErrorCode.TOO_MANY_INITIAL_STATES, initialStates.toArray()));

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

    @SuppressWarnings("unchecked")
    public boolean compileDFA()
    {
        var errors = checkDFA();
        graphPane.getMainPane().getConsolePane().clear();
        errors.forEach(e ->
        {
            switch (e.code())
            {
                case TOO_MANY_INITIAL_STATES -> graphPane.getMainPane().getConsolePane().log(System.Logger.Level.ERROR, "Error: Too many initial states { %s }", Arrays.stream(e.data()).map(o -> ((State) o).getName()).collect(Collectors.joining(", ")));

                case NO_INITIAL_STATE -> graphPane.getMainPane().getConsolePane().log(System.Logger.Level.ERROR, "No initial state");

                case NODE_DOES_NOT_MATCH_ALPHABET -> {
                    if (((boolean) e.data()[1]))
                        graphPane.getMainPane().getConsolePane().log(System.Logger.Level.ERROR, "Error: State \"%s\" has too many outputs", ((State) e.data()[0]).getName());
                    else
                        graphPane.getMainPane().getConsolePane().log(System.Logger.Level.ERROR, "Error: State \"%s\" is missing outputs { %s }",
                                ((State) e.data()[0]).getName(),
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
                currentStateProperty.set(l.target().get());
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

    public State getInitialState()
    {
        return states.stream().filter(state -> state.initialProperty().get()).findAny().orElseThrow();
    }
    public Set<Character> getAlphabet()
    {
        return graphPane.getEdges().stream().map(l -> l.alphabetProperty().get()).collect(TreeSet::new, TreeSet::addAll, TreeSet::addAll);
    }

    public GraphPane getGraphPane()
    {
        return graphPane;
    }

    public ObservableSet<Link> getLinks()
    {
        return links;
    }
    public ObservableSet<State> getStates()
    {
        return states;
    }
    public IOManager ioManager()
    {
        return ioManager;
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
