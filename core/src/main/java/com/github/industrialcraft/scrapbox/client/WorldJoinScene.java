package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.github.industrialcraft.scrapbox.common.net.IConnection;
import com.github.industrialcraft.scrapbox.common.net.msg.EditorUIInput;
import com.github.industrialcraft.scrapbox.server.SaveFile;
import com.github.industrialcraft.scrapbox.server.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class WorldJoinScene extends StageBasedScreen {
    @Override
    public void create() {
        super.create();
        Skin skin = ScrapBox.getInstance().getSkin();
        List<String> list = new List<>(skin);
        ScrollPane scrollPane = new ScrollPane(list, skin);
        scrollPane.addListener(new InputListener() {
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                stage.setScrollFocus(scrollPane);
            }
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                stage.setScrollFocus(null);
            }
        });
        File[] saves = new File("./saves").listFiles();
        Arrays.sort(saves, Comparator.comparing(File::lastModified));
        Collections.reverse(Arrays.asList(saves));
        Array<String> array = new Array<>();
        for (File save : saves) {
            array.add(save.getName().replace(".sbs", ""));
        }
        list.setItems(array);
        table.add(scrollPane);
        table.row();
        Table buttons = new Table();
        TextButton joinButton = new TextButton("Join", skin);
        joinButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(list.getSelected() == null)
                    return;
                File saveFile = new File("./saves/" + list.getSelected() + ".sbs");
                Server server = new Server(0, saveFile);
                try{
                    FileInputStream fileInputStream = new FileInputStream(saveFile);
                    server.loadSaveFile(new SaveFile(new DataInputStream(fileInputStream)));
                    fileInputStream.close();
                } catch(Exception e){
                    e.printStackTrace();
                    System.out.println("couldn't load");
                    ScrapBox.getInstance().setScene(new MainMenuScene());
                    server.stop();
                    return;
                }
                IConnection connection = server.joinLocalPlayer();
                server.start();
                ScrapBox.getInstance().setScene(new InGameScene(connection, server, null));
            }
        });
        buttons.add(joinButton);
        TextButton copyButton = new TextButton("Copy", skin);
        copyButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(list.getItems().isEmpty())
                    return;
                String saveFileName = list.getSelected();

                TextField input = new TextField("", skin);
                Dialog dialog = new Dialog("Copy World", skin, "dialog") {
                    public void result(Object obj) {
                        if(obj instanceof String && !input.getText().isEmpty()){
                            File saveFile = new File("./saves/" + input.getText() + ".sbs");
                            try {
                                File oldFile = new File("./saves/" + saveFileName + ".sbs");
                                Files.copy(oldFile.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            ScrapBox.getInstance().setScene(new WorldJoinScene());
                        }
                    }
                };
                dialog.button("Cancel");
                dialog.button("Ok", "");
                dialog.getContentTable().add(input);
                dialog.show(stage);
            }
        });
        buttons.add(copyButton);
        TextButton deleteButton = new TextButton("Delete", skin);
        deleteButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(list.getItems().isEmpty())
                    return;
                String saveFileName = list.getSelected();
                Dialog dialog = new Dialog("Do you want to delete \"" + saveFileName + "\" savefile?", skin, "dialog") {
                    public void result(Object obj) {
                        if(obj instanceof String){
                            File saveFile = new File("./saves/" + saveFileName + ".sbs");
                            saveFile.delete();
                            Array<String> items = list.getItems();
                            items.removeValue(saveFileName, false);
                            list.setItems(items);
                        }
                    }
                };
                dialog.button("Cancel");
                dialog.button("Delete", "");
                dialog.show(stage);
            }
        });
        buttons.add(deleteButton);
        TextButton createButton = new TextButton("Create", skin);
        createButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TextField input = new TextField("", skin);
                Dialog dialog = new Dialog("Create World", skin, "dialog") {
                    public void result(Object obj) {
                        if(obj instanceof String && !input.getText().isEmpty()){
                            File saveFile = new File("./saves/" + input.getText() + ".sbs");
                            Server server = new Server(0, saveFile);
                            IConnection connection = server.joinLocalPlayer();
                            server.start();
                            ScrapBox.getInstance().setScene(new InGameScene(connection, server, null));
                        }
                    }
                };
                dialog.button("Cancel");
                dialog.button("Ok", "");
                dialog.getContentTable().add(input);
                dialog.show(stage);
            }
        });
        buttons.add(createButton);
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScrapBox.getInstance().setScene(new MainMenuScene());
            }
        });
        buttons.add(backButton);
        table.add(buttons);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
