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

import static javafx.scene.paint.Color.*;

public class Constants
{
    public static final ObservableObjectValue<Font> FONT = new SimpleObjectProperty<>(Font.font("Courier", 20));

    public static final ObservableIntegerValue       NODE_INNER_CIRCLE_RADIUS = new SimpleIntegerProperty(50);
    public static final ObservableIntegerValue       NODE_OUTER_CIRCLE_RADIUS = new SimpleIntegerProperty(60);
    public static final ObservableObjectValue<Color> NODE_BASE_COLOR          = new SimpleObjectProperty<>(GREEN);
    public static final ObservableObjectValue<Color> NODE_CURRENT_COLOR       = new SimpleObjectProperty<>(RED);
    public static final ObservableObjectValue<Color> NODE_INITIAL_COLOR       = new SimpleObjectProperty<>(BLUE);

    public static final ObservableIntegerValue       EDGE_LINE_WIDTH      = new SimpleIntegerProperty(10);
    public static final ObservableObjectValue<Color> EDGE_LINE_COLOR      = new SimpleObjectProperty<>(BLACK);
    public static final ObservableDoubleValue        EDGE_SIDELINE_LENGTH = new SimpleDoubleProperty(20);

    public static final ObservableDoubleValue  EDGE_TEXT_DISTANCE_FROM_LINE          = new SimpleDoubleProperty(25);
    public static final ObservableDoubleValue  EDGE_TEXT_DISTANCE_FROM_NODE_FACTOR   = new SimpleDoubleProperty(0.25);
    public static final ObservableDoubleValue  EDGE_TEXT_DISTANCE_FROM_NODE_ABSOLUTE = new SimpleDoubleProperty(20);
    public static final ObservableBooleanValue EDGE_TEXT_USE_ABSOLUTE_DISTANCE       = new SimpleBooleanProperty(true);
}
