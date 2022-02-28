package ch.thechi2000.dfasimulator;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DragAndDropExample extends Application
{


    private static class Position {
        double x;
        double y;
    }

    private void draggable(Node node) {
        final Position pos = new Position();

        //Prompt the user that the node can be clicked
        node.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> node.setCursor(Cursor.HAND));
        node.addEventHandler(MouseEvent.MOUSE_EXITED, event -> node.setCursor(Cursor.DEFAULT));

        //Prompt the user that the node can be dragged
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            node.setCursor(Cursor.MOVE);

            //When a press event occurs, the location coordinates of the event are cached
            pos.x = event.getX();
            pos.y = event.getY();
        });
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> node.setCursor(Cursor.DEFAULT));

        //Realize drag and drop function
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double distanceX = event.getX() - pos.x;
            double distanceY = event.getY() - pos.y;

            double x = node.getLayoutX() + distanceX;
            double y = node.getLayoutY() + distanceY;

            //After calculating X and y, relocate the node to the specified coordinate point (x, y)
            node.relocate(x, y);
        });
    }

    private Pane generateCircleNode(String data) {
        Pane node = new StackPane();

        Circle circle = new Circle(20);
        circle.setStyle("-fx-fill: rgb(51,184,223)");

        Text text = new Text(data);
        text.setStyle("-fx-fill: rgb(93,93,93);-fx-font-weight: bold;");

        node.getChildren().addAll(circle, text);

        return node;
    }

    @Override
    public void start(Stage primaryStage)
    {
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        Pane node1 = generateCircleNode("1");
        Pane node2 = generateCircleNode("2");

        //Relocate the node to any position
        node1.relocate(50, 50);
        node2.relocate(300, 150);

        //Make nodes draggable
        draggable(node1);
        draggable(node2);

        //Create line
        Line line = new Line();

        //Bind the starting point coordinate of the line with the center coordinate of node1
        line.startXProperty().bind(node1.layoutXProperty().add(node1.widthProperty().divide(2)));
        line.startYProperty().bind(node1.layoutYProperty().add(node1.heightProperty().divide(2)));

        //Bind the end coordinates of the line with the center coordinates of node2
        line.endXProperty().bind(node2.layoutXProperty().add(node2.widthProperty().divide(2)));
        line.endYProperty().bind(node2.layoutYProperty().add(node2.heightProperty().divide(2)));

        root.getChildren().addAll(line, node1, node2);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}