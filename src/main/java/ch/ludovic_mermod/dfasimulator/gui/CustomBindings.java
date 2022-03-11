package ch.ludovic_mermod.dfasimulator.gui;

import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;

public class CustomBindings
{
    public static <T> Binding<T> ternary(ObservableBooleanValue bool, ObservableValue<T> ifTrue, ObservableValue<T> ifFalse)
    {
        return new ObjectBinding<T>()
        {
            {
                super.bind(bool, ifTrue, ifFalse);
            }

            @Override
            protected T computeValue()
            {
                return bool.get() ? ifTrue.getValue() : ifFalse.getValue();
            }
        };
    }
    public static <T> Binding<T> ternary(ObservableBooleanValue bool, T ifTrue, ObservableValue<T> ifFalse)
    {
        return new ObjectBinding<T>()
        {
            {
                super.bind(bool, ifFalse);
            }

            @Override
            protected T computeValue()
            {
                return bool.get() ? ifTrue : ifFalse.getValue();
            }
        };
    }
    public static <T> Binding<T> ternary(ObservableBooleanValue bool, ObservableValue<T> ifTrue, T ifFalse)
    {
        return new ObjectBinding<T>()
        {
            {
                super.bind(bool, ifTrue);
            }

            @Override
            protected T computeValue()
            {
                return bool.get() ? ifTrue.getValue() : ifFalse;
            }
        };
    }
    public static <T> Binding<T> ternary(ObservableBooleanValue bool, T ifTrue, T ifFalse)
    {
        return new ObjectBinding<T>()
        {
            {
                super.bind(bool);
            }

            @Override
            protected T computeValue()
            {
                return bool.get() ? ifTrue : ifFalse;
            }
        };
    }
}
