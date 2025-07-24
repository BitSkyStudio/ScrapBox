package com.github.industrialcraft.scrapbox.common.editui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.industrialcraft.scrapbox.client.ClientGameObjectEditor;
import com.github.industrialcraft.scrapbox.client.ToolBox;
import com.github.industrialcraft.scrapbox.common.net.msg.EditorUIInput;
import com.github.industrialcraft.scrapbox.server.EItemType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class EditorUIInventory extends EditorUIElement{
    public final EnumMap<EItemType,Float> inventory;
    public EditorUIInventory(EnumMap<EItemType,Float> inventory) {
        this.inventory = inventory;
    }
    public EditorUIInventory(DataInputStream stream) throws IOException {
        this.inventory = new EnumMap<>(EItemType.class);
        int size = stream.readInt();
        for(int i = 0;i < size;i++){
            this.inventory.put(EItemType.byId(stream.readByte()), stream.readFloat());
        }
    }
    @Override
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(inventory.size());
        for(Map.Entry<EItemType, Float> entry : inventory.entrySet()){
            stream.writeByte(entry.getKey().id);
            stream.writeFloat(entry.getValue());
        }
    }
    @Override
    public Actor createActor(Skin skin, ClientGameObjectEditor editor) {
        Table table = new Table();
        for(EItemType itemType : EItemType.values()){
            TextButton less = new TextButton("<", skin);
            Image image = new Image(editor.scene.itemTextures.get(itemType)){
                @Override
                public float getMinWidth() {
                    return 32;
                }
                @Override
                public float getMinHeight() {
                    return 32;
                }
            };
            Label label = new Label(ToolBox.formatFloat(inventory.getOrDefault(itemType, 0f)), skin);
            TextButton more = new TextButton(">", skin);
            Actor linkActor = new EditorUILink(itemType.id, false, 0f, false, false).createActor(skin, editor);
            table.add(less, image, label, more, linkActor).row();
            less.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    editor.scene.connection.send(new EditorUIInput(editor.gameObjectID, "inventory_sub", itemType.id+""));
                }
            });
            more.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    editor.scene.connection.send(new EditorUIInput(editor.gameObjectID, "inventory_add", itemType.id+""));
                }
            });
        }
        return table;
    }
    @Override
    public int getType() {
        return 6;
    }
}
