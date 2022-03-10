module ch.thechi2000.dfasimulator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires com.google.gson;

    exports ch.ludovic_mermod.dfasimulator;
    exports ch.ludovic_mermod.dfasimulator.logic;
    exports ch.ludovic_mermod.dfasimulator.gui.scene;

    opens ch.ludovic_mermod.dfasimulator.gui to javafx.fxml;
    opens ch.ludovic_mermod.dfasimulator.gui.scene to javafx.fxml;
}