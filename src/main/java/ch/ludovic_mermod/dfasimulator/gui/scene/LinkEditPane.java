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
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

class LinkEditPane extends VBox
{
    public LinkEditPane(SimulationPane simulationPane, Link link)
    {

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
                    link.setAlphabet(elements.stream().map(s -> s.charAt(0)).collect(Collectors.toSet()));
            });
            alphabetField.setText(link.getAlphabet().stream().map(Objects::toString).collect(Collectors.joining(", ")));

            getChildren().add(new HBox(alphabetText, alphabetField));
        }

        // Setup linked nodes edit
        {
            ComboBox<String> sourceNodeBox = new ComboBox<>();
            ComboBox<String> targetNodeBox = new ComboBox<>();

            var items = FXCollections.observableList(new ArrayList<>(simulationPane.getNodes().stream().map(n -> n.getState().getName()).toList()));
            sourceNodeBox.setItems(items);
            targetNodeBox.setItems(items);

            simulationPane.getNodes().addListener((ListChangeListener<StateNode>) change ->
            {
                change.next();
                if (change.getAddedSize() > 0)
                    items.addAll(change.getAddedSubList().stream().map(n -> n.getState().getName()).toList());

                if (change.getRemovedSize() > 0)
                    items.removeAll(change.getRemoved().stream().map(n -> n.getState().getName()).toList());
            });
            sourceNodeBox.setValue(link.getSourceName());
            targetNodeBox.setValue(link.getTargetName());

            sourceNodeBox.valueProperty().addListener((o, ov, nv) -> link.getSource().set(simulationPane.getNodes().stream().filter(n -> n.getState().getName().equals(nv)).findAny().orElseThrow()));
            targetNodeBox.valueProperty().addListener((o, ov, nv) -> link.getTarget().set(simulationPane.getNodes().stream().filter(n -> n.getState().getName().equals(nv)).findAny().orElseThrow()));

            Text linkingText = new Text();
            Strings.bind("editpane.link.nodes_linking", linkingText.textProperty());

            getChildren().add(new HBox(sourceNodeBox, linkingText, targetNodeBox));
        }

        Button deleteButton = new Button();
        Strings.bind("delete", deleteButton.textProperty());
        deleteButton.setOnAction(event ->
        {
            simulationPane.deleteLink(link);
            simulationPane.removeEditPane();
        });

        getChildren().add(deleteButton);
    }

    private static class StateNodeStringConverter extends StringConverter<StateNode>
    {
        private final SimulationPane simulationPane;

        private StateNodeStringConverter(SimulationPane simulationPane)
        {
            this.simulationPane = simulationPane;
        }

        @Override
        public String toString(StateNode stateNode)
        {
            return stateNode.getState().getName();
        }
        @Override
        public StateNode fromString(String s)
        {
            return simulationPane.getNodes().stream().filter(n -> n.getState().getName().equals(s)).findAny().orElse(null);
        }
    }
}
