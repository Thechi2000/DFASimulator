package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SimulationStatePane extends VBox
{
    private MainPane mainPane;

    public SimulationStatePane()
    {

    }

    public void create(MainPane mainPane)
    {
        this.mainPane = mainPane;
        // Remaining input
        Text remainingInputText = new Text();
        remainingInputText.textProperty().bind(mainPane.getGraphPane().remainingInputProperty());
        getChildren().add(remainingInputText);

        // Next step
        Button nextStepButton = new Button();
        nextStepButton.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty().not());
        Strings.bind("simulation_state_pane.next_step", nextStepButton.textProperty());
        nextStepButton.setOnAction(event -> mainPane.getGraphPane().nextSimulationStep());
        getChildren().add(nextStepButton);

        // Finish
        Button finishButton = new Button();
        finishButton.disableProperty().bind(mainPane.getGraphPane().isSimulatingProperty().not());
        Strings.bind("simulation_state_pane.finish", finishButton.textProperty());
        finishButton.setOnAction(event -> mainPane.getGraphPane().finish());
        getChildren().add(finishButton);

        // Result
        Text resultText = new Text();
        mainPane.getGraphPane().resultProperty().addListener((o, ov, nv) -> updateResultText(resultText));
        mainPane.getGraphPane().initialInputProperty().addListener((o, ov, nv) -> updateResultText(resultText));
        mainPane.getGraphPane().simulationEndedProperty().addListener((o, ov, nv) -> updateResultText(resultText));
        Strings.get("simulation_state_pane.result_false").addListener((o, ov, nv) -> updateResultText(resultText));
        Strings.get("simulation_state_pane.result_true").addListener((o, ov, nv) -> updateResultText(resultText));
        getChildren().add(resultText);
    }

    private void updateResultText(Text resultText)
    {
        if (mainPane.getGraphPane().initialInputProperty().get().equals("") || !mainPane.getGraphPane().simulationEndedProperty().get())
        {
            resultText.setVisible(false);
            return;
        }

        resultText.setVisible(true);
        resultText.setText(Strings.get("simulation_state_pane.result_" + mainPane.getGraphPane().resultProperty().get()).get().formatted(mainPane.getGraphPane().initialInputProperty()));
    }
}
