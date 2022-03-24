package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.logic.FiniteAutomaton;
import ch.ludovic_mermod.dfasimulator.logic.State;
import ch.ludovic_mermod.dfasimulator.utils.Utils;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.util.Comparator;
import java.util.Objects;

public class TablePane extends ScrollPane
{
    private static final int ADDITIONAL_COLUMNS = 4;

    private final TableView<State> tableView;

    public TablePane()
    {
        tableView = new TableView<>();
        setContent(tableView);
    }

    public TablePane(FiniteAutomaton finiteAutomaton)
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
                super.bind(widthProperty(), finiteAutomaton.alphabet(), tableView.getColumns());
            }

            @Override
            protected double computeValue()
            {
                return widthProperty().get() / (finiteAutomaton.alphabet().size() + tableView.getColumns().size() - 1);
            }
        };

        // Name column
        {
            TableColumn<State, Group> nameColumn = new TableColumn<>();
            Strings.bind("table_pane.label_column", nameColumn.textProperty());
            nameColumn.setCellValueFactory(cellFeatures ->
            {
                SimpleObjectProperty<Group> cell = new SimpleObjectProperty<>(new Group());

                if (cellFeatures.getValue() == null)
                {
                    TextField addStateField = new TextField();
                    cell.get().getChildren().add(addStateField);
                    addStateField.setVisible(false);

                    Button addStateButton = new Button();
                    Strings.bind("table_pane.add_state", addStateButton.textProperty());
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
                {
                    Text text = new Text();
                    text.textProperty().bind(cellFeatures.getValue().nameProperty());
                    cell.get().getChildren().add(text);
                }

                return cell;
            });
            nameColumn.prefWidthProperty().bind(columnWidthBinding);
            tableView.getColumns().add(nameColumn);
        }

        // Alphabet columns
        {
            TableColumn<State, Node> alphabetColumn = new TableColumn<>();
            Strings.bind("table_pane.alphabet_column", alphabetColumn.textProperty());

            finiteAutomaton.alphabet().stream().sorted().forEach(character -> alphabetColumn.getColumns().add(createAlphabetColumn(character, finiteAutomaton, columnWidthBinding)));

            finiteAutomaton.alphabet().addListener((SetChangeListener<? super Character>) change ->
            {
                if (change.wasAdded())
                    alphabetColumn.getColumns().add(finiteAutomaton.alphabet().size() - 1, createAlphabetColumn(change.getElementAdded(), finiteAutomaton, columnWidthBinding));

                if (change.wasRemoved())
                    alphabetColumn.getColumns().removeIf(c -> c.getText().equals(change.getElementRemoved().toString()));
            });

            tableView.getColumns().add(alphabetColumn);
        }

        // Accepting column
        {
            TableColumn<State, CheckBox> acceptingColumn = new TableColumn<>();
            Strings.bind("table_pane.accepting_column", acceptingColumn.textProperty());
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

        // Initial column
        {
            TableColumn<State, Toggle> initialColumn = new TableColumn<>();
            Strings.bind("table_pane.initial_column", initialColumn.textProperty());
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
                if (nv != null && !Objects.equals(nv.getUserData(), finiteAutomaton.initialState()))
                    finiteAutomaton.initialStateProperty().set((State) nv.getUserData());
            });
            finiteAutomaton.initialStateProperty().addListener((o, ov, nv) ->
            {
                if (toggleGroup.getSelectedToggle() != null && !Objects.equals(nv, toggleGroup.getSelectedToggle().getUserData()))
                    toggleGroup.selectToggle(toggleGroup.getToggles().stream().filter(t -> Objects.equals(t.getUserData(), nv)).findAny().orElse(null));
            });

            initialColumn.prefWidthProperty().bind(columnWidthBinding);
            tableView.getColumns().add(initialColumn);
        }

        // Delete column
        {
            TableColumn<State, Node> deleteColumn = new TableColumn<>();
            deleteColumn.setCellValueFactory(cellFeatures ->
            {
                if (cellFeatures.getValue() == null)
                {
                    Group cell = new Group();

                    TextField addAlphabetField = new TextField();
                    cell.getChildren().add(addAlphabetField);
                    addAlphabetField.setVisible(false);

                    Button addAlphabetButton = new Button();
                    Strings.bind("table_pane.add_alphabet", addAlphabetButton.textProperty());
                    cell.getChildren().add(addAlphabetButton);

                    addAlphabetField.focusedProperty().addListener((o, ov, nv) ->
                    {
                        if (!nv)
                        {
                            addAlphabetField.setVisible(false);
                            addAlphabetButton.setVisible(true);
                        }
                    });
                    addAlphabetField.setOnAction(event -> {
                        if (addAlphabetField.getText().length() == 1)
                            finiteAutomaton.alphabet().add(addAlphabetField.getText().charAt(0));
                    });

                    addAlphabetButton.setOnAction(event ->
                    {
                        addAlphabetField.setVisible(true);
                        addAlphabetButton.setVisible(false);
                        addAlphabetField.requestFocus();
                    });

                    return new SimpleObjectProperty<>(cell);
                }
                else
                {
                    Button button = new Button();
                    Strings.bind("table_pane.delete_button", button.textProperty());
                    button.setOnAction(event -> finiteAutomaton.removeState(cellFeatures.getValue()));
                    return new SimpleObjectProperty<>(button);
                }
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

    private TableColumn<State, Node> createAlphabetColumn(Character character, FiniteAutomaton finiteAutomaton, DoubleBinding columnWidthBinding)
    {
        TableColumn<State, Node> column = new TableColumn<>(character.toString());

        column.setCellValueFactory(cellFeatures ->
        {
            if (cellFeatures.getValue() == null)
            {
                Button button = new Button();
                Strings.bind("table_pane.remove_alphabet", button.textProperty());
                button.setOnAction(event -> finiteAutomaton.alphabet().remove(character));
                return new SimpleObjectProperty<>(button);
            }

            ChoiceBox<State> choiceBox = new ChoiceBox<>();
            choiceBox.setItems(finiteAutomaton.states());

            choiceBox.setConverter(Utils.stringConverter(State::name, finiteAutomaton::getState, ""));
            finiteAutomaton.states().forEach(s -> s.nameProperty().addListener((o, ov, nv) -> choiceBox.setConverter(Utils.stringConverter(State::name, finiteAutomaton::getState, ""))));

            choiceBox.valueProperty().bindBidirectional(cellFeatures.getValue().transitionMap().get(character));
            return new SimpleObjectProperty<>(choiceBox);
        });

        column.prefWidthProperty().bind(columnWidthBinding);
        return column;
    }
}
