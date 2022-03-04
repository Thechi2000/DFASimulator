package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class GraphPane extends Region
{
    private final ObjectProperty<StateNode> currentStateProperty;
    private final ObjectProperty<Link> lastUsedLinkProperty;
    private final StringProperty remainingInputProperty;
    private final BooleanProperty isSimulating;

    private final ListProperty<StateNode> nodes;
    private final ListProperty<Link> links;

    private ContextMenu menu;

    private Point2D menuPosition;
    private Tool tool;

    public GraphPane()
    {
        currentStateProperty = new SimpleObjectProperty<>();
        lastUsedLinkProperty = new SimpleObjectProperty<>();
        remainingInputProperty = new SimpleStringProperty();

        isSimulating = new SimpleBooleanProperty(false);

        nodes = new SimpleListProperty<>(FXCollections.observableArrayList());
        links = new SimpleListProperty<>(FXCollections.observableArrayList());

        tool = Tool.EDIT;
    }


    public void create(MainPane mainPane)
    {
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
        return isSimulating;
    }

    private boolean hasNode(String name)
    {
        return nodes.stream().anyMatch(n -> n.getName().equals(name));
    }
    private StateNode getNode(String name)
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

        StateNode node = new StateNode("new" + (i == 0 ? "" : Integer.toString(i)));
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
        create.disableProperty().bind(isSimulating);
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
                } else
                    errors.add(new Error(ErrorCode.NODE_DOES_NOT_MATCH_ALPHABET, new Object[]{node, true}));
            }
        }


        var initialNodes = nodes.stream().filter(n -> n.initialProperty().get()).toList();
        if (initialNodes.size() == 0)
            errors.add(new Error(ErrorCode.NO_INITIAL_STATE, null));
        else if (initialNodes.size() > 1)
            errors.add(new Error(ErrorCode.TOO_MANY_INITIAL_STATES, initialNodes.toArray()));

        return errors;
    }

    public List<Error> startSimulation(String input)
    {
        Set<Character> alphabet = getAlphabet();
        List<Error> errors = checkDFA();

        if (alphabet.stream().anyMatch(c -> !alphabet.contains(c)))
            errors.add(new Error(ErrorCode.STRING_DOES_NOT_MATCH_ALPHABET, new Object[]{alphabet.stream().filter(c -> !alphabet.contains(c)).collect(Collectors.toSet())}));

        if (!errors.isEmpty())
            return errors;

        remainingInputProperty.set(input);
        currentStateProperty.set(getInitialState());
        lastUsedLinkProperty.set(null);
        isSimulating.set(true);

        return List.of();
    }

    private StateNode getInitialState()
    {
        return null;
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
