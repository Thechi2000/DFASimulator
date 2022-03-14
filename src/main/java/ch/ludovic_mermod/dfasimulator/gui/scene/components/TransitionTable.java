package ch.ludovic_mermod.dfasimulator.gui.scene.components;

import ch.ludovic_mermod.dfasimulator.Utils;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.FiniteAutomaton;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;

public class TransitionTable extends ScrollPane
{
    private final TableView<State> tableView;

    public TransitionTable()
    {
        tableView = new TableView<>();
        setContent(tableView);
    }

    public TransitionTable(FiniteAutomaton finiteAutomaton)
    {
        this();
        loadFiniteAutomaton(finiteAutomaton);
    }

    public void loadFiniteAutomaton(FiniteAutomaton finiteAutomaton)
    {
        tableView.getColumns().clear();
        tableView.getItems().clear();

        TableColumn<State, String> nameColumn = new TableColumn<>();
        Strings.bind("transition_table.label_column", nameColumn.textProperty());
        nameColumn.setCellValueFactory(cellFeatures -> new SimpleStringProperty(cellFeatures.getValue().name()));
        nameColumn.prefWidthProperty().bind(widthProperty().divide(finiteAutomaton.alphabet().size() + 3));
        tableView.getColumns().add(nameColumn);

        finiteAutomaton.alphabet().stream().sorted().forEach(character ->
        {
            TableColumn<State, ChoiceBox<State>> column = new TableColumn<>(character.toString());

            column.setCellValueFactory(map ->
            {
                ChoiceBox<State> choiceBox = new ChoiceBox<>();
                choiceBox.setItems(FXCollections.observableList(finiteAutomaton.states().stream().toList()));
                choiceBox.setConverter(Utils.stringConverter(State::name, finiteAutomaton::getState));
                choiceBox.valueProperty().bindBidirectional(map.getValue().transitionMap().get(character));
                return new SimpleObjectProperty<>(choiceBox);
            });

            column.prefWidthProperty().bind(widthProperty().divide(finiteAutomaton.alphabet().size() + 3));
            tableView.getColumns().add(column);
        });

        TableColumn<State, CheckBox> acceptingColumn = new TableColumn<>();
        Strings.bind("transition_table.accepting_column", acceptingColumn.textProperty());
        acceptingColumn.setCellValueFactory(cellFeatures ->
        {
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty().bindBidirectional(cellFeatures.getValue().isAcceptingProperty());
            return new SimpleObjectProperty<>(checkBox);
        });
        acceptingColumn.prefWidthProperty().bind(widthProperty().divide(finiteAutomaton.alphabet().size() + 3));
        tableView.getColumns().add(acceptingColumn);

        TableColumn<State, Toggle> initialColumn = new TableColumn<>();
        Strings.bind("transition_table.initial_column", initialColumn.textProperty());

        ToggleGroup toggleGroup = new ToggleGroup();
        initialColumn.setCellValueFactory(cellFeatures ->
        {
            Toggle button = new RadioButton();
            button.setUserData(cellFeatures.getValue());
            button.setToggleGroup(toggleGroup);
            button.setSelected(cellFeatures.getValue().isInitialBinding().get());
            return new SimpleObjectProperty<>(button);
        });

        toggleGroup.selectedToggleProperty().addListener((o, ov, nv) ->
        {
            if (!nv.getUserData().equals(finiteAutomaton.initialState()))
                finiteAutomaton.initialStateProperty().set((State) nv.getUserData());
        });
        finiteAutomaton.initialStateProperty().addListener((o, ov, nv) ->
        {
            if (!toggleGroup.getSelectedToggle().getUserData().equals(nv))
                toggleGroup.selectToggle(toggleGroup.getToggles().stream().filter(t -> t.getUserData().equals(nv)).findAny().orElse(null));
        });

        initialColumn.prefWidthProperty().bind(widthProperty().divide(finiteAutomaton.alphabet().size() + 3));
        tableView.getColumns().add(initialColumn);

        finiteAutomaton.states().forEach(state -> tableView.getItems().add(state));
    }
}
