module ch.thechi2000.dfasimulator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;

    opens ch.thechi2000.dfasimulator to javafx.fxml;
    exports ch.thechi2000.dfasimulator;
}