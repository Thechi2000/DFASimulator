package ch.ludovic_mermod.dfasimulator.gui.components;

import ch.ludovic_mermod.dfasimulator.constants.Constants;
import ch.ludovic_mermod.dfasimulator.constants.Strings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SettingGroup extends VBox
{
    public SettingGroup(String id)
    {
        var childrenKeys = Constants.keySet().stream().filter(k -> k.startsWith(id)).toList();
        var path = id.equals("") ? List.of() : Arrays.asList(id.split("\\."));

        VBox childrenBox = new VBox();

        if (!path.isEmpty())
        {
            HBox hBox = new HBox();

            Text text = new Text();
            Strings.bind("settings." + path.get(path.size() - 1), text.textProperty());
            hBox.getChildren().add(text);

            ToggleButton toggleButton = new ToggleButton();
            toggleButton.setSelected(true);
            toggleButton.selectedProperty().addListener((o, ov, nv) -> {
                if(nv && getChildren().size() == 1)
                    getChildren().add(childrenBox);
                else if(!nv)
                    getChildren().remove(childrenBox);
            });
            hBox.getChildren().add(toggleButton);
            getChildren().add(hBox);

            childrenBox.setPadding(new Insets(0, 0, 0, 20));
        }

        Map<String, SettingGroup> childrenGroups = new TreeMap<>();
        Map<String, Setting> childrenSettings = new TreeMap<>();

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

        getChildren().addAll(childrenBox);
    }
}
