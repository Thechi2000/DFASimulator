package ch.ludovic_mermod.dfasimulator.gui.scene.components;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.DFA;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;

import java.util.Map;

public class TransitionTable extends Region
{
    private final TableView<Map<Character, String>> tableView;

    public TransitionTable()
    {
        tableView = new TableView<>();
        getChildren().add(tableView);
    }

    public TransitionTable(DFA dfa)
    {
        this();
        loadDFA(dfa);
    }

    public void loadDFA(DFA dfa)
    {
        tableView.getColumns().clear();

        TableColumn<Map<Character, String>, String> labelColumn = new TableColumn<>();
        Strings.bind("transition_table.label_column", labelColumn.textProperty());
        labelColumn.setCellValueFactory(map -> new SimpleStringProperty(dfa.transitionMap()
                .entrySet()
                .stream()
                .filter(e -> e.getValue().equals(map.getValue()))
                .findAny()
                .orElseThrow()
                .getKey()));
        tableView.getColumns().add(labelColumn);

        dfa.alphabet().stream().sorted().forEach(character ->
        {
            TableColumn<Map<Character, String>, String> column = new TableColumn<>(character.toString());
            column.setCellValueFactory(map -> new SimpleStringProperty(map.getValue().get(character)));
            tableView.getColumns().add(column);
        });

        dfa.states().forEach(state -> tableView.getItems().add(dfa.transitionMap().get(state)));
    }
}
