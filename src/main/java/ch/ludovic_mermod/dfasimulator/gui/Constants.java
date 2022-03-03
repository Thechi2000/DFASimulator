package ch.ludovic_mermod.dfasimulator.gui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.GREEN;

public class Constants
{
    public static class Node
    {
        public static class Circle
        {
            public static final ObservableIntegerValue radius = new SimpleIntegerProperty(50);
            public static final ObservableObjectValue<Color> color = new SimpleObjectProperty<>(GREEN);
        }

        public static class Text
        {
            public static final ObservableObjectValue<Font> font = new SimpleObjectProperty<>(Font.font("Courier", 20));
        }
    }

    public static class Link
    {
        public static class Line
        {
            public static final ObservableIntegerValue width = new SimpleIntegerProperty(10);
            public static final ObservableObjectValue<Color> color = new SimpleObjectProperty<>(BLACK);
            public static final ObservableDoubleValue sidelineLength = new SimpleDoubleProperty(20);
        }

        public static class Text
        {
            public static final ObservableDoubleValue distanceFromLine = new SimpleDoubleProperty(25);

            public static final ObservableDoubleValue distanceFromNodeFactor = new SimpleDoubleProperty(0.25);
            public static final ObservableDoubleValue distanceFromNodeAbsolute = new SimpleDoubleProperty(20);
            public static final ObservableBooleanValue usesAbsoluteDistance = new SimpleBooleanProperty(true);
        }
    }
}
