package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

class EditPaneCreator extends VBox
{
    public static VBox createLinkEditPane(GraphPane graphPane, Link link)
    {
        VBox pane = new VBox();

        // Setup alphabet edit
        {
            Text alphabetText = new Text();
            Strings.bind("editpane.link.alphabet_text", alphabetText.textProperty());

            TextField alphabetField = new TextField();
            Strings.bind("editpane.link.alphabet_prompt", alphabetField.promptTextProperty());


            alphabetField.setOnAction(event ->
            {
                System.out.println("test");
                var elements = Arrays.stream(alphabetField.getText().replace(" ", "").split(",")).toList();

                if (elements.stream().allMatch(s -> s.length() == 1))
                {
                    link.alphabetProperty().clear();
                    link.alphabetProperty().addAll(elements.stream().map(s -> s.charAt(0)).collect(Collectors.toSet()));
                }
            });
            alphabetField.setText(link.alphabetProperty().get().stream().map(Objects::toString).collect(Collectors.joining(", ")));

            pane.getChildren().add(new HBox(alphabetText, alphabetField));
        }

        // Setup linked nodes edit
        {
            ComboBox<String> sourceNodeBox = new ComboBox<>();
            ComboBox<String> targetNodeBox = new ComboBox<>();

            var items = FXCollections.observableList(new ArrayList<>(graphPane.getNodes().stream().map(StateNode::getName).toList()));
            sourceNodeBox.setItems(items);
            targetNodeBox.setItems(items);

            graphPane.getNodes().addListener((ListChangeListener<StateNode>) change ->
            {
                change.next();
                if (change.getAddedSize() > 0)
                    items.addAll(change.getAddedSubList().stream().map(StateNode::getName).toList());

                if (change.getRemovedSize() > 0)
                    items.removeAll(change.getRemoved().stream().map(StateNode::getName).toList());
            });
            sourceNodeBox.setValue(link.getSourceName());
            targetNodeBox.setValue(link.getTargetName());

            sourceNodeBox.valueProperty().addListener((o, ov, nv) -> link.getSource().set(graphPane.getNodes().stream().filter(n -> n.getName().equals(nv)).findAny().orElseThrow()));
            targetNodeBox.valueProperty().addListener((o, ov, nv) -> link.getTarget().set(graphPane.getNodes().stream().filter(n -> n.getName().equals(nv)).findAny().orElseThrow()));

            Text linkingText = new Text();
            Strings.bind("editpane.link.nodes_linking", linkingText.textProperty());

            pane.getChildren().add(new HBox(sourceNodeBox, linkingText, targetNodeBox));
        }

        Button deleteButton = new Button();
        Strings.bind("delete", deleteButton.textProperty());
        deleteButton.setOnAction(event ->
        {
            graphPane.deleteLink(link);
            graphPane.removeEditPane();
        });

        pane.getChildren().add(deleteButton);

        return pane;
    }

    protected static VBox createNodeEditPane(GraphPane graphPane, StateNode node)
    {
        VBox pane = new VBox();

        {
            TextField text = new TextField(node.getName());
            Strings.bind("editpane.node.state_prompt", text.promptTextProperty());
            text.setText(node.getName());
            text.setOnAction(event -> node.nameProperty().set(text.getText()));
            pane.getChildren().add(text);
        }

        {
            Button deleteButton = new Button();
            Strings.bind("delete", deleteButton.textProperty());
            deleteButton.setOnAction(event ->
            {
                graphPane.deleteNode(node);
                graphPane.removeEditPane();
            });
            pane.getChildren().add(deleteButton);
        }

        return pane;
    }
}
