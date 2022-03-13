package ch.ludovic_mermod.dfasimulator.gui.scene.components;

import ch.ludovic_mermod.dfasimulator.Utils;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.DFA;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

public class TransitionTable extends ScrollPane
{
    private final TableView<DFA.State> tableView;

    public TransitionTable()
    {
        tableView = new TableView<>();
        setContent(tableView);
    }

    public TransitionTable(DFA dfa)
    {
        this();
        loadDFA(dfa);
    }

    public void loadDFA(DFA dfa)
    {
        tableView.getColumns().clear();
        tableView.getItems().clear();

        TableColumn<DFA.State, String> nameColumn = new TableColumn<>();
        Strings.bind("transition_table.label_column", nameColumn.textProperty());
        nameColumn.setCellValueFactory(cellFeatures -> new SimpleStringProperty(cellFeatures.getValue().name()));
        nameColumn.prefWidthProperty().bind(widthProperty().divide(dfa.alphabet.size() + 3));
        tableView.getColumns().add(nameColumn);

        dfa.alphabet.stream().sorted().forEach(character ->
        {
            TableColumn<DFA.State, ChoiceBox<DFA.State>> column = new TableColumn<>(character.toString());

            column.setCellValueFactory(map ->
            {
                ChoiceBox<DFA.State> choiceBox = new ChoiceBox<>();
                choiceBox.setItems(FXCollections.observableList(dfa.states.stream().toList()));
                choiceBox.setConverter(Utils.stringConverter(DFA.State::name, dfa::state));
                choiceBox.setValue(dfa.transitionMap.get(map.getValue()).get(character));
                //choiceBox.setDisable(true);
                return new SimpleObjectProperty<>(choiceBox);
            });

            column.prefWidthProperty().bind(widthProperty().divide(dfa.alphabet.size() + 3));
            tableView.getColumns().add(column);
        });

        TableColumn<DFA.State, CheckBox> acceptingColumn = new TableColumn<>();
        Strings.bind("transition_table.accepting_column", acceptingColumn.textProperty());
        acceptingColumn.setCellValueFactory(cellFeatures ->
        {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(cellFeatures.getValue().isAccepting());
            return new SimpleObjectProperty<>(checkBox);
        });
        acceptingColumn.prefWidthProperty().bind(widthProperty().divide(dfa.alphabet.size() + 3));
        tableView.getColumns().add(acceptingColumn);

        TableColumn<DFA.State, Toggle> initialColumn = new TableColumn<>();
        Strings.bind("transition_table.initial_column", initialColumn.textProperty());

        ToggleGroup toggleGroup = new ToggleGroup();
        initialColumn.setCellValueFactory(cellFeatures ->
        {
            Toggle button = new RadioButton();
            button.setToggleGroup(toggleGroup);
            button.setSelected(cellFeatures.getValue().isInitial());
            return new SimpleObjectProperty<>(button);
        });
        initialColumn.prefWidthProperty().bind(widthProperty().divide(dfa.alphabet.size() + 3));
        tableView.getColumns().add(initialColumn);

        dfa.states.forEach(state -> tableView.getItems().add(state));
    }
}
