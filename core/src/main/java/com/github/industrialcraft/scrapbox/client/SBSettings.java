package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class SBSettings {
    public final Preferences preferences;
    public final Keybinding UP = new Keybinding("up", "Up", Input.Keys.W);
    public final Keybinding DOWN = new Keybinding("down", "Down", Input.Keys.S);
    public final Keybinding LEFT = new Keybinding("left", "Left", Input.Keys.A);
    public final Keybinding RIGHT = new Keybinding("right", "Right", Input.Keys.D);
    public final Keybinding GHOST_MODE = new Keybinding("ghost_mode", "Toggle Ghost Mode", Input.Keys.Q);
    public final Keybinding WELD = new Keybinding("weld", "Weld", Input.Keys.F);
    public final Keybinding FREEZE = new Keybinding("freeze", "Freeze", Input.Keys.X);
    public final Keybinding OPEN_CONTROLLER = new Keybinding("controller_open", "Control Vehicle", Input.Keys.C);
    public final Keybinding EDIT_OBJECT = new Keybinding("edit_object", "Edit Object", Input.Keys.V);
    public final Keybinding BREAK_CONNECTION = new Keybinding("break_connection", "Break Connections", Input.Keys.B);
    public final Keybinding GEAR_CONNECTION = new Keybinding("gear_connection", "Gear Connections", Input.Keys.G);
    public final Keybinding WRENCH = new Keybinding("wrench", "Wrench", Input.Keys.N);
    public final Keybinding DEBUG_PHYSICS_RENDERING = new Keybinding("debug_physics_rendering", "Debug Physics Rendering", Input.Keys.F1);
    public final Keybinding PAUSE_GAME = new Keybinding("pause_game", "Pause Game", Input.Keys.F2);
    public final Keybinding STEP_GAME = new Keybinding("step_game", "Single Step Game", Input.Keys.F3);
    public final Keybinding[] KEYBINDINGS = new Keybinding[]{UP, DOWN, LEFT, RIGHT, GHOST_MODE, WELD, FREEZE, OPEN_CONTROLLER, EDIT_OBJECT, BREAK_CONNECTION, GEAR_CONNECTION, WRENCH, DEBUG_PHYSICS_RENDERING, PAUSE_GAME, STEP_GAME};
    public SBSettings() {
        this.preferences = Gdx.app.getPreferences("ScrapBoxPrefs");
        for(Keybinding keybinding : KEYBINDINGS){
            keybinding.key = preferences.getInteger("key." + keybinding.id, keybinding.defaultKey);
        }
    }
    public Table createTable(Runnable onSettingChange){
        Table table = new Table();
        for(Keybinding keybinding : KEYBINDINGS){
            TextButton button = new TextButton(Input.Keys.toString(keybinding.key), ScrapBox.getInstance().getSkin());
            button.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int btn) {
                    event.getStage().setKeyboardFocus(button);
                    return false;
                }
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    event.getStage().setKeyboardFocus(null);
                }
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if(button.isPressed()){
                        keybinding.key = keycode;
                        button.setText(Input.Keys.toString(keycode));
                        preferences.putInteger("key." + keybinding.id, keycode);
                        preferences.flush();
                        if(onSettingChange != null)
                            onSettingChange.run();
                        return true;
                    }
                    return false;
                }
            });
            table.add(new Label(keybinding.name, ScrapBox.getInstance().getSkin()), button).row();
        }
        return table;
    }
    public static class Keybinding{
        public final String id;
        public final String name;
        public final int defaultKey;
        public int key;
        public Keybinding(String id, String name, int defaultKey) {
            this.id = id;
            this.name = name;
            this.defaultKey = defaultKey;
            this.key = defaultKey;
        }
        public boolean isDown(){
            return Gdx.input.isKeyPressed(key);
        }
        public boolean isJustDown(){
            return Gdx.input.isKeyJustPressed(key);
        }
    }
}
