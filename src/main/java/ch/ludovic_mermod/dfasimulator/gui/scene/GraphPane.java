package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static ch.ludovic_mermod.dfasimulator.gui.scene.GraphPane.ErrorCode.TOO_MANY_INITIAL_STATES;

public class GraphPane extends Region
{
    private final ObjectProperty<StateNode> currentStateProperty;
    private final ObjectProperty<Link> lastUsedLinkProperty;
    private final StringProperty remainingInputProperty;
    private final BooleanProperty isSimulatingProperty;

    private final StringProperty initialInputProperty;
    private final BooleanProperty resultProperty;
    private final BooleanProperty simulationEndedProperty;

    private final ListProperty<StateNode> nodes;
    private final ListProperty<Link> links;

    private ContextMenu menu;
    private MainPane mainPane;

    private Point2D menuPosition;
    private Tool tool;

    public GraphPane()
    {
        currentStateProperty = new SimpleObjectProperty<>();
        lastUsedLinkProperty = new SimpleObjectProperty<>();
        remainingInputProperty = new SimpleStringProperty();
        isSimulatingProperty = new SimpleBooleanProperty(false);

        initialInputProperty = new SimpleStringProperty("");
        resultProperty = new SimpleBooleanProperty(false);
        simulationEndedProperty = new SimpleBooleanProperty(false);

        nodes = new SimpleListProperty<>(FXCollections.observableArrayList());
        links = new SimpleListProperty<>(FXCollections.observableArrayList());

        tool = Tool.EDIT;
    }


    public void create(MainPane mainPane)
    {
        this.mainPane = mainPane;
        menu = createContextMenu();

        setOnMousePressed(event -> menu.hide());
        setOnContextMenuRequested(event ->
        {
            menuPosition = new Point2D(event.getX(), event.getY());
            menu.show(this, event.getScreenX(), event.getScreenY());
        });
    }

    protected ReadOnlyListProperty<StateNode> getNodes()
    {
        return nodes;
    }
    protected ReadOnlyListProperty<Link> getLinks()
    {
        return links;
    }

    public ReadOnlyObjectProperty<StateNode> currentStateProperty()
    {
        return currentStateProperty;
    }
    public ReadOnlyObjectProperty<Link> lastUsedLinkProperty()
    {
        return lastUsedLinkProperty;
    }
    public StringProperty remainingInputProperty()
    {
        return remainingInputProperty;
    }
    public BooleanProperty isSimulatingProperty()
    {
        return isSimulatingProperty;
    }

    public StringProperty initialInputProperty()
    {
        return initialInputProperty;
    }
    public BooleanProperty resultProperty()
    {
        return resultProperty;
    }
    public BooleanProperty simulationEndedProperty()
    {
        return simulationEndedProperty;
    }

    public Tool getTool()
    {
        return tool;
    }
    public void setTool(Tool tool)
    {
        this.tool = tool;
    }

    public ReadOnlyBooleanProperty getSimulationProperty()
    {
        return isSimulatingProperty;
    }

    protected boolean hasNode(String name)
    {
        return nodes.stream().anyMatch(n -> n.getName().equals(name));
    }
    protected StateNode getNode(String name)
    {
        return nodes.stream().filter(n -> n.getName().equals(name)).findAny().orElse(null);
    }

    public void addState(StateNode stateNode)
    {
        getChildren().add(stateNode);
        nodes.add(stateNode);
    }
    public void addLink(Link link)
    {
        link.getSource().get().addLink(link);
        getChildren().add(link);
        links.add(link);
    }

    private JsonElement toJSONObject()
    {
        JsonObject object = new JsonObject();
        JsonArray nodesArray = new JsonArray(),
                linksArray = new JsonArray();

        nodes.stream().map(StateNode::toJSONObject).forEach(nodesArray::add);
        links.stream().map(Link::toJSONObject).forEach(linksArray::add);

        object.add("nodes", nodesArray);
        object.add("links", linksArray);
        return object;
    }

