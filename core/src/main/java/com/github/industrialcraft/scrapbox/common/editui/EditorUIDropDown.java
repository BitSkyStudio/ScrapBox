package com.github.industrialcraft.scrapbox.common.editui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.github.industrialcraft.scrapbox.client.ClientGameObjectEditor;
import com.github.industrialcraft.scrapbox.common.net.msg.EditorUIInput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class EditorUIDropDown extends EditorUIElement{
    public final String id;
    public final ArrayList<String> content;
    public final int selected;
    public EditorUIDropDown(String id, ArrayList<String> content, int selected) {
        this.id = id;
        this.content = content;
        this.selected = selected;
    }
    public EditorUIDropDown(DataInputStream stream) throws IOException {
        this.id = stream.readUTF();
        int count = stream.readInt();
        this.content = new ArrayList<>(count);
        for(int i = 0;i < count;i++)
            content.add(stream.readUTF());
        this.selected = stream.readInt();
    }
    @Override
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeUTF(id);
        stream.writeInt(content.size());
        for(String element : content){
            stream.writeUTF(element);
        }
        stream.writeInt(selected);
    }
    @Override
    public Actor createActor(Skin skin, ClientGameObjectEditor editor) {
        SelectBox<String> dropdown = new SelectBox<>(skin);
        dropdown.setItems(this.content.toArray(String[]::new));
        dropdown.setSelectedIndex(selected);
        dropdown.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                editor.scene.connection.send(new EditorUIInput(editor.gameObjectID, id, ""+dropdown.getSelectedIndex()));
            }
        });
        return dropdown;
    }
    @Override
    public int getText() {
        return 3;
    }
}
