package ch.thechi2000.dfasimulator.scene;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static java.lang.Math.sqrt;
import static javafx.scene.paint.Color.*;

public class Constants
{
    protected static class Node
    {
        protected static class Circle
        {
            protected static final ObservableIntegerValue radius = new SimpleIntegerProperty(50);
            protected static final ObservableObjectValue<Color> color = new SimpleObjectProperty<>(GREEN);
        }

        protected static class Text
        {
            protected static final ObservableObjectValue<Font> font = new SimpleObjectProperty<>(Font.font("Courier", 30));
        }
    }

    protected static class Link
    {
        protected static class Line
        {
            protected static final ObservableIntegerValue width = new SimpleIntegerProperty(10);
            protected static final ObservableObjectValue<Color> color = new SimpleObjectProperty<>(BLACK);
            protected static final ObservableDoubleValue normalFactor = new SimpleDoubleProperty(1 / sqrt(3));
            protected static final ObservableDoubleValue sidelineLength = new SimpleDoubleProperty(20);
        }

        protected static class Text
        {
            protected static final ObservableDoubleValue distance = new SimpleDoubleProperty(25);
        }
    }
}
