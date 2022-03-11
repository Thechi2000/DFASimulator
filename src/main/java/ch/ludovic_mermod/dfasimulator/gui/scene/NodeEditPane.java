package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.Simulation;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class NodeEditPane extends EditPane
{
    private final State node;

    public NodeEditPane(Simulation simulation, State state)
    {
        this.node = state;

        // Name
        {
            TextField nameField = new TextField(node.getName());
            Strings.bind("edit_pane.node.state_prompt", nameField.promptTextProperty());
            nameField.setText(node.getName());
            nameField.setOnAction(event ->
            {
                if (!simulation.hasState(nameField.getText()))
                    node.nameProperty().set(nameField.getText());
            });
            getChildren().add(nameField);
        }

        // Initial state
        {
            CheckBox initialStateBox = new CheckBox();
            Strings.bind("edit_pane.node.initial_state", initialStateBox.textProperty());
            initialStateBox.setSelected(node.initialProperty().get());
            node.initialProperty().bind(initialStateBox.selectedProperty());
            getChildren().add(initialStateBox);
        }

        // Accepting state
        {
            CheckBox acceptingStateBox = new CheckBox();
            Strings.bind("edit_pane.node.accepting_state", acceptingStateBox.textProperty());
            acceptingStateBox.setSelected(node.acceptingProperty().get());
            node.acceptingProperty().bind(acceptingStateBox.selectedProperty());
            getChildren().add(acceptingStateBox);
        }

        // Delete
        {
            Button deleteButton = new Button();
            Strings.bind("delete", deleteButton.textProperty());
            deleteButton.setOnAction(event ->
            {
                simulation.deleteState(state);
                simulation.getGraphPane().getMainPane().removeEditPane();
            });
            getChildren().add(deleteButton);
        }
    }

    @Override
    public void unbind()
    {
        node.initialProperty().unbind();
        node.acceptingProperty().unbind();
    }
}
