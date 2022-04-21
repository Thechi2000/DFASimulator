package ch.ludovic_mermod.dfasimulator.gui;

import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.constants.settings.Settings;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Pane for controls to start/update simulations and display information about them
 */
public class SimulationPane extends VBox
{
    private MainPane  mainPane;
    private TextField inputTextField;

    public void create(MainPane mainPane)
    {
        this.mainPane = mainPane;
        // Input
        {
            inputTextField = new TextField();
            Strings.bind("simulation_pane.input_prompt", inputTextField.promptTextProperty());
            inputTextField.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty());
            getChildren().add(inputTextField);
        }

       /* // Compile
        {
            Button compileButton = new Button();
            Strings.bind("simulation_pane.compile_button", compileButton.textProperty());
            compileButton.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty());
            compileButton.setOnAction(event -> mainPane.getSimulation().compileDFA());
            getChildren().add(compileButton);
        }*/

        // Simulate
        {
            Button simulateButton = new Button();
            Strings.bind("simulation_pane.simulate_button", simulateButton.textProperty());
            simulateButton.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty());
            simulateButton.setOnAction(event -> mainPane.getSimulation().startSimulation(inputTextField.getText()));
            getChildren().add(simulateButton);
        }

        // Separator
        getChildren().add(new Separator(Orientation.VERTICAL));

        // Remaining input
        {
            Text remainingInputText = new Text();
            remainingInputText.textProperty().bind(mainPane.getGraphPane().remainingInputProperty());
            getChildren().add(remainingInputText);
        }

        // Next step
        {
            Button nextStepButton = new Button();
            nextStepButton.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty().not());
            Strings.bind("simulation_pane.next_step", nextStepButton.textProperty());
            nextStepButton.setOnAction(event -> mainPane.getSimulation().nextSimulationStep());
            getChildren().add(nextStepButton);
        }

        // Finish
        {
            Button finishButton = new Button();
            finishButton.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty().not());
            Strings.bind("simulation_pane.finish", finishButton.textProperty());
            finishButton.setOnAction(event -> mainPane.getSimulation().finish());
            getChildren().add(finishButton);
        }

        // Result
        {
            Text resultText = new Text();
            mainPane.getSimulation().resultProperty().addListener((o, ov, nv) -> updateResultText(resultText));
            mainPane.getSimulation().simulationEndedProperty().addListener((o, ov, nv) -> updateResultText(resultText));
            Strings.get("simulation_pane.result_false").addListener((o, ov, nv) -> updateResultText(resultText));
            Strings.get("simulation_pane.result_true").addListener((o, ov, nv) -> updateResultText(resultText));
            getChildren().add(resultText);
        }

        getStyleClass().add("background");
    }

    private void updateResultText(Text resultText)
    {
        if (mainPane.getGraphPane().initialInputProperty().get().equals("") || !mainPane.getSimulation().simulationEndedProperty().get())
        {
            resultText.setVisible(false);
            return;
        }

        resultText.setVisible(true);
        Strings.bindFormat("simulation_pane.result_" + mainPane.getSimulation().resultProperty().get(), resultText.textProperty(), mainPane.getGraphPane().initialInputProperty().get());
    }
}
