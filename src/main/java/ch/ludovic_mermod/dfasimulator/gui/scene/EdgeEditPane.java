package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.logic.Link;
import ch.ludovic_mermod.dfasimulator.logic.Simulation;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class EdgeEditPane extends EditPane
{
    public EdgeEditPane(Simulation simulation, Link link)
    {
        // Setup alphabet edit
        {
            Text alphabetText = new Text();
            Strings.bind("edit_pane.edge.alphabet_text", alphabetText.textProperty());

            TextField alphabetField = new TextField();
            Strings.bind("edit_pane.edge.alphabet_prompt", alphabetField.promptTextProperty());


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

            getChildren().add(new HBox(alphabetText, alphabetField));
        }

        // Setup linked nodes edit
        {
            ComboBox<String> sourceNodeBox = new ComboBox<>();
            ComboBox<String> targetNodeBox = new ComboBox<>();

            var items = FXCollections.observableList(new ArrayList<>(simulation.getStates().stream().map(State::getName).toList()));
            sourceNodeBox.setItems(items);
            targetNodeBox.setItems(items);

            simulation.getStates().addListener((SetChangeListener<? super State>) change ->
            {
                if (change.wasAdded())
                    items.add(change.getElementAdded().getName());

                if (change.wasRemoved())
                    items.add(change.getElementRemoved().getName());
            });
            sourceNodeBox.setValue(link.source().get().getName());
            targetNodeBox.setValue(link.target().get().getName());

            sourceNodeBox.valueProperty().addListener((o, ov, nv) -> link.source().set(simulation.getState(nv)));
            targetNodeBox.valueProperty().addListener((o, ov, nv) -> link.target().set(simulation.getState(nv)));

            Text linkingText = new Text();
            Strings.bind("edit_pane.edge.nodes_linking", linkingText.textProperty());

            getChildren().add(new HBox(sourceNodeBox, linkingText, targetNodeBox));
        }

        Button deleteButton = new Button();
        Strings.bind("delete", deleteButton.textProperty());
        deleteButton.setOnAction(event ->
        {
            simulation.deleteLink(link);
            simulation.getGraphPane().getMainPane().removeEditPane();
        });

        getChildren().add(deleteButton);
    }

    @Override
    public void unbind()
    {

    }
}
