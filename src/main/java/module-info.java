module ch.thechi2000.dfasimulator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.json;

    exports ch.ludovic_mermod.dfasimulator;
    exports ch.ludovic_mermod.dfasimulator.gui;
    opens ch.ludovic_mermod.dfasimulator.gui to javafx.fxml;
    exports ch.ludovic_mermod.dfasimulator.gui.scene;
    opens ch.ludovic_mermod.dfasimulator.gui.scene to javafx.fxml;
}