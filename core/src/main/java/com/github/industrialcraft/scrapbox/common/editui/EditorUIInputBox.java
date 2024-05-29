package com.github.industrialcraft.scrapbox.common.editui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.github.industrialcraft.scrapbox.client.ClientGameObjectEditor;
import com.github.industrialcraft.scrapbox.common.net.msg.EditorUIInput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EditorUIInputBox extends EditorUIElement{
    public final String id;
    public final String content;
    public EditorUIInputBox(String id, String content) {
        this.id = id;
        this.content = content;
    }
    public EditorUIInputBox(DataInputStream stream) throws IOException {
        this.id = stream.readUTF();
        this.content = stream.readUTF();
    }
    @Override
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeUTF(id);
        stream.writeUTF(content);
    }
    @Override
    public Actor createActor(Skin skin, ClientGameObjectEditor editor) {
        TextField inputBox = new TextField(content, skin);
        inputBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //todo: only on enter
                editor.scene.connection.send(new EditorUIInput(editor.gameObjectID, id, inputBox.getText()));
            }
        });
        return inputBox;
    }
    @Override
    public int getText() {
        return 4;
    }
}
