package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import ch.ludovic_mermod.dfasimulator.utils.Utils;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.util.StringConverter;

import java.util.List;
import java.util.stream.Collectors;

public class CheckComboBox<T> extends Group
{
    private final ObjectProperty<StringConverter<T>> converter;

    private final ListProperty<T>   items;
    private final ObservableList<T> selectedItems;

    private final MenuButton menu;

    public CheckComboBox()
    {
        this(FXCollections.observableArrayList());
    }

    public CheckComboBox(ObservableList<T> items)
    {
        this.items = new SimpleListProperty<>();
        selectedItems = FXCollections.observableArrayList();

        converter = new SimpleObjectProperty<>(Utils.stringConverter(Object::toString, str -> {throw new UnsupportedOperationException();}, ""));

        menu = new MenuButton();
        menu.textProperty().bind(CustomBindings.create(() -> selectedItems.stream().map(converter.get()::toString).collect(Collectors.joining(", ")), selectedItems, converter));
        getChildren().add(menu);

        this.items.addListener((ListChangeListener<? super T>) change -> {
            change.next();
            change.getAddedSubList().forEach(t -> {
                CheckMenuItem item = new CheckMenuItem();
                item.setUserData(t);
                item.textProperty().bind(CustomBindings.create(() -> converter.get().toString(t), converter));
                item.selectedProperty().addListener((o, ov, nv) -> {
                    if (nv) selectedItems.add(t);
                    else selectedItems.remove(t);
                });
                menu.getItems().add(item);
            });

            change.getRemoved().forEach(t -> menu.getItems().removeIf(i -> i.getUserData().equals(t)));
        });
        this.items.set(items);
    }

    public ObservableList<T> getSelectedItems()
    {
        return selectedItems;
    }
    public ObservableList<T> getItems()
    {
        return items.get();
    }
    public ListProperty<T> itemsProperty()
    {
        return items;
    }
    public StringConverter<T> getConverter()
    {
        return converter.get();
    }
    public ObjectProperty<StringConverter<T>> converterProperty()
    {
        return converter;
    }

    public void setSelectedItems(List<T> items)
    {
        menu.getItems().stream().map(i -> (CheckMenuItem) i).filter(i -> items.contains(i.getUserData())).forEach(i -> i.setSelected(true));
    }
    public void setConverter(StringConverter<T> converter)
    {
        this.converter.set(converter);
    }
    public void setItems(ObservableList<T> items)
    {
        this.items.set(items);
    }
}
