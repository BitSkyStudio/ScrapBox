package com.github.industrialcraft.scrapbox.common.editui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.industrialcraft.scrapbox.client.ClientGameObjectEditor;
import com.github.industrialcraft.scrapbox.client.InGameScene;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EditorUILabel extends EditorUIElement{
    public final String text;
    public EditorUILabel(String text) {
        this.text = text;
    }
    public EditorUILabel(DataInputStream stream) throws IOException {
        this.text = stream.readUTF();
    }
    @Override
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeUTF(this.text);
    }
    @Override
    public Actor createActor(Skin skin, ClientGameObjectEditor editor) {
        return new Label(text, skin);
    }
    @Override
    public int getId() {
        return 1;
    }
}
