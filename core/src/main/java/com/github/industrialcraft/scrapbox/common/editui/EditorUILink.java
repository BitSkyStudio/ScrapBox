package com.github.industrialcraft.scrapbox.common.editui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.industrialcraft.scrapbox.client.ClientGameObjectEditor;
import com.github.industrialcraft.scrapbox.common.net.msg.CreateValueConnection;
import com.github.industrialcraft.scrapbox.common.net.msg.DestroyValueConnection;
import com.github.industrialcraft.scrapbox.common.net.msg.EditorUIInput;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EditorUILink extends EditorUIElement{
    public final int id;
    public final boolean input;
    public final float defaultValue;
    public final boolean filled;
    public EditorUILink(int id, boolean input, float defaultValue, boolean filled) {
        this.id = id;
        this.input = input;
        this.defaultValue = defaultValue;
        this.filled = filled;
    }
    public EditorUILink(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.input = stream.readBoolean();
        this.defaultValue = stream.readFloat();
        this.filled = stream.readBoolean();
    }
    @Override
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeBoolean(input);
        stream.writeFloat(defaultValue);
        stream.writeBoolean(filled);
    }
    @Override
    public Actor createActor(Skin skin, ClientGameObjectEditor editor) {
        Image image = new Image(input?(filled?editor.linkInputFilled:editor.linkInput):editor.linkOutput);
        if(input) {
            image.addListener(new ClickListener(Input.Buttons.LEFT){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    TextField input = new TextField(defaultValue+"", skin);
                    Dialog dialog = new Dialog("Enter default value", skin, "dialog") {
                        public void result(Object obj) {
                            if(obj instanceof String){
                                editor.scene.connection.send(new EditorUIInput(editor.gameObjectID, ""+id, input.getText()));
                            }
                        }
                    };
                    dialog.button("Cancel");
                    dialog.button("Ok", "");
                    dialog.getContentTable().add(input);
                    dialog.show(editor.scene.stage);
                }
            });
            image.addListener(new ClickListener(Input.Buttons.RIGHT){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    editor.scene.connection.send(new DestroyValueConnection(editor.gameObjectID, EditorUILink.this.id));
                }
            });
            editor.scene.dragAndDrop.addTarget(new DragAndDrop.Target(image) {
                @Override
                public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    return payload.getObject() instanceof ConnectionData;
                }
                @Override
                public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    ConnectionData output = (ConnectionData) payload.getObject();
                    editor.scene.connection.send(new CreateValueConnection(editor.gameObjectID, EditorUILink.this.id, output.gameObject, output.connectionId));
                }
            });
        } else {
            editor.scene.dragAndDrop.addSource(new DragAndDrop.Source(image) {
                @Override
                public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    image.setVisible(false);
                    DragAndDrop.Payload payload = new DragAndDrop.Payload();
                    Image connection = new Image(editor.linkOutput);
                    payload.setDragActor(connection);
                    payload.setObject(new ConnectionData(editor.gameObjectID, EditorUILink.this.id, image.localToStageCoordinates(new Vector2(image.getWidth()/2, image.getHeight()/2))));
                    editor.scene.dragAndDrop.setDragActorPosition(image.getWidth(), -image.getHeight()/2);
                    return payload;
                }

                @Override
                public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                    image.setVisible(true);
                    super.dragStop(event, x, y, pointer, payload, target);
                }
            });
        }
        return image;
    }
    public static class ConnectionData{
        public final int gameObject;
        public final int connectionId;
        public final Vector2 position;
        public ConnectionData(int gameObject, int connectionId, Vector2 position) {
            this.gameObject = gameObject;
            this.connectionId = connectionId;
            this.position = position;
        }
    }
    @Override
    public int getId() {
        return 2;
    }
}
