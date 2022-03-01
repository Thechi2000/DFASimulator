package ch.thechi2000.dfasimulator.scene;

import javafx.animation.KeyValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.input.MouseButton;

public class Controls
{
    public static class SimulatorPane
    {
        public static class Node
        {
            public static final MouseButton dragButton = MouseButton.PRIMARY;
            public static final MouseButton linkButton = MouseButton.SECONDARY;
        }
    }
}
