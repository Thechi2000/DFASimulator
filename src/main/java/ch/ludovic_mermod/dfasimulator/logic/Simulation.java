package ch.ludovic_mermod.dfasimulator.logic;

import ch.ludovic_mermod.dfasimulator.gui.scene.MainPane;
import javafx.beans.property.*;

import java.util.Timer;
import java.util.TimerTask;

public class Simulation
{
    private final FiniteAutomaton finiteAutomaton;

    private final ObjectProperty<State> currentStateProperty;
    private final StringProperty remainingInputProperty;
    private final BooleanProperty isSimulatingProperty;

    private final StringProperty initialInputProperty;
    private final BooleanProperty resultProperty;
    private final BooleanProperty simulationEndedProperty;

    public Simulation(MainPane mainPane)
    {
        finiteAutomaton = mainPane.getFiniteAutomaton();

        currentStateProperty = new SimpleObjectProperty<>();
        remainingInputProperty = new SimpleStringProperty();
        isSimulatingProperty = new SimpleBooleanProperty(false);

        initialInputProperty = new SimpleStringProperty("");
        resultProperty = new SimpleBooleanProperty(false);
        simulationEndedProperty = new SimpleBooleanProperty(false);

        simulationEndedProperty.addListener((o, ov, nv) -> updateResult());
        currentStateProperty.addListener((o, ov, nv) -> updateResult());
    }

    private void updateResult()
    {
        if (currentStateProperty.get() != null)
            resultProperty.set(currentStateProperty.get().isAcceptingProperty().get() && simulationEndedProperty.get());
    }

    public ReadOnlyObjectProperty<State> currentStateProperty()
    {
        return currentStateProperty;
    }
    public ReadOnlyStringProperty remainingInputProperty()
    {
        return remainingInputProperty;
    }
    public ReadOnlyBooleanProperty isSimulatingProperty()
    {
        return isSimulatingProperty;
    }

    public ReadOnlyStringProperty initialInputProperty()
    {
        return initialInputProperty;
    }
    public ReadOnlyBooleanProperty resultProperty()
    {
        return resultProperty;
    }
    public ReadOnlyBooleanProperty simulationEndedProperty()
    {
        return simulationEndedProperty;
    }


    public void startSimulation(String input)
    {
        isSimulatingProperty.set(true);
        simulationEndedProperty.set(false);
        currentStateProperty.set(finiteAutomaton.initialState());
        remainingInputProperty.set(input);
        initialInputProperty.set(input);
    }

    public void nextSimulationStep()
    {
        if (remainingInputProperty.get().length() == 0)
        {
            currentStateProperty.set(null);
            isSimulatingProperty.set(false);
            return;
        }

        String input = remainingInputProperty.get();
        char c = input.charAt(0);
        remainingInputProperty.set(input.substring(1));

        currentStateProperty.set(currentStateProperty.get().transitionMap().get(c));

        if (remainingInputProperty.get().length() == 0)
            simulationEndedProperty.set(true);
    }

    public void finish()
    {
        if (!isSimulatingProperty.get()) return;

        nextSimulationStep();
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                finish();
            }
        }, 1000);
    }

    public enum ErrorCode
    {
        TOO_MANY_INITIAL_STATES,
        NO_INITIAL_STATE,
        NODE_DOES_NOT_MATCH_ALPHABET,
        STRING_DOES_NOT_MATCH_ALPHABET
    }

    public record Error(ErrorCode code, Object[] data)
    {
    }
}
