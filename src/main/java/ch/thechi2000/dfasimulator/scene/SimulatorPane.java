package ch.thechi2000.dfasimulator.scene;

import ch.thechi2000.dfasimulator.simulator.State;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

public class SimulatorPane extends Region
{
    private final List<StateNode> nodes;
    private final List<Link> links;

    private final ContextMenu menu;
    private Tool tool;

    public SimulatorPane()
    {
        nodes = new ArrayList<>();
        links = new ArrayList<>();

        menu = createContextMenu();
        tool = Tool.DRAG;

        setOnMousePressed(event -> menu.hide());
        setOnContextMenuRequested(event -> menu.show(this, event.getScreenX(), event.getScreenY()));
    }

    private ContextMenu createContextMenu()
    {
        ContextMenu menu = new ContextMenu();

        MenuItem create = new MenuItem("Create");
        create.setOnAction(event -> createNode(0, 0));
        menu.getItems().add(create);

        menu.setAutoHide(true);

        return menu;
    }

    public Tool getTool()
    {
        return tool;
    }

    private boolean hasNode(String name)
    {
        return nodes.stream().anyMatch(n -> n.getState().getName().equals(name));
    }
    private StateNode getNode(String name)
    {
        return nodes.stream().filter(n -> n.getState().getName().equals(name)).findAny().orElse(null);
    }

    public void addState(StateNode stateNode)
    {
        getChildren().add(stateNode);
        nodes.add(stateNode);
    }
    public void addLink(Link link)
    {
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
    protected void createNode(double x, double y)
    {
        StateNode node = new StateNode(new State("new"));
        node.relocate(x, y);
        addState(node);
    }
    protected void deleteNode(String name)
    {
        if (!hasNode(name))
        {
            System.out.println("Could not remove node " + name + ", node does not exist");
            return;
        }

        getChildren().remove(getNode(name));
        nodes.remove(getNode(name));
        links.stream().filter(l -> l.getStartName().equals(name) || l.getTargetName().equals(name)).forEach(l -> getChildren().remove(l));
        links.removeIf(l -> l.getStartName().equals(name) || l.getTargetName().equals(name));
    }

    public enum Tool
    {
        EDIT,
        DRAG,
        LINK
    }
}
