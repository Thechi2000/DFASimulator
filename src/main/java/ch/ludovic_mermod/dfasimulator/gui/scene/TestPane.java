package ch.ludovic_mermod.dfasimulator.gui.scene;

import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.lang.Strings;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import ch.ludovic_mermod.dfasimulator.utils.Utils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestPane extends VBox
{
    private final MainPane  mainPane;
    private final TextField discardedCharacterField;

    public TestPane(MainPane mainPane)
    {
        this.mainPane = mainPane;
        TextField maxValueField = new TextField();
        Strings.bind("test_pane.max_value_prompt", maxValueField.promptTextProperty());
        maxValueField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));

        discardedCharacterField = new TextField();
        discardedCharacterField.setText("0");
        Strings.bind("test_pane.discarded_character_prompt", discardedCharacterField.promptTextProperty());

        ChoiceBox<Radix> radixChoiceBox = new ChoiceBox<>(FXCollections.observableList(List.of(Radix.values())));
        radixChoiceBox.setConverter(Utils.stringConverter(radix -> Strings.get("test_pane.radix_" + radix.toString().toLowerCase()).get(), string -> {throw new UnsupportedOperationException();}, "null"));
        radixChoiceBox.setValue(Radix.HEX);

        Button runButton = new Button();
        Strings.bind("test_pane.run_button", runButton.textProperty());

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.gridLinesVisibleProperty().bind(Constants.TEST_PANE_GRID_LINES);

        HBox inputBox = new HBox(maxValueField, mainPane.getFiniteAutomaton().hasBinaryAlphabet().getValue() ? radixChoiceBox : discardedCharacterField, runButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.spacingProperty().bind(Constants.TEST_PANE_INPUT_SPACING);
        getChildren().add(inputBox);
        getChildren().add(gridPane);

        mainPane.getFiniteAutomaton().hasBinaryAlphabet().addListener((o, ov, nv) -> inputBox.getChildren().set(1, nv ? radixChoiceBox : discardedCharacterField));

        runButton.setOnAction(event -> {
            if (!mainPane.getSimulation().compileDFA()) return;

            try
            {
                Integer.parseInt(maxValueField.getText());
            }
            catch (Exception e)
            {
                return;
            }

            final int maxValue = Integer.parseInt(maxValueField.getText());

            Set<Character> alphabet = mainPane.getFiniteAutomaton().alphabet();
            if (alphabet.size() == 2 && alphabet.contains('0') && alphabet.contains('1'))
            {
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
            }
            else
            {
                var entries = generateEntries(maxValue);

                for (int i = 0; i < entries.size(); ++i)
                {
                    Text text = new Text(String.format(" %" + (int) Math.ceil(Math.log(maxValue) / Math.log(alphabet.size())) + "s ", entries.get(i)));
                    text.fontProperty().bind(Constants.FONT);
                    text.fillProperty().bind(mainPane.getSimulation().test(entries.get(i)) ? Constants.TEST_PANE_SUCCESS : Constants.TEST_PANE_FAILURE);
                    gridPane.add(text, i / 16, i % 16);
                }
            }
        });
    }

    private List<String> generateEntries(int count)
    {
        List<Character> alphabet = new ArrayList<>(mainPane.getFiniteAutomaton().alphabet());
        char c = 0;

        if (discardedCharacterField.getText().length() == 1)
        {
            c = discardedCharacterField.getText().charAt(0);

            if (alphabet.contains(c))
            {
                alphabet.remove((Character) c);
                alphabet.add(0, c);
            }
        }

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < (int) Math.ceil(Math.log(count) / Math.log(alphabet.size())); ++i) list.add(0);

        List<String> result = new ArrayList<>();

        for (int i = 0; i < count; ++i)
        {
            result.add(convert(alphabet, list, c));
            increment(list, alphabet.size());
        }

        return result;
    }

    private void increment(List<Integer> list, int bound)
    {
        if (list.get(list.size() - 1) == bound - 1)
        {
            list.set(list.size() - 1, 0);
            increment(list.subList(0, list.size() - 1), bound);
        }
        else
            list.set(list.size() - 1, list.get(list.size() - 1) + 1);
    }

    private String convert(List<Character> alphabet, List<Integer> list, char discarded)
    {
        StringBuilder sb = new StringBuilder();
        list.forEach(i -> {
            if (alphabet.get(i) == discarded && sb.isEmpty()) return;
            sb.append(alphabet.get(i));
        });
        return sb.toString();
    }

    enum Radix
    {
        HEX,
        DEC,
        BIN
    }
}
