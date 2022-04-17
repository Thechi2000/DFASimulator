module ch.ludovic_mermod.dfasimulator {
    requires javafx.controls;
    requires com.google.gson;
    requires java.logging;

    exports ch.ludovic_mermod.dfasimulator;
    exports ch.ludovic_mermod.dfasimulator.json;
    exports ch.ludovic_mermod.dfasimulator.logic;
    exports ch.ludovic_mermod.dfasimulator.gui;

    opens ch.ludovic_mermod.dfasimulator.gui to javafx.fxml;
    opens ch.ludovic_mermod.dfasimulator.gui.components to javafx.fxml;
    opens ch.ludovic_mermod.dfasimulator.utils to javafx.fxml;
    opens ch.ludovic_mermod.dfasimulator.constants to javafx.fxml;

    exports ch.ludovic_mermod.dfasimulator.utils;
    exports ch.ludovic_mermod.dfasimulator.gui.components;
    exports ch.ludovic_mermod.dfasimulator.constants;
    exports ch.ludovic_mermod.dfasimulator.gui.pane_manager;
}