package ch.ludovic_mermod.dfasimulator.gui.pane_manager;

import ch.ludovic_mermod.dfasimulator.json.JSONObject;
import ch.ludovic_mermod.dfasimulator.utils.CustomBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class Sentinel extends Element
{
    private final ObjectProperty<Element> element;

    private final AnchorPane anchorPane;
    private final boolean    isMain;

    public Sentinel(Element e)
    {
        this(e, false);
    }
    public Sentinel(Element e, boolean isMain)
    {
        element = new SimpleObjectProperty<>();
        this.isMain = isMain;
        anchorPane = new AnchorPane();

        element.addListener((o, ov, nv) -> {
            anchorPane.getChildren().clear();

            if (nv != null)
            {
                nv.removeFromParent();

                Node content = nv.getContent();
                anchorPane.getChildren().add(content);

                AnchorPane.setBottomAnchor(content, 0.0);
                AnchorPane.setLeftAnchor(content, 0.0);
                AnchorPane.setTopAnchor(content, 0.0);
                AnchorPane.setRightAnchor(content, 0.0);
            }
        });
        element.set(e);
        element.get().parent = this;
    }

    public boolean isEmpty() {return element.get() == null;}
    public boolean isMain()
    {
        return isMain;
    }

    @Override
    protected void update(Element oldValue, Element newValue)
    {
        if (element.get() == oldValue) element.set(newValue);
        else throw new IllegalArgumentException();
    }

    @Override
    protected void removeChild(Element child)
    {
    }

    @Override
    public void add(Item i, double x, double y)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Element remove(Item i)
    {
        element.set(element.get().remove(i));
        return this;
    }

    @Override
    public Parent getContent()
    {
        return anchorPane;
    }
    /*public ObservableValue<Parent> getContentBinding()
    {
        return CustomBindings.create(this::getContent, element);
    }*/

    @Override
    public JSONObject getJSONObject()
    {
        var obj = element.get() == null ? new JSONObject() : element.get().getJSONObject();
        if (element != null) obj.addProperty(PaneManager.JSON_IS_MAIN, isMain);
        return obj;
    }
}
