package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Controls;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.gui.scene.components.TransitionTable;
import ch.ludovic_mermod.dfasimulator.logic.DFA;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MenuBar extends javafx.scene.control.MenuBar
{
    private final FileChooser fileChooser;
    private MainPane mainPane;

    public MenuBar()
    {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DFA", "*.dfa"));
    }

    public void create(MainPane mainPane)
    {
        this.mainPane = mainPane;
        var graphPane = mainPane.getGraphPane();

        // File
        {
            Menu fileMenu = new Menu();
            Strings.bind("menu.file", fileMenu.textProperty());
            getMenus().add(fileMenu);

            MenuItem newItem = new MenuItem();
            Strings.bind("menu.file.new", newItem.textProperty());
            newItem.setOnAction(event -> mainPane.getSimulation().ioManager().openNew());
            newItem.acceleratorProperty().bind(Controls.newFile);
            fileMenu.getItems().add(newItem);

            MenuItem open = new MenuItem();
            Strings.bind("menu.file.open", open.textProperty());
            open.setOnAction(event -> open());
            open.acceleratorProperty().bind(Controls.open);
            fileMenu.getItems().add(open);

            SeparatorMenuItem separator = new SeparatorMenuItem();
            fileMenu.getItems().add(separator);

            MenuItem save = new MenuItem();
            Strings.bind("menu.file.save", save.textProperty());
            save.setOnAction(event -> mainPane.getSimulation().ioManager().save());
            save.acceleratorProperty().bind(Controls.save);
            fileMenu.getItems().add(save);

            MenuItem saveAs = new MenuItem();
            Strings.bind("menu.file.save_as", saveAs.textProperty());
            saveAs.setOnAction(event -> saveAs());
            saveAs.acceleratorProperty().bind(Controls.saveAs);
            fileMenu.getItems().add(saveAs);
        }

        // Tools
        {
            Menu toolsMenu = new Menu();
            Strings.bind("menu.tools", toolsMenu.textProperty());
            getMenus().add(toolsMenu);

            MenuItem edit = new MenuItem();
            Strings.bind("menu.tools.edit", edit.textProperty());
            edit.setOnAction(event -> graphPane.setTool(GraphPane.Tool.EDIT));
            edit.acceleratorProperty().bind(Controls.editTool);
            toolsMenu.getItems().add(edit);

            MenuItem drag = new MenuItem();
            Strings.bind("menu.tools.drag", drag.textProperty());
            drag.setOnAction(event -> graphPane.setTool(GraphPane.Tool.DRAG));
            drag.acceleratorProperty().bind(Controls.dragTool);
            toolsMenu.getItems().add(drag);

            MenuItem link = new MenuItem();
            Strings.bind("menu.tools.link", link.textProperty());
            link.setOnAction(event -> graphPane.setTool(GraphPane.Tool.LINK));
            link.acceleratorProperty().bind(Controls.linkTool);
            toolsMenu.getItems().add(link);
        }

        // DFA
        {
            Menu dfaMenu = new Menu();
            Strings.bind("menu.dfa", dfaMenu.textProperty());
            getMenus().add(dfaMenu);

            MenuItem transitionTableItem = new MenuItem();
            Strings.bind("menu.dfa.transition_table", transitionTableItem.textProperty());
            transitionTableItem.setOnAction(event ->
            {
                DFA dfa = mainPane.getSimulation().generateDFA();
                if (dfa == null) return;

                TransitionTable table = new TransitionTable();
                table.loadDFA(dfa);

                Stage stage = new Stage();
                stage.setScene(new Scene(table));

                stage.show();
            });
            dfaMenu.getItems().add(transitionTableItem);
        }
    }

    private void open()
    {
        fileChooser.setTitle(Strings.get("menu.file.open").get());
        File f = fileChooser.showOpenDialog(getScene().getWindow());
        saveLastFilePath(f);
        if (f != null) mainPane.getSimulation().ioManager().open(f.getAbsolutePath());
    }

    private void saveAs()
    {
        String path = chooseSaveFile();
        if (path != null) mainPane.getSimulation().ioManager().saveAs(path);
    }

    public String chooseSaveFile()
    {
        fileChooser.setTitle(Strings.get("menu.file.save_as").get());
        File f = fileChooser.showSaveDialog(getScene().getWindow());
        saveLastFilePath(f);
        return f == null ? null : f.getAbsolutePath();
    }

    private void saveLastFilePath(File file)
    {
        if (file == null) return;

        fileChooser.setInitialDirectory(file.getParentFile());
        fileChooser.setInitialFileName(file.getName());
    }
}
