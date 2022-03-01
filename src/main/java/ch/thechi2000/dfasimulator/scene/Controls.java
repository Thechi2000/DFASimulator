package ch.thechi2000.dfasimulator.scene;

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
}
