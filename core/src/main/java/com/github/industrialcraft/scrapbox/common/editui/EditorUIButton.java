package com.github.industrialcraft.scrapbox.common.editui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.industrialcraft.scrapbox.client.ClientGameObjectEditor;
import com.github.industrialcraft.scrapbox.common.net.msg.EditorUIInput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EditorUIButton extends EditorUIElement{
    public final String text;
    public final String action;
    public EditorUIButton(String text, String action) {
        this.text = text;
        this.action = action;
    }
    public EditorUIButton(DataInputStream stream) throws IOException {
        this.text = stream.readUTF();
        this.action = stream.readUTF();
    }
    @Override
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.text);
        stream.writeUTF(this.action);
    }
    @Override
    public Actor createActor(Skin skin, ClientGameObjectEditor editor) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                editor.scene.connection.send(new EditorUIInput(editor.gameObjectID, action, ""));
            }
        });
        return button;
    }
    @Override
    public int getType() {
        return 5;
    }
}
