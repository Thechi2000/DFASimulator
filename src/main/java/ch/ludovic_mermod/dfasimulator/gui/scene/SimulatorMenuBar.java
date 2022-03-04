package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Controls;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class SimulatorMenuBar extends MenuBar
{
    private MainPane mainPane;

    public void create(MainPane mainPane)
    {
        this.mainPane = mainPane;
        var graphPane = mainPane.getGraphPane();

        Menu toolsMenu = new Menu();
        Strings.bind("tools", toolsMenu.textProperty());
        getMenus().add(toolsMenu);

        MenuItem edit = new MenuItem();
        Strings.bind("edit", edit.textProperty());
        edit.setOnAction(event -> graphPane.setTool(GraphPane.Tool.EDIT));
        edit.acceleratorProperty().bind(Controls.editTool);
        toolsMenu.getItems().add(edit);

        MenuItem drag = new MenuItem();
        Strings.bind("drag", drag.textProperty());
        drag.setOnAction(event -> graphPane.setTool(GraphPane.Tool.DRAG));
        drag.acceleratorProperty().bind(Controls.dragTool);
        toolsMenu.getItems().add(drag);

        MenuItem link = new MenuItem();
        Strings.bind("link", link.textProperty());
        link.setOnAction(event -> graphPane.setTool(GraphPane.Tool.LINK));
        link.acceleratorProperty().bind(Controls.linkTool);
        toolsMenu.getItems().add(link);

        MenuItem test = new MenuItem("test");
        test.setOnAction(event -> compileDFA());
        toolsMenu.getItems().add(test);
    }

    private void print(System.Logger.Level lvl, String str)
    {
        mainPane.getConsolePane().log(lvl, str);
    }

    private void print(System.Logger.Level lvl, String str, Object... objects)
    {
        mainPane.getConsolePane().log(lvl, str, objects);
    }

    public void compileDFA()
    {
        var errors = mainPane.getGraphPane().checkDFA();
        errors.forEach(e ->
        {
            switch (e.code())
            {
                case TOO_MANY_INITIAL_STATES -> print(System.Logger.Level.ERROR, "Error: Too many initial states { %s }", Arrays.stream(e.data()).map(o -> ((StateNode) o).getName()).collect(Collectors.joining(", ")));

                case NO_INITIAL_STATE -> print(System.Logger.Level.ERROR, "No initial state");

                case NODE_DOES_NOT_MATCH_ALPHABET -> {
                    if (((boolean) e.data()[1]))
                        print(System.Logger.Level.ERROR, "Error: Node \"%s\" has too many outputs", ((StateNode) e.data()[0]).getName());
                    else print(System.Logger.Level.ERROR, "Error: Node \"%s\" is missing outputs { %s }",
                            ((StateNode) e.data()[0]).getName(),
                            ((Set<Character>) e.data()[2]).stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(", ")));
                }

                default -> throw new IllegalStateException("Unexpected value: " + e.code());
            }
        });
    }
}
