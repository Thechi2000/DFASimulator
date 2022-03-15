package ch.ludovic_mermod.dfasimulator.gui.scene.components;

import ch.ludovic_mermod.dfasimulator.Utils;
import ch.ludovic_mermod.dfasimulator.gui.Constants;
import ch.ludovic_mermod.dfasimulator.gui.scene.GraphPane;
import ch.ludovic_mermod.dfasimulator.logic.State;
import javafx.beans.Observable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.stream.Collectors;

public class Edge extends Group
{
    private final State source;
    private final State target;

    private final Line line, leftLine, rightLine;
    private final Text alphabetDisplay;

    public Edge(State source, State target, GraphPane graphPane)
    {
        this.source = source;
        this.target = target;

        alphabetDisplay = new Text();
        updateAlphabetDisplay();
        source.transitionMap().addListener((p, k, o, n) -> updateAlphabetDisplay());

        line = new Line();
        line.fillProperty().bind(Constants.Link.Line.color);
        line.strokeWidthProperty().bind(Constants.Link.Line.width);
        line.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        leftLine = new Line();
        leftLine.fillProperty().bind(Constants.Link.Line.color);
        leftLine.strokeWidthProperty().bind(Constants.Link.Line.width);
        leftLine.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        rightLine = new Line();
        rightLine.fillProperty().bind(Constants.Link.Line.color);
        rightLine.strokeWidthProperty().bind(Constants.Link.Line.width);
        rightLine.visibleProperty().bind(alphabetDisplay.textProperty().isEqualTo("").not());

        bindPositions();

        getChildren().addAll(line, leftLine, rightLine, alphabetDisplay);
    }

    public State source()
    {
        return source;
    }
    public State target()
    {
        return target;
    }
    public String getSourceName()
    {
        return source.name();
    }
    public String getTargetName()
    {
        return target.name();
    }

    private void updateAlphabetDisplay()
    {
        alphabetDisplay.setText(source.transitionMap()
                .entrySet()
                .stream()
                .filter(e -> target.equals(e.getValue().get()))
                .map(e -> e.getKey().toString())
                .sorted()
                .collect(Collectors.joining(", ")));
    }

    @Override
    public String toString()
    {
        return "Edge{" +
                "source=" + source.name() +
                ", target=" + target.name() +
                '}';
    }


    private void bindPositions()
    {
        final Node sn = source.getNode();
        final Node tn = target.getNode();

        Observable[] observables = new Observable[]{
                sn.layoutXProperty(), sn.layoutYProperty(), sn.widthProperty(), sn.heightProperty(),
                tn.layoutXProperty(), tn.layoutYProperty(), tn.widthProperty(), tn.heightProperty(),
                Constants.Node.Circle.radius,
                Constants.Link.Line.sidelineLength,
                Constants.Link.Text.distanceFromNodeFactor,
                Constants.Link.Text.distanceFromNodeAbsolute,
                Constants.Link.Text.usesAbsoluteDistance
        };

        Utils.bindDouble(line.startXProperty(), () -> computePoints().start.getX(), observables);
        Utils.bindDouble(line.startYProperty(), () -> computePoints().start.getY(), observables);
        Utils.bindDouble(line.endXProperty(), () -> computePoints().end.getX(), observables);
        Utils.bindDouble(line.endYProperty(), () -> computePoints().end.getY(), observables);

        Utils.bindDouble(leftLine.startXProperty(), () -> computePoints().leftLineStart.getX(), observables);
        Utils.bindDouble(leftLine.startYProperty(), () -> computePoints().leftLineStart.getY(), observables);
        Utils.bindDouble(leftLine.endXProperty(), () -> computePoints().end.getX(), observables);
        Utils.bindDouble(leftLine.endYProperty(), () -> computePoints().end.getY(), observables);


        Utils.bindDouble(rightLine.startXProperty(), () -> computePoints().rightLineStart.getX(), observables);
        Utils.bindDouble(rightLine.startYProperty(), () -> computePoints().rightLineStart.getY(), observables);
        Utils.bindDouble(rightLine.endXProperty(), () -> computePoints().end.getX(), observables);
        Utils.bindDouble(rightLine.endYProperty(), () -> computePoints().end.getY(), observables);

        Utils.bindDouble(alphabetDisplay.xProperty(), () -> computePoints().textPos.getX(), observables);
        Utils.bindDouble(alphabetDisplay.yProperty(), () -> computePoints().textPos.getY(), observables);
    }

    private Points computePoints()
    {
        final Node sn = source.getNode();
        final Node tn = target.getNode();

        Point2D startCenter = new Point2D(sn.getLayoutX() + sn.getWidth() / 2, sn.getLayoutY() + sn.getHeight() / 2),
                endCenter = new Point2D(tn.getLayoutX() + tn.getWidth() / 2, tn.getLayoutY() + tn.getHeight() / 2),
                director = endCenter.subtract(startCenter).normalize(),
                normal = new Point2D(director.getY(), -director.getX()),
                start = startCenter.add(director.multiply(Constants.Node.Circle.radius.get())),
                end = endCenter.subtract(director.multiply(Constants.Node.Circle.radius.get())),
                projectionPoint = end.subtract(director.multiply(Constants.Link.Line.sidelineLength.get())),
                projectionDistance = normal.multiply(Constants.Link.Line.sidelineLength.get()),
                leftStart = projectionPoint.add(projectionDistance),
                rightStart = projectionPoint.subtract(projectionDistance),
                projectionRelative = start.add(end.subtract(start).multiply(Constants.Link.Text.distanceFromNodeFactor.get())),
                projectionAbsolute = start.add(director.multiply(Constants.Link.Text.distanceFromNodeAbsolute.get())),
                textPos = (Constants.Link.Text.usesAbsoluteDistance.get() && (projectionRelative.subtract(start).magnitude() > Constants.Link.Text.distanceFromNodeAbsolute.get()) ? projectionAbsolute : projectionRelative).add(normal.multiply(Constants.Link.Text.distanceFromLine.get()));

        return new Points(start, end, leftStart, rightStart, textPos);
    }

    private record Points(Point2D start, Point2D end, Point2D leftLineStart, Point2D rightLineStart, Point2D textPos) {}
}
