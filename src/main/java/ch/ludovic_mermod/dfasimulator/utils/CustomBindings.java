package ch.ludovic_mermod.dfasimulator.utils;

import ch.ludovic_mermod.dfasimulator.Main;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

    public static <T> Binding<T> binding(Supplier<T> computeValue, Observable... observables)
    {
        return new ObjectBinding<T>() {
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

    public static DoubleBinding doubleBinding(Supplier<Double> computeValue, Observable... observables)
    {
        return new DoubleBinding()
        {
            {
                super.bind(observables);
            }

            @Override
            protected double computeValue()
            {
                return computeValue.get();
            }
        };
    }
    public static void bindDouble(DoubleProperty property, Supplier<Double> computeValue, Observable... observables)
    {
        property.bind(doubleBinding(computeValue, observables));
    }

    public static StringBinding format(ObservableValue<String> format, Object... objects)
    {
        int occ = (format.getValue().length() - format.getValue().replace("%s", "").length()) / 2;
        if (objects.length != occ)
            Main.log(Level.WARNING, "Invalid parameters count in \"%s\", expected: %d actual: %d", format.getValue(), objects.length, occ);

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

    public static <K, V> Binding<V> map(ObjectProperty<K> key, Map<K, V> map)
    {
        return new ObjectBinding<V>()
        {
            {
                bind(key);
            }

            @Override
            protected V computeValue()
            {
                return map.get(key.get());
            }
        };
    }
}