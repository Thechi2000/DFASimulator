package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Strings;
import ch.ludovic_mermod.dfasimulator.constants.settings.Settings;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represent a group of Setting and SettingGroup
 */
public class SettingGroup extends VBox
{
    private final Map<String, SettingGroup> childrenGroups;
    private final Map<String, Setting>      childrenSettings;

    /**
     * Constructs a SettingGroup for the given id
     *
     * @param id root of the settings of the group
     */
    public SettingGroup(String id)
    {
        var childrenKeys = Settings.keySet().stream().filter(k -> k.startsWith(id)).toList();
        var path = id.equals("") ? List.of() : Arrays.asList(id.split("\\."));

        VBox childrenBox = new VBox();

        if (!path.isEmpty())
        {
            HBox hBox = new HBox();

            Text text = new Text();
            Strings.bind("settings." + path.get(path.size() - 1), text.textProperty());
            hBox.getChildren().add(text);

            ToggleButton toggleButton = new ToggleButton();
            toggleButton.textProperty().bind(CustomBindings.ternary(toggleButton.selectedProperty(), "-", "+"));
            toggleButton.selectedProperty().addListener((o, ov, nv) -> {
                if (nv && getChildren().size() == 1)
                    getChildren().add(childrenBox);
                else if (!nv)
                    getChildren().remove(childrenBox);
            });
            toggleButton.setSelected(false);
            hBox.getChildren().add(toggleButton);
            getChildren().add(hBox);

            childrenBox.setPadding(new Insets(0, 0, 0, 20));
        }
        else
            getChildren().addAll(childrenBox);

        childrenGroups = new TreeMap<>();
        childrenSettings = new TreeMap<>();

        childrenKeys.forEach(k -> {
            var keyPath = Arrays.asList(k.split("\\."));

            if (keyPath.size() == path.size() + 1 && !childrenSettings.containsKey(k))
                childrenSettings.put(k, new Setting(k));
            else
            {
                String key = String.join(".", keyPath.subList(0, path.size() + 1));
                if (!childrenGroups.containsKey(key))
                    childrenGroups.put(key, new SettingGroup(key));
            }
        });

        childrenGroups.values().forEach(childrenBox.getChildren()::add);
        childrenSettings.values().forEach(childrenBox.getChildren()::add);
    }

    /**
     * Save recursively the current values of all child Setting to Settings
     */
    public void saveChanges()
    {
        childrenGroups.values().forEach(SettingGroup::saveChanges);
        childrenSettings.values().forEach(Setting::saveChanges);
    }
}
