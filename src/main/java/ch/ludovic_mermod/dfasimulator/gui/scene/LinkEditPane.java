package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class LinkEditPane extends VBox
{
    private final TextField alphabetField;
    private final ComboBox<String> sourceNodeBox, targetNodeBox;
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
            sourceNodeBox = new ComboBox<>();
            targetNodeBox = new ComboBox<>();

            simulationPane.getNodes().addListener((o, ov, nv) -> updateComboBoxesContent());
            updateComboBoxesContent();

            /*
            sourceNodeBox.setCellFactory(l -> new TextFieldListCell<>(new StateNodeStringConverter(simulationPane)));
            sourceNodeBox.setItems(simulationPane.getNodes());
            sourceNodeBox.setConverter(new StateNodeStringConverter(simulationPane));
            sourceNodeBox.setEditable(true);

            targetNodeBox.setCellFactory(l -> new TextFieldListCell<>(new StateNodeStringConverter(simulationPane)));
            targetNodeBox.setItems(simulationPane.getNodes());
            targetNodeBox.setConverter(new StateNodeStringConverter(simulationPane));
            targetNodeBox.setEditable(false);
            */

            Text linkingText = new Text();
            Strings.bind("editpane.link.nodes_linking", linkingText.textProperty());

            getChildren().add(new HBox(sourceNodeBox, linkingText, targetNodeBox));
        }

        Strings.bind("delete", deleteButton.textProperty());
    }

    private void updateComboBoxesContent()
    {
        List<String> items = simulationPane.getNodes().stream().map(n -> n.getState().getName()).toList();
        sourceNodeBox.setItems(FXCollections.observableList(items));
        targetNodeBox.setItems(FXCollections.observableList(items));
    }

    protected void bind(Link link)
    {
        if (alphabetFieldListener != null)
            alphabetField.textProperty().removeListener(alphabetFieldListener);
        alphabetFieldListener = (o, ov, nv) ->
        {
            var elements = Arrays.stream(nv.replace(" ", "").split(",")).toList();

            if (elements.stream().allMatch(s -> s.length() == 1))
                link.setAlphabet(elements.stream().map(s -> s.charAt(0)).collect(Collectors.toSet()));
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

        sourceNodeBox.setValue(link.getSourceName());
        targetNodeBox.setValue(link.getTargetName());

        sourceNodeBox.valueProperty().unbind();

        sourceNodeBox.valueProperty().addListener((o, ov, nv) -> link.getSource().set(simulationPane.getNodes().stream().filter(n -> n.getState().getName().equals(nv)).findAny().orElseThrow()));
        targetNodeBox.valueProperty().addListener((o, ov, nv) -> link.getTarget().set(simulationPane.getNodes().stream().filter(n -> n.getState().getName().equals(nv)).findAny().orElseThrow()));

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
