package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

class NodeEditPane extends VBox
{
    public NodeEditPane(SimulationPane pane, StateNode node)
    {
        {
            TextField text = new TextField(node.getState().getName());
            Strings.bind("editpane.node.state_prompt", text.promptTextProperty());
            text.setText(node.getState().getName());
            text.setOnAction(event -> node.getState().setName(text.getText()));
            getChildren().add(text);
        }

        {
            Button deleteButton = new Button();
            Strings.bind("delete", deleteButton.textProperty());
            deleteButton.setOnAction(event ->
            {
                pane.deleteNode(node);
                pane.removeEditPane();
            });
            getChildren().add(deleteButton);
        }
    }
}
