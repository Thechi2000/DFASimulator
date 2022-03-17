package ch.ludovic_mermod.dfasimulator.constants;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class Controls
{
    public static final ObservableObjectValue<KeyCombination> editTool = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> dragTool = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> linkTool = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN));

    public static final ObservableObjectValue<KeyCombination> save    = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> saveAs  = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    public static final ObservableObjectValue<KeyCombination> open    = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> newFile = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
}
