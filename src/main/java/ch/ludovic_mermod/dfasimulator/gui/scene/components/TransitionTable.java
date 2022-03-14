package ch.ludovic_mermod.dfasimulator.gui.scene.components;

import ch.ludovic_mermod.dfasimulator.Utils;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.FiniteAutomaton;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.text.Text;

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

        TableColumn<State, Group> nameColumn = new TableColumn<>();
        Strings.bind("transition_table.label_column", nameColumn.textProperty());
        nameColumn.setCellValueFactory(cellFeatures ->
        {
            SimpleObjectProperty<Group> cell = new SimpleObjectProperty<>(new Group());

            if (cellFeatures.getValue() == null)
            {
                TextField addStateField = new TextField();
                cell.get().getChildren().add(addStateField);
                addStateField.setVisible(false);

                Button addStateButton = new Button();
                Strings.bind("transition_table.add_state", addStateButton.textProperty());
                cell.get().getChildren().add(addStateButton);

                addStateField.focusedProperty().addListener((o, ov, nv) ->
                {
                    if(!nv)
                    {
                        addStateField.setVisible(false);
                        addStateButton.setVisible(true);
                    }
                });
                addStateField.setOnAction(event -> finiteAutomaton.addState(addStateField.getText()));

                addStateButton.setOnAction(event ->
                {
                    addStateField.setVisible(true);
                    addStateButton.setVisible(false);
                    addStateField.requestFocus();
                });
            }
            else
                cell.get().getChildren().add(new Text(cellFeatures.getValue().name()));

            return cell;
        });
        nameColumn.prefWidthProperty().bind(widthProperty().divide(finiteAutomaton.alphabet().size() + 3));
        tableView.getColumns().add(nameColumn);

        finiteAutomaton.alphabet().stream().sorted().forEach(character -> addColumn(character, finiteAutomaton));

        TableColumn<State, CheckBox> acceptingColumn = new TableColumn<>();
        Strings.bind("transition_table.accepting_column", acceptingColumn.textProperty());
        acceptingColumn.setCellValueFactory(cellFeatures ->
        {
            if (cellFeatures.getValue() == null) return new SimpleObjectProperty<>();

            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty().bindBidirectional(cellFeatures.getValue().isAcceptingProperty());
            return new SimpleObjectProperty<>(checkBox);
        });
        acceptingColumn.prefWidthProperty().bind(widthProperty().divide(finiteAutomaton.alphabet().size() + 3));
        tableView.getColumns().add(acceptingColumn);

        finiteAutomaton.alphabet().addListener((SetChangeListener<? super Character>) change ->
        {
            if (change.wasAdded())
                addColumn(change.getElementAdded(), finiteAutomaton);

            if (change.wasRemoved())
                tableView.getColumns().removeIf(c -> c.getText().equals(change.getElementRemoved().toString()));
        });

        TableColumn<State, Toggle> initialColumn = new TableColumn<>();
        Strings.bind("transition_table.initial_column", initialColumn.textProperty());

        ToggleGroup toggleGroup = new ToggleGroup();
        initialColumn.setCellValueFactory(cellFeatures ->
        {
            if (cellFeatures.getValue() == null) return new SimpleObjectProperty<>();

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

        tableView.getItems().addAll(finiteAutomaton.states());
        tableView.getItems().add(null);

        finiteAutomaton.states().addListener((ListChangeListener<? super State>) change ->
        {
            change.next();

            tableView.getItems().clear();
            tableView.getItems().addAll(finiteAutomaton.states());
            tableView.getItems().add(null);
        });
    }

    private void addColumn(Character character, FiniteAutomaton finiteAutomaton)
    {
        TableColumn<State, ChoiceBox<State>> column = new TableColumn<>(character.toString());

        column.setCellValueFactory(cellFeatures ->
        {
            if (cellFeatures.getValue() == null) return new SimpleObjectProperty<>();

            ChoiceBox<State> choiceBox = new ChoiceBox<>();
            choiceBox.setItems(finiteAutomaton.states());
            choiceBox.setConverter(Utils.stringConverter(State::name, finiteAutomaton::getState, ""));
            choiceBox.valueProperty().bindBidirectional(cellFeatures.getValue().transitionMap().get(character));
            return new SimpleObjectProperty<>(choiceBox);
        });

        column.prefWidthProperty().bind(widthProperty().divide(finiteAutomaton.alphabet().size() + 3));
        tableView.getColumns().add(column);
    }
}
