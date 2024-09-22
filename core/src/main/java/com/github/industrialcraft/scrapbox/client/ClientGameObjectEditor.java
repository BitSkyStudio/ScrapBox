package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.github.industrialcraft.scrapbox.common.net.msg.CloseGameObjectEditUI;
import com.github.industrialcraft.scrapbox.common.net.msg.SetGameObjectEditUIData;

public class ClientGameObjectEditor {
    public final int gameObjectID;
    public final Texture linkInput;
    public final Texture linkInputFilled;
    public final Texture linkOutput;
    public final InGameScene scene;
    public final Window window;
    public ClientGameObjectEditor(int gameObjectID, InGameScene scene, SetGameObjectEditUIData data) {
        this.gameObjectID = gameObjectID;
        this.linkInput = new Texture("link_input.png");
        this.linkInputFilled = new Texture("link_input_filled.png");
        this.linkOutput = new Texture("link_output.png");
        this.scene = scene;
        Skin skin = ScrapBox.getInstance().getSkin();
        this.window = new Window("Edit Object", skin){
            @Override
            public void keepWithinStage () {
                Stage stage = getStage();
                if (stage == null) return;
                Camera camera = stage.getCamera();
                if (camera instanceof OrthographicCamera) {
                    OrthographicCamera orthographicCamera = (OrthographicCamera)camera;
                    float parentWidth = stage.getWidth();
                    float parentHeight = stage.getHeight();
                    if (getX(Align.right) - camera.position.x + scene.toolBox.getWidth() > parentWidth / 2 / orthographicCamera.zoom)
                        setPosition(camera.position.x - scene.toolBox.getWidth() + parentWidth / 2 / orthographicCamera.zoom, getY(Align.right), Align.right);
                    if (getX(Align.left) - camera.position.x < -parentWidth / 2 / orthographicCamera.zoom)
                        setPosition(camera.position.x - parentWidth / 2 / orthographicCamera.zoom, getY(Align.left), Align.left);
                    if (getY(Align.top) - camera.position.y > parentHeight / 2 / orthographicCamera.zoom)
                        setPosition(getX(Align.top), camera.position.y + parentHeight / 2 / orthographicCamera.zoom, Align.top);
                    if (getY(Align.bottom) - camera.position.y < -parentHeight / 2 / orthographicCamera.zoom)
                        setPosition(getX(Align.bottom), camera.position.y - parentHeight / 2 / orthographicCamera.zoom, Align.bottom);
                }/* else if (getParent() == stage.getRoot()) {
                    float parentWidth = stage.getWidth()-scene.toolBox.getWidth();
                    float parentHeight = stage.getHeight();
                    if (getX() < 0) setX(0);
                    if (getRight() > parentWidth) setX(parentWidth - getWidth());
                    if (getY() < 0) setY(0);
                    if (getTop() > parentHeight) setY(parentHeight - getHeight());
                }*/
            }
        };
        window.setPosition(Gdx.input.getX(), Gdx.graphics.getHeight()-Gdx.input.getY());
        rebuild(data);
    }
    public void rebuild(SetGameObjectEditUIData data){
        this.window.clearChildren();
        Skin skin = ScrapBox.getInstance().getSkin();
        for(int i = 0;i < data.rows.size();i++){
            data.rows.get(i).elements.forEach(editorUIElement -> window.add(editorUIElement.createActor(skin, this)));
            window.row();
        }
        TextButton close = new TextButton("close", skin);
        close.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                scene.connection.send(new CloseGameObjectEditUI(ClientGameObjectEditor.this.gameObjectID));
                scene.closeEditor(ClientGameObjectEditor.this);
            }
        });
        this.window.add(close);
        window.pack();
    }
    public void dispose(){
        this.linkInput.dispose();
        this.linkOutput.dispose();
    }
}
