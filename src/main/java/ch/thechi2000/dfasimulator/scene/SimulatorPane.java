package ch.thechi2000.dfasimulator.scene;

import ch.thechi2000.dfasimulator.scene.lang.Strings;
import ch.thechi2000.dfasimulator.simulator.State;
import javafx.geometry.Point2D;
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
    private Point2D menuPosition;

    private Tool tool;

    public SimulatorPane()
    {
        nodes = new ArrayList<>();
        links = new ArrayList<>();

        menu = createContextMenu();
        tool = Tool.DRAG;

        setOnMousePressed(event -> menu.hide());
        setOnContextMenuRequested(event ->
        {
            menuPosition = new Point2D(event.getX(), event.getY());
            menu.show(this, event.getScreenX(), event.getScreenY());
        });
    }

    private ContextMenu createContextMenu()
    {
        ContextMenu menu = new ContextMenu();

        MenuItem create = new MenuItem();
        Strings.bind("create", create.textProperty());
        create.setOnAction(event -> createNode(menuPosition.getX(), menuPosition.getY()));
        menu.getItems().add(create);

        menu.setAutoHide(true);

        return menu;
    }

    public Tool getTool()
    {
        return tool;
    }
    public void setTool(Tool tool)
    {
        this.tool = tool;
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

        StateNode node = new StateNode(new State("new" + (i == 0 ? "" : Integer.toString(i))));
        node.relocate(x, y);
        addState(node);
    }

    protected void deleteLink(Link link)
    {
        links.remove(link);
        getChildren().remove(link);
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
        links.stream().filter(l -> l.getSourceName().equals(name) || l.getTargetName().equals(name)).forEach(l -> getChildren().remove(l));
        links.removeIf(l -> l.getSourceName().equals(name) || l.getTargetName().equals(name));
    }

    public enum Tool
    {
        EDIT,
        DRAG,
        LINK
    }
}
