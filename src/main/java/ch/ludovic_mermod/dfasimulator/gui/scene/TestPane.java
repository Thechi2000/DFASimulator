package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.utils.Utils;
import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.converter.NumberStringConverter;

import java.util.List;

public class TestPane extends VBox
{
    public TestPane(MainPane mainPane)
    {
        TextField maxValueField = new TextField();
        Strings.bind("test_pane.max_value_prompt", maxValueField.promptTextProperty());
        maxValueField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));

        ChoiceBox<Radix> radixChoiceBox = new ChoiceBox<>(FXCollections.observableList(List.of(Radix.values())));
        radixChoiceBox.setConverter(Utils.stringConverter(radix -> Strings.get("test_pane.radix_" + radix.toString().toLowerCase()).get(), string -> {throw new UnsupportedOperationException();}, "null"));
        radixChoiceBox.setValue(Radix.HEX);

        Button runButton = new Button();
        Strings.bind("test_pane.run_button", runButton.textProperty());

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.gridLinesVisibleProperty().bind(Constants.TEST_PANE_GRID_LINES);

        HBox inputBox = new HBox(maxValueField, radixChoiceBox, runButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.spacingProperty().bind(Constants.TEST_PANE_INPUT_SPACING);
        getChildren().add(inputBox);
        getChildren().add(gridPane);

        runButton.setOnAction(event -> {
            if(!mainPane.getSimulation().compileDFA()) return;

            try{
                Integer.parseInt(maxValueField.getText());
            }
            catch (Exception e)
            {
                return;
            }

            final int maxValue = Integer.parseInt(maxValueField.getText());
            int maxBinLen = Math.max(6, Integer.toBinaryString(maxValue).length()),
                    maxHexLen = maxBinLen / 4,
                    maxDecLen = (int) Math.ceil(Math.log10(maxValue));

            for (int i = 0; i < maxValue; ++i)
            {
                Text text = new Text();
                text.fontProperty().bind(Constants.FONT);
                text.fillProperty().bind(mainPane.getSimulation().test(Integer.toBinaryString(i)) ? Constants.TEST_PANE_SUCCESS : Constants.TEST_PANE_FAILURE);

                int finalI = i;
                text.textProperty().bind(CustomBindings.binding(() ->
                        switch (radixChoiceBox.valueProperty().get())
                                {
                                    case HEX -> String.format(" %" + maxHexLen + "x ", finalI);
                                    case DEC -> String.format(" %" + maxDecLen + "d ", finalI);
                                    case BIN -> String.format(" %" + maxBinLen + "s ", Integer.toBinaryString(finalI));
                                }, radixChoiceBox.valueProperty()));

                gridPane.add(text, i / 16, i % 16);
            }
        });
    }

    enum Radix
    {
        HEX,
        DEC,
        BIN
    }
}
