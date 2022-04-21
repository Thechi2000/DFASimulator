package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.constants.settings.Settings;
import ch.ludovic_mermod.dfasimulator.gui.components.CheckComboBox;
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
import javafx.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Table to visualize and modify a FiniteAutomaton
 */
public class TablePane extends ScrollPane
{
    private final TableView<Pair<State, Boolean>> tableView;

    public TablePane()
    {
        tableView = new TableView<>();
        setContent(tableView);

        getStyleClass().add("background");
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
            TableColumn<Pair<State, Boolean>, Group> nameColumn = new TableColumn<>();
            Strings.bind("table_pane.label_column", nameColumn.textProperty());
            nameColumn.setCellValueFactory(cellFeatures ->
            {
                SimpleObjectProperty<Group> cell = new SimpleObjectProperty<>(new Group());

                if (cellFeatures.getValue().getValue())
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
                else if (cellFeatures.getValue().getKey() == null)
                {
                    return new SimpleObjectProperty<>();
                }
                else
                {
                    State state = cellFeatures.getValue().getKey();
                    Text text = new Text();
                    text.textProperty().bind(state.nameProperty());
                    cell.get().getChildren().add(text);
                }

                return cell;
            });
            nameColumn.prefWidthProperty().bind(columnWidthBinding);
            tableView.getColumns().add(nameColumn);
        }

        // Alphabet columns
        {
            TableColumn<Pair<State, Boolean>, Node> alphabetColumn = new TableColumn<>();
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
            TableColumn<Pair<State, Boolean>, CheckBox> acceptingColumn = new TableColumn<>();
            Strings.bind("table_pane.accepting_column", acceptingColumn.textProperty());
            acceptingColumn.setCellValueFactory(cellFeatures ->
            {
                if (cellFeatures.getValue().getValue() || cellFeatures.getValue().getKey() == null) return new SimpleObjectProperty<>();

                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty().bindBidirectional(cellFeatures.getValue().getKey().isAcceptingProperty());
                return new SimpleObjectProperty<>(checkBox);
            });
            acceptingColumn.prefWidthProperty().bind(columnWidthBinding);
            tableView.getColumns().add(acceptingColumn);
        }

        // Initial column
        {
            TableColumn<Pair<State, Boolean>, Toggle> initialColumn = new TableColumn<>();
            Strings.bind("table_pane.initial_column", initialColumn.textProperty());
            ToggleGroup toggleGroup = new ToggleGroup();

            initialColumn.setCellValueFactory(cellFeatures ->
            {
                if (cellFeatures.getValue().getValue() || cellFeatures.getValue().getKey() == null) return new SimpleObjectProperty<>();

                Toggle button = new RadioButton();
                button.setUserData(cellFeatures.getValue().getKey());
                button.setToggleGroup(toggleGroup);
                button.setSelected(cellFeatures.getValue().getKey().isInitialBinding().get());
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
            TableColumn<Pair<State, Boolean>, Node> deleteColumn = new TableColumn<>();
            deleteColumn.setCellValueFactory(cellFeatures ->
            {
                if (cellFeatures.getValue().getValue())
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
                else if (cellFeatures.getValue().getKey() == null)
                {
                    return new SimpleObjectProperty<>();
                }
                else
                {
                    Button button = new Button();
                    Strings.bind("table_pane.delete_button", button.textProperty());
                    button.setOnAction(event -> finiteAutomaton.removeState(cellFeatures.getValue().getKey()));
                    return new SimpleObjectProperty<>(button);
                }
            });
            deleteColumn.prefWidthProperty().bind(columnWidthBinding);
            tableView.getColumns().add(deleteColumn);
        }

        // Add items
        {
            tableView.getItems().addAll(convert(finiteAutomaton.states()));
            tableView.getItems().add(new Pair<>(null, true));

            Comparator<Pair<State, Boolean>> stateComparator = (o1, o2) ->
                    o1.getKey() == null && o2.getKey() == null ? 0 :
                    o1.getKey() == null ? Integer.MAX_VALUE :
                    o2.getKey() == null ? Integer.MIN_VALUE :
                    o1.getKey().name().compareTo(o2.getKey().name());
            final ChangeListener<String> nameListener = (o, ov, nv) -> tableView.getItems().sort(stateComparator);
            tableView.getItems().sort(stateComparator);
            finiteAutomaton.states().forEach(s -> s.nameProperty().addListener(nameListener));

            finiteAutomaton.states().addListener((ListChangeListener<? super State>) change ->
            {
                change.next();

                if (change.wasAdded())
                    tableView.getItems().addAll(convert(change.getAddedSubList()));

                if (change.wasRemoved())
                    tableView.getItems().removeAll(convert(change.getRemoved()));

                tableView.getItems().sort(stateComparator);
            });
        }
    }

    private TableColumn<Pair<State, Boolean>, Node> createAlphabetColumn(Character character, FiniteAutomaton finiteAutomaton, DoubleBinding columnWidthBinding)
    {
        TableColumn<Pair<State, Boolean>, Node> column = new TableColumn<>(character.toString());

        column.setCellValueFactory(cellFeatures ->
        {
            if (cellFeatures.getValue().getValue())
            {
                Button button = new Button();
                Strings.bind("table_pane.remove_alphabet", button.textProperty());
                button.setOnAction(event -> finiteAutomaton.alphabet().remove(character));
                return new SimpleObjectProperty<>(button);
            }
            else if (cellFeatures.getValue().getKey() == null)
            {
                return new SimpleObjectProperty<>();
            }

            State state = cellFeatures.getValue().getKey();
            CheckComboBox<State> checkComboBox = new CheckComboBox<>(finiteAutomaton.states());
            checkComboBox.setSelectedItems(state.transitionMap().getValue(character));
            checkComboBox.setConverter(Utils.stringConverter(State::name, s -> {throw new UnsupportedOperationException();}, ""));
            checkComboBox.getSelectedItems().addListener((ListChangeListener<? super State>) change -> {
                change.next();
                state.transitionMap().setValue(character, checkComboBox.getSelectedItems());
            });
            return new SimpleObjectProperty<>(checkComboBox);
        });

        column.prefWidthProperty().bind(columnWidthBinding);
        return column;
    }

    private List<Pair<State, Boolean>> convert(List<? extends State> states)
    {
        return states.stream().map(s -> new Pair<>((State) s, false)).toList();
    }
}
