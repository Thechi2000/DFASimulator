package ch.ludovic_mermod.dfasimulator.constants;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import static javafx.scene.paint.Color.*;

public class Constants
{
    public static final ObservableObjectValue<Font> GRAPH_FONT     = new SimpleObjectProperty<>(Font.font("Courier", 20));
    public static final ObservableObjectValue<Font> NODE_NAME_FONT = new SimpleObjectProperty<>(Font.font("Courier", 20));

    public static final ObservableIntegerValue       NODE_INNER_CIRCLE_RADIUS = new SimpleIntegerProperty(50);
    public static final ObservableIntegerValue       NODE_OUTER_CIRCLE_RADIUS = new SimpleIntegerProperty(60);
    public static final ObservableObjectValue<Color> NODE_BASE_COLOR          = new SimpleObjectProperty<>(GREEN);
    public static final ObservableObjectValue<Color> NODE_CURRENT_COLOR       = new SimpleObjectProperty<>(RED);
    public static final ObservableObjectValue<Color> NODE_INITIAL_COLOR       = new SimpleObjectProperty<>(BLUE);

    public static final ObservableIntegerValue       EDGE_LINE_WIDTH      = new SimpleIntegerProperty(10);
    public static final ObservableObjectValue<Color> EDGE_LINE_COLOR      = new SimpleObjectProperty<>(BLACK);
    public static final ObservableDoubleValue        EDGE_SIDELINE_LENGTH = new SimpleDoubleProperty(35);

    public static final ObservableDoubleValue  EDGE_TEXT_DISTANCE_FROM_LINE          = new SimpleDoubleProperty(25);
    public static final ObservableDoubleValue  EDGE_TEXT_DISTANCE_FROM_NODE_FACTOR   = new SimpleDoubleProperty(0.25);
    public static final ObservableDoubleValue  EDGE_TEXT_DISTANCE_FROM_NODE_ABSOLUTE = new SimpleDoubleProperty(20);
    public static final ObservableBooleanValue EDGE_TEXT_USE_ABSOLUTE_DISTANCE       = new SimpleBooleanProperty(true);

    public static final ObservableDoubleValue        TEST_PANE_INPUT_SPACING = new SimpleDoubleProperty(20);
    public static final ObservableBooleanValue       TEST_PANE_GRID_LINES    = new SimpleBooleanProperty(true);
    public static final ObservableObjectValue<Paint> TEST_PANE_SUCCESS       = new SimpleObjectProperty<>(GREEN);
    public static final ObservableObjectValue<Paint> TEST_PANE_FAILURE       = new SimpleObjectProperty<>(RED);

    public static final ObservableDoubleValue        CONTROL_POINT_RADIUS = new SimpleDoubleProperty(5);
    public static final ObservableObjectValue<Paint> CONTROL_POINT_FILL   = new SimpleObjectProperty<>(RED);

    public static final ObservableDoubleValue        CONTROL_LINE_WIDTH = new SimpleDoubleProperty(3);
    public static final ObservableObjectValue<Paint> CONTROL_LINE_FILL  = new SimpleObjectProperty<>(RED);
}