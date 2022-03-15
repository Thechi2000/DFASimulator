package ch.ludovic_mermod.dfasimulator.gui.scene.components;

import ch.ludovic_mermod.dfasimulator.Utils;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.FiniteAutomaton;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.util.Comparator;

public class TransitionTable extends ScrollPane
{
    private static final int ADDITIONAL_COLUMNS = 4;
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

        DoubleBinding columnWidthBinding = new DoubleBinding()
        {
            {
                super.bind(widthProperty(), finiteAutomaton.alphabet());
            }

            @Override
            protected double computeValue()
            {
                return widthProperty().get() / (finiteAutomaton.alphabet().size() + ADDITIONAL_COLUMNS);
            }
        };

        // Name column
        {
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
                        if (!nv)
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
            nameColumn.prefWidthProperty().bind(columnWidthBinding);
            tableView.getColumns().add(nameColumn);

            finiteAutomaton.alphabet().stream().sorted().forEach(character -> addColumn(character, finiteAutomaton, columnWidthBinding));
        }

        // Accepting column
        {
            TableColumn<State, CheckBox> acceptingColumn = new TableColumn<>();
            Strings.bind("transition_table.accepting_column", acceptingColumn.textProperty());
            acceptingColumn.setCellValueFactory(cellFeatures ->
            {
                if (cellFeatures.getValue() == null) return new SimpleObjectProperty<>();

                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty().bindBidirectional(cellFeatures.getValue().isAcceptingProperty());
                return new SimpleObjectProperty<>(checkBox);
            });
            acceptingColumn.prefWidthProperty().bind(columnWidthBinding);
            tableView.getColumns().add(acceptingColumn);
        }

        // Alphabet columns
        {
            finiteAutomaton.alphabet().addListener((SetChangeListener<? super Character>) change ->
            {
                if (change.wasAdded())
                    addColumn(change.getElementAdded(), finiteAutomaton, columnWidthBinding);

                if (change.wasRemoved())
                    tableView.getColumns().removeIf(c -> c.getText().equals(change.getElementRemoved().toString()));
            });
        }

        // Initial column
        {
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

            initialColumn.prefWidthProperty().bind(columnWidthBinding);
            tableView.getColumns().add(initialColumn);
        }

        // Delete column
        {
            TableColumn<State, Button> deleteColumn = new TableColumn<>();
            deleteColumn.setCellValueFactory(cellFeatures ->
            {
                if(cellFeatures.getValue() == null) return new SimpleObjectProperty<>();

                Button button = new Button();
                Strings.bind("transition_pane.delete_button", button.textProperty());
                button.setOnAction(event -> finiteAutomaton.removeState(cellFeatures.getValue()));
                return new SimpleObjectProperty<>(button);
            });
            deleteColumn.prefWidthProperty().bind(columnWidthBinding);
            tableView.getColumns().add(deleteColumn);
        }

        // Add items
        {
            tableView.getItems().addAll(finiteAutomaton.states());
            tableView.getItems().add(null);

            Comparator<State> stateComparator = (o1, o2) ->
                    o1 == null && o2 == null ? 0 :
                            o1 == null ? Integer.MAX_VALUE :
                                    o2 == null ? Integer.MIN_VALUE :
                                            o1.name().compareTo(o2.name());
            final ChangeListener<String> nameListener = (o, ov, nv) -> tableView.getItems().sort(stateComparator);
            tableView.getItems().sort(stateComparator);
            finiteAutomaton.states().forEach(s -> s.nameProperty().addListener(nameListener));

            finiteAutomaton.states().addListener((ListChangeListener<? super State>) change ->
            {
                change.next();

                if (change.wasPermutated()) return;

                if (change.wasAdded())
                    change.getAddedSubList().forEach(s ->
                    {
                        tableView.getItems().add(s);
                        s.nameProperty().addListener(nameListener);
                    });

                if (change.wasRemoved())
                    change.getRemoved().forEach(s ->
                    {
                        tableView.getItems().remove(s);
                        s.nameProperty().removeListener(nameListener);
                    });
            });
        }
    }

    private void addColumn(Character character, FiniteAutomaton finiteAutomaton, DoubleBinding columnWidthBinding)
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

        column.prefWidthProperty().bind(columnWidthBinding);
        tableView.getColumns().add(column);
    }
}
