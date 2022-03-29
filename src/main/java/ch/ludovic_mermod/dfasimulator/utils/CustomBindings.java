package ch.ludovic_mermod.dfasimulator.utils;

import ch.ludovic_mermod.dfasimulator.Main;
import javafx.beans.Observable;
import javafx.beans.binding.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Static class to easily create bindings
 */
public class CustomBindings
{
    private CustomBindings() {}

    /**
     * Creates a ternary operator like binding
     *
     * @param bool    condition to choose between the two values
     * @param ifTrue  value of the binding if the condition is true
     * @param ifFalse value of the binding if the condition is false
     * @param <T>     type of the values
     * @return a ternary operator like binding
     */
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
    /**
     * Creates a ternary operator like binding
     *
     * @param bool    condition to choose between the two values
     * @param ifTrue  value of the binding if the condition is true
     * @param ifFalse value of the binding if the condition is false
     * @param <T>     type of the values
     * @return a ternary operator like binding
     */
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
    /**
     * Creates a ternary operator like binding
     *
     * @param bool    condition to choose between the two values
     * @param ifTrue  value of the binding if the condition is true
     * @param ifFalse value of the binding if the condition is false
     * @param <T>     type of the values
     * @return a ternary operator like binding
     */
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
    /**
     * Creates a ternary operator like binding
     *
     * @param bool    condition to choose between the two values
     * @param ifTrue  value of the binding if the condition is true
     * @param ifFalse value of the binding if the condition is false
     * @param <T>     type of the values
     * @return a ternary operator like binding
     */
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

    /**
     * Create a Binding.
     * When an observable changes, the value of the binding is updated to the return value of computeValue
     *
     * @param computeValue supplies the value of the binding
     * @param observables  observables on which the binding will depend
     * @param <T>          type of the binding
     * @return a Binding with the value returned by computeValue and depending on observables
     */
    public static <T> Binding<T> create(Supplier<T> computeValue, Observable... observables)
    {
        return new ObjectBinding<T>()
        {
            {
                bind(observables);
            }

            @Override
            protected T computeValue()
            {
                return computeValue.get();
            }
        };
    }

    /**
     * Create a DoubleBinding.
     * When an observable changes, the value of the binding is updated to the return value of computeValue
     *
     * @param computeValue supplies the value of the binding
     * @param observables  observables on which the binding will depend
     * @return a DoubleBinding with the value returned by computeValue and depending on observables
     */
    public static DoubleBinding createDouble(Supplier<Double> computeValue, Observable... observables)
    {
        return new DoubleBinding()
        {
            {
                bind(observables);
            }

            @Override
            protected double computeValue()
            {
                return computeValue.get();
            }
        };
    }
    /**
     * Create a StringBinding.
     * When an observable changes, the value of the binding is updated to the return value of computeValue
     *
     * @param computeValue supplies the value of the binding
     * @param observables  observables on which the binding will depend
     * @return a StringBinding with the value returned by computeValue and depending on observables
     */
    public static StringBinding createString(Supplier<String> computeValue, Observable... observables)
    {
        return new StringBinding()
        {
            {
                bind(observables);
            }

            @Override
            protected String computeValue()
            {
                return computeValue.get();
            }
        };
    }
    /**
     * Create a BooleanBinding.
     * When an observable changes, the value of the binding is updated to the return value of computeValue
     *
     * @param computeValue supplies the value of the binding
     * @param observables  observables on which the binding will depend
     * @return a BooleanBinding with the value returned by computeValue and depending on observables
     */
    public static BooleanBinding createBoolean(Supplier<Boolean> computeValue, Observable... observables)
    {
        return new BooleanBinding()
        {
            {super.bind(observables);}

            @Override
            protected boolean computeValue()
            {
                return computeValue.get();
            }
        };
    }

    /**
     * Creates and bind a binding to the property
     *
     * @param property     the property to bind the binding to
     * @param computeValue supplies the value of the binding
     * @param observables  observables on which the binding will depend
     */
    public static void bindDouble(DoubleProperty property, Supplier<Double> computeValue, Observable... observables)
    {
        property.bind(createDouble(computeValue, observables));
    }

    /**
     * Return a StringBinding with the value of format parameter formatted with the objects
     * If an object is a property, its value is used in the formatting and the return binding will depend on the property
     *
     * @param format  the format String
     * @param objects the objects used to format
     * @return a StringBinding with the value of format parameter formatted with the objects
     */
    public static StringBinding format(ObservableValue<String> format, Object... objects)
    {return format(format, true, objects);}
    /**
     * Return a StringBinding with the value of format parameter formatted with the objects
     * If an object is a property, its value is used in the formatting and the return binding will depend on the property
     *
     * @param format     the format String
     * @param checkCount whether the function must log a warning if the format String does not match the amount of objects
     * @param objects    the objects used to format
     * @return a StringBinding with the value of format parameter formatted with the objects
     */
    public static StringBinding format(ObservableValue<String> format, boolean checkCount, Object... objects)
    {
        int occ = (format.getValue().length() - format.getValue().replace("%s", "").length()) / 2;
        if (checkCount && objects.length != occ)
            Main.log(Level.WARNING, "Invalid parameters count in \"%s\", expected: %d actual: %d", format.getValue(), objects.length, occ);
        if (objects.length == 0) return CustomBindings.createString(format::getValue, format);

        Set<ObservableValue<?>> properties = Arrays.stream(objects)
                .filter(o -> o instanceof ObservableValue<?>)
                .map(o -> (ObservableValue<?>) o)
                .collect(Collectors.toSet());

        properties.add(format);

        return new StringBinding()
        {
            {
                bind(properties.toArray(new Observable[0]));
            }

            @Override
            protected String computeValue()
            {
                return String.format(format.getValue(), Arrays.stream(objects).map(o -> o instanceof ObservableValue<?> ? ((ObservableValue<?>) o).getValue() : o).toArray());
            }
        };
    }
}
