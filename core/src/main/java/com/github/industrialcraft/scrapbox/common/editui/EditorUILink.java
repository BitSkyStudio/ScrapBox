package com.github.industrialcraft.scrapbox.common.editui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.industrialcraft.scrapbox.client.ClientGameObjectEditor;
import com.github.industrialcraft.scrapbox.common.net.msg.CreateValueConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EditorUILink extends EditorUIElement{
    public final int id;
    public final boolean input;
    public EditorUILink(int id, boolean input) {
        this.id = id;
        this.input = input;
    }
    public EditorUILink(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.input = stream.readBoolean();
    }
    @Override
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeBoolean(input);
    }
    @Override
    public Actor createActor(Skin skin, ClientGameObjectEditor editor) {
        Image image = new Image(input?editor.linkInput:editor.linkOutput);
        if(input) {
            editor.scene.dragAndDrop.addTarget(new DragAndDrop.Target(image) {
                @Override
                public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    return payload.getObject() instanceof ConnectionData;
                }
                @Override
                public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    ConnectionData output = (ConnectionData) payload.getObject();
                    ConnectionData input = new ConnectionData(editor.gameObjectID, EditorUILink.this.id);
                    editor.scene.connection.send(new CreateValueConnection(input.gameObject, input.connectionId, output.gameObject, output.connectionId));
                }
            });
        } else {
            editor.scene.dragAndDrop.addSource(new DragAndDrop.Source(image) {
                @Override
                public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    DragAndDrop.Payload payload = new DragAndDrop.Payload();
                    Image connection = new Image(editor.linkOutput);
                    payload.setDragActor(connection);
                    payload.setObject(new ConnectionData(editor.gameObjectID, EditorUILink.this.id));
                    return payload;
                }
            });
        }
        return image;
    }
    public static class ConnectionData{
        public final int gameObject;
        public final int connectionId;
        public ConnectionData(int gameObject, int connectionId) {
            this.gameObject = gameObject;
            this.connectionId = connectionId;
        }
    }
    @Override
    public int getId() {
        return 2;
    }
}
