package com.github.industrialcraft.scrapbox.common.editui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.industrialcraft.scrapbox.client.ClientGameObjectEditor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class EditorUIElement {
    public abstract void toStream(DataOutputStream stream) throws IOException;
    public abstract Actor createActor(Skin skin, ClientGameObjectEditor editor);
    public abstract int getType();

    public static EditorUIElement load(DataInputStream stream) throws IOException {
        int id = stream.readInt();
        if(id == 1){
            return new EditorUILabel(stream);
        }
        if(id == 2){
            return new EditorUILink(stream);
        }
        if(id == 3){
            return new EditorUIDropDown(stream);
        }
        if(id == 4){
            return new EditorUIInputBox(stream);
        }
        if(id == 5){
            return new EditorUIButton(stream);
        }
        if(id == 6){
            return new EditorUIInventory(stream);
        }
        return null;
    }
}
