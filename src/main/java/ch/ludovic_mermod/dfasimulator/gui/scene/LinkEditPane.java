package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.stream.Collectors;

class LinkEditPane extends VBox
{
    private final TextField alphabetField;
    private final ComboBox<StateNode> sourceNodeBox, targetNodeBox;
    private final Button deleteButton;
    private final SimulationPane simulationPane;

    private ChangeListener<? super String> alphabetFieldListener;
    private Link link;

    public LinkEditPane(SimulationPane simulationPane)
    {
        this.simulationPane = simulationPane;
        deleteButton = new Button();

        // Setup alphabet edit
        {
            Text alphabetText = new Text();
            Strings.bind("editpane.link.alphabet_text", alphabetText.textProperty());

            alphabetField = new TextField();
            Strings.bind("editpane.link.alphabet_prompt", alphabetField.promptTextProperty());

            getChildren().add(new HBox(alphabetText, alphabetField));
        }

        // Setup linked nodes edit
        {
            sourceNodeBox = new ComboBox<>(simulationPane.getNodes());
            targetNodeBox = new ComboBox<>(simulationPane.getNodes());

            sourceNodeBox.setConverter(new StateNodeStringConverter(simulationPane));
            targetNodeBox.setConverter(new StateNodeStringConverter(simulationPane));

            Text linkingText = new Text();
            Strings.bind("editpane.link.nodes_linking", linkingText.textProperty());

            getChildren().add(new HBox(sourceNodeBox, linkingText, targetNodeBox));
        }

        Strings.bind("delete", deleteButton.textProperty());
    }

    private void bind(Link link)
    {
        if (alphabetFieldListener != null)
            alphabetField.textProperty().removeListener(alphabetFieldListener);
        alphabetFieldListener = (o, ov, nv) ->
        {
            var elements = Arrays.stream(nv.replace(" ", "").split(",")).toList();

            if (elements.stream().allMatch(s -> s.length() == 1))
                link.setAlphabet(elements.stream().map(s -> ((Character) s.charAt(0))).collect(Collectors.toSet()));
            else
            {
                //TODO
            }
        };

        if (this.link != null)
        {
            this.link.getSource().unbind();
            this.link.getTarget().unbind();
        }
        this.link = link;

        alphabetField.textProperty().addListener(alphabetFieldListener);

        sourceNodeBox.setValue(link.getSource().get());
        targetNodeBox.setValue(link.getTarget().get());

        sourceNodeBox.valueProperty().unbind();

        link.getSource().bind(sourceNodeBox.valueProperty());
        link.getTarget().bind(targetNodeBox.valueProperty());

        deleteButton.setOnAction(event -> simulationPane.deleteLink(link));
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