    void saveToFile(String filename)
    {
        File file = new File(filename);
        try
        {
            if (!file.exists()) file.createNewFile();
            try (FileOutputStream o = new FileOutputStream(file))
            {
                o.write(toJSONObject().toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    void loadFromFile(String filename)
    {
        try
        {
            JsonObject object = JsonParser.parseReader(new FileReader(filename)).getAsJsonObject();
            var nodes = object.get("nodes").getAsJsonArray();
            var links = object.get("links").getAsJsonArray();

            nodes.forEach(e -> addState(StateNode.fromJSONObject(e.getAsJsonObject(), this)));
            links.forEach(e -> addLink(Link.fromJSONObject(e.getAsJsonObject(), this)));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Create and add a link between two StateNodes
     *
     * @param from the name of the source state
     * @param to   the name of the target state
     */
    protected void createLink(String from, String to)
    {
        if (!hasNode(from) || !hasNode(to))
        {
            System.out.println("Could not link " + from + " and " + to);
            System.out.println("{" + hasNode(from) + ", " + hasNode(to) + "}");
            return;
        }

        Link lnk = new Link(getNode(from), getNode(to));
        addLink(lnk);
    }
    /**
     * Create and add a node at the given coordinates
     *
     * @param x coordinate
     * @param y coordinate
     */
    protected void createNode(double x, double y)
    {
        int i = 0;
        if (hasNode("new"))
            do
            {
                ++i;
            }
            while (hasNode("new" + i));

        StateNode node = new StateNode("new" + (i == 0 ? "" : Integer.toString(i)), this);
        node.relocate(x, y);
        addState(node);
    }

    protected void deleteLink(Link link)
    {
        link.getSource().get().removeLink(link);
        links.remove(link);
        getChildren().remove(link);
    }
    protected void deleteNode(StateNode node)
    {
        getChildren().remove(node);
        nodes.remove(node);

        String name = node.getName();
        links.stream().filter(l -> l.getSourceName().equals(name) || l.getTargetName().equals(name)).forEach(l -> getChildren().remove(l));
        links.removeIf(l -> l.getSourceName().equals(name) || l.getTargetName().equals(name));
    }

    protected void bindEditPane(Link link)
    {
        ((MainPane) getParent()).bindEditPane(link);
    }
    protected void bindEditPane(StateNode stateNode)
    {
        ((MainPane) getParent()).bindEditPane(stateNode);
    }
    protected void removeEditPane()
    {
        ((MainPane) getParent()).removeEditPane();
    }

    private ContextMenu createContextMenu()
    {
        ContextMenu menu = new ContextMenu();

        MenuItem create = new MenuItem();
        Strings.bind("create", create.textProperty());
        create.setOnAction(event -> createNode(menuPosition.getX(), menuPosition.getY()));
        create.disableProperty().bind(isSimulatingProperty);
        menu.getItems().add(create);

        return menu;
    }

    public boolean isValidDFA()
    {
        return checkDFA().isEmpty();
    }

    public List<Error> checkDFA()
    {
        List<Error> errors = new ArrayList<>();
        Set<Character> alphabet = getAlphabet();

        for (StateNode node : nodes)
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


        var initialNodes = nodes.stream().filter(n -> n.initialProperty().get()).toList();
        if (initialNodes.size() == 0)
            errors.add(new Error(ErrorCode.NO_INITIAL_STATE, null));
        else if (initialNodes.size() > 1)
            errors.add(new Error(TOO_MANY_INITIAL_STATES, initialNodes.toArray()));

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
        mainPane.getConsolePane().clear();
        errors.forEach(e ->
        {
            switch (e.code())
            {
                case TOO_MANY_INITIAL_STATES -> mainPane.getConsolePane().log(System.Logger.Level.ERROR, "Error: Too many initial states { %s }", Arrays.stream(e.data()).map(o -> ((StateNode) o).getName()).collect(Collectors.joining(", ")));

                case NO_INITIAL_STATE -> mainPane.getConsolePane().log(System.Logger.Level.ERROR, "No initial state");

                case NODE_DOES_NOT_MATCH_ALPHABET -> {
                    if (((boolean) e.data()[1]))
                        mainPane.getConsolePane().log(System.Logger.Level.ERROR, "Error: Node \"%s\" has too many outputs", ((StateNode) e.data()[0]).getName());
                    else
                        mainPane.getConsolePane().log(System.Logger.Level.ERROR, "Error: Node \"%s\" is missing outputs { %s }",
                                ((StateNode) e.data()[0]).getName(),
                                ((Set<Character>) e.data()[2]).stream()
                                        .map(Object::toString)
                                        .collect(Collectors.joining(", ")));
                }

                default -> throw new IllegalStateException("Unexpected value: " + e.code());
            }
        });

        if (errors.size() == 0)
            mainPane.getConsolePane().log(System.Logger.Level.INFO, "DFA is valid");

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

    private StateNode getInitialState()
    {
        return nodes.stream().filter(n -> n.initialProperty().get()).findAny().orElse(null);
    }
    public Set<Character> getAlphabet()
    {
        return links.stream().map(l -> l.alphabetProperty().get()).collect(TreeSet::new, TreeSet::addAll, TreeSet::addAll);
    }

    public enum ErrorCode
    {
        TOO_MANY_INITIAL_STATES,
        NO_INITIAL_STATE,
        NODE_DOES_NOT_MATCH_ALPHABET,
        STRING_DOES_NOT_MATCH_ALPHABET
    }

    public enum Tool
    {
        EDIT,
        DRAG,
        LINK
    }

    public record Error(ErrorCode code, Object[] data)
    {
    }
}
