package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.utils.Utils;
import ch.ludovic_mermod.dfasimulator.constants.Controls;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.gui.components.TransitionTable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.FileChooser;

import java.io.File;

public class MenuBar extends javafx.scene.control.MenuBar
{
    private final FileChooser fileChooser;
    private       MainPane    mainPane;

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
            newItem.setOnAction(event -> mainPane.getIoManager().openNew());
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
            save.setOnAction(event -> mainPane.getIoManager().save());
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
            transitionTableItem.setOnAction(event -> Utils.openNewStage(new TransitionTable(mainPane.getFiniteAutomaton())));
            dfaMenu.getItems().add(transitionTableItem);

            MenuItem testItem = new MenuItem();
            Strings.bind("menu.dfa.test", testItem.textProperty());
            testItem.setOnAction(event -> Utils.openNewStage(new TestPane(mainPane), 400, 600));
            dfaMenu.getItems().add(testItem);
        }
    }

    private void open()
    {
        fileChooser.setTitle(Strings.get("menu.file.open").get());
        File f = fileChooser.showOpenDialog(getScene().getWindow());
        saveLastFilePath(f);
        if (f != null) mainPane.getIoManager().open(f.getAbsolutePath());
    }

    private void saveAs()
    {
        String path = chooseSaveFile();
        if (path != null) mainPane.getIoManager().saveAs(path);
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
