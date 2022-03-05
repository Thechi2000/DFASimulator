package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SimulationSettingsPane extends VBox
{
    public void create(MainPane mainPane)
    {
        TextField inputTextField = new TextField();
        Strings.bind("simulation_pane.input_prompt", inputTextField.promptTextProperty());
        getChildren().add(inputTextField);

        Button compileButton = new Button();
        Strings.bind("simulation_pane.compile_button", compileButton.textProperty());
        compileButton.setOnAction(event -> mainPane.getGraphPane().compileDFA());
        getChildren().add(compileButton);

        Button simulateButton = new Button();
        Strings.bind("simulation_pane.simulate_button", simulateButton.textProperty());
        simulateButton.setOnAction(event -> mainPane.getGraphPane().startSimulation(inputTextField.getText()));
        getChildren().add(simulateButton);
    }
}
