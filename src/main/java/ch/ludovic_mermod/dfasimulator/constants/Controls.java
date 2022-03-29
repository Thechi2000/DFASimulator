package ch.ludovic_mermod.dfasimulator.constants;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Static grouping of all shortcuts of the application
 * May be moved in Constants in a later version
 */
public class Controls
{
    public static final ObservableObjectValue<KeyCombination> EDIT_TOOL = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> DRAG_TOOL = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> LINK_TOOL = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> SAVE     = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> SAVE_AS  = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
    public static final ObservableObjectValue<KeyCombination> OPEN     = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> NEW_FILE = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    public static final ObservableObjectValue<KeyCombination> SETTINGS = new SimpleObjectProperty<>(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
    private Controls() {}
}
