module ch.ludovic_mermod.dfasimulator {
    requires javafx.controls;
    requires com.google.gson;

    exports ch.ludovic_mermod.dfasimulator;
    exports ch.ludovic_mermod.dfasimulator.json;
    exports ch.ludovic_mermod.dfasimulator.logic;
    exports ch.ludovic_mermod.dfasimulator.gui.scene;

    opens ch.ludovic_mermod.dfasimulator.gui to javafx.fxml;
    opens ch.ludovic_mermod.dfasimulator.gui.scene to javafx.fxml;
    exports ch.ludovic_mermod.dfasimulator.gui.scene.components;
    opens ch.ludovic_mermod.dfasimulator.gui.scene.components to javafx.fxml;
    opens ch.ludovic_mermod.dfasimulator.utils to javafx.fxml;
    exports ch.ludovic_mermod.dfasimulator.utils;
}