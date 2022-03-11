package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SimulationPane extends VBox
{
    private MainPane mainPane;
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

        // Compile
        {
            Button compileButton = new Button();
            Strings.bind("simulation_pane.compile_button", compileButton.textProperty());
            compileButton.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty());
            compileButton.setOnAction(event -> mainPane.getGraphPane().getSimulation().compileDFA());
            getChildren().add(compileButton);
        }

        // Simulate
        {
            Button simulateButton = new Button();
            Strings.bind("simulation_pane.simulate_button", simulateButton.textProperty());
            simulateButton.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty());
            simulateButton.setOnAction(event -> mainPane.getGraphPane().getSimulation().startSimulation(inputTextField.getText()));
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
            nextStepButton.setOnAction(event -> mainPane.getGraphPane().getSimulation().nextSimulationStep());
            getChildren().add(nextStepButton);
        }

        // Finish
        {
            Button finishButton = new Button();
            finishButton.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty().not());
            Strings.bind("simulation_pane.finish", finishButton.textProperty());
            finishButton.setOnAction(event -> mainPane.getGraphPane().getSimulation().finish());
            getChildren().add(finishButton);
        }

        // Result
        {
            Text resultText = new Text();
            mainPane.getGraphPane().resultProperty().addListener((o, ov, nv) -> updateResultText(resultText));
            mainPane.getGraphPane().simulationEndedProperty().addListener((o, ov, nv) -> updateResultText(resultText));
            Strings.get("simulation_pane.result_false").addListener((o, ov, nv) -> updateResultText(resultText));
            Strings.get("simulation_pane.result_true").addListener((o, ov, nv) -> updateResultText(resultText));
            getChildren().add(resultText);
        }
    }

    private void updateResultText(Text resultText)
    {
        if (mainPane.getGraphPane().initialInputProperty().get().equals("") || !mainPane.getGraphPane().simulationEndedProperty().get())
        {
            resultText.setVisible(false);
            return;
        }

        resultText.setVisible(true);
        Strings.format("simulation_state_pane.result_" + mainPane.getGraphPane().resultProperty().get(), resultText.textProperty(), mainPane.getGraphPane().initialInputProperty().get());
    }
}
