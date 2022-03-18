package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.logic.FiniteAutomaton;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class NodeEditPane extends EditPane
{
    private final State    node;
    private final CheckBox acceptingStateBox;

    public NodeEditPane(FiniteAutomaton simulation, State state)
    {
        this.node = state;

        // Name
        {
            TextField nameField = new TextField(node.name());
            Strings.bind("edit_pane.node.state_prompt", nameField.promptTextProperty());
            nameField.setOnAction(event ->
            {
                if (!simulation.hasState(nameField.getText()))
                    node.nameProperty().set(nameField.getText());
            });
            getChildren().add(nameField);
        }

        // Accepting state
        {
            acceptingStateBox = new CheckBox();
            Strings.bind("edit_pane.node.accepting_state", acceptingStateBox.textProperty());
            acceptingStateBox.setSelected(node.isAccepting());
            node.isAcceptingProperty().bindBidirectional(acceptingStateBox.selectedProperty());
            getChildren().add(acceptingStateBox);
        }

        // Delete
        {
            Button deleteButton = new Button();
            Strings.bind("delete", deleteButton.textProperty());
            deleteButton.setOnAction(event ->
            {
                simulation.removeState(state);
                simulation.getMainPane().removeEditPane();
            });
            getChildren().add(deleteButton);
        }
    }

    @Override
    public void unbind()
    {
        node.isAcceptingProperty().unbindBidirectional(acceptingStateBox.selectedProperty());
    }
}
