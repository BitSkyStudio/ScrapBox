package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SBSettings {
    private static final Logger log = LoggerFactory.getLogger(SBSettings.class);
    public final Preferences preferences;
    public final Keybinding UP = new Keybinding("up", "Up", Input.Keys.W);
    public final Keybinding DOWN = new Keybinding("down", "Down", Input.Keys.S);
    public final Keybinding LEFT = new Keybinding("left", "Left", Input.Keys.A);
    public final Keybinding RIGHT = new Keybinding("right", "Right", Input.Keys.D);
    public final Keybinding GHOST_MODE = new Keybinding("ghost_mode", "Toggle Ghost Mode", Input.Keys.Q);
    public final Keybinding WELD = new Keybinding("weld", "Weld", Input.Keys.F);
    public final Keybinding FREEZE = new Keybinding("freeze", "Freeze", Input.Keys.X);
    public final Keybinding OPEN_CONTROLLER = new Keybinding("controller_open", "Control Vehicle", Input.Keys.C);
    public final Keybinding EDIT_OBJECT = new Keybinding("edit_object", "Edit Object", Keybinding.fromButton(Input.Buttons.RIGHT));
    public final Keybinding BREAK_CONNECTION = new Keybinding("break_connection", "Break Connections", Input.Keys.B);
    public final Keybinding GEAR_CONNECTION = new Keybinding("gear_connection", "Gear Connections", Input.Keys.G);
    public final Keybinding WRENCH = new Keybinding("wrench", "Wrench", Input.Keys.N);
    public final Keybinding DEBUG_PHYSICS_RENDERING = new Keybinding("debug_physics_rendering", "Debug Physics Rendering", Input.Keys.F1);
    public final Keybinding PAUSE_GAME = new Keybinding("pause_game", "Pause Game", Input.Keys.F2);
    public final Keybinding SAVESTATE = new Keybinding("save_state", "Save State", Input.Keys.F4);
    public final Keybinding STEP_GAME = new Keybinding("step_game", "Single Step Game", Input.Keys.F3);
    public final Keybinding[] KEYBINDINGS = new Keybinding[]{UP, DOWN, LEFT, RIGHT, GHOST_MODE, WELD, FREEZE, OPEN_CONTROLLER, EDIT_OBJECT, BREAK_CONNECTION, GEAR_CONNECTION, WRENCH, DEBUG_PHYSICS_RENDERING, PAUSE_GAME, STEP_GAME, SAVESTATE};
    public SBSettings() {
        this.preferences = Gdx.app.getPreferences("ScrapBoxPrefs");
        for(Keybinding keybinding : KEYBINDINGS){
            keybinding.key = preferences.getInteger("key." + keybinding.id, keybinding.defaultKey);
        }
    }
    public Table createTable(Runnable onSettingChange){
        Table table = new Table();
        for(Keybinding keybinding : KEYBINDINGS){
            TextButton resetButton = new TextButton("Reset", ScrapBox.getInstance().getSkin());
            TextButton button = new TextButton(keybinding.keyToString(), ScrapBox.getInstance().getSkin());
            button.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int btn) {
                    if(btn == Input.Buttons.LEFT) {
                        event.getStage().setKeyboardFocus(button);
                    } else {
                        if(button.isPressed()){
                            keybinding.key = Keybinding.fromButton(btn);
                            button.setText(keybinding.keyToString());
                            preferences.putInteger("key." + keybinding.id, keybinding.key);
                            preferences.flush();
                            resetButton.setDisabled(keybinding.isDefault());
                            if(onSettingChange != null)
                                onSettingChange.run();
                            return true;
                        }
                    }
                    return false;
                }
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if(button == Input.Keys.LEFT) {
                        event.getStage().setKeyboardFocus(null);
                    }
                }
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if(button.isPressed()){
                        keybinding.key = keycode;
                        button.setText(keybinding.keyToString());
                        preferences.putInteger("key." + keybinding.id, keybinding.key);
                        preferences.flush();
                        resetButton.setDisabled(keybinding.isDefault());
                        if(onSettingChange != null)
                            onSettingChange.run();
                        return true;
                    }
                    return false;
                }
            });
            resetButton.setDisabled(keybinding.isDefault());
            resetButton.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    keybinding.key = keybinding.defaultKey;
                    button.setText(keybinding.keyToString());
                    preferences.remove("key." + keybinding.id);
                    preferences.flush();
                    resetButton.setDisabled(true);
                    if(onSettingChange != null)
                        onSettingChange.run();
                }
            });
            table.add(new Label(keybinding.name, ScrapBox.getInstance().getSkin()), button, resetButton).row();
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
        public static int fromButton(int button){
            return -button - 1;
        }
        public String keyToString(){
            if(key < 0){
                switch(-key - 1){
                    case Input.Buttons.LEFT:
                        return "Left";
                    case Input.Buttons.RIGHT:
                        return "Right";
                    case Input.Buttons.MIDDLE:
                        return "Middle";
                    case Input.Buttons.FORWARD:
                        return "Forward";
                    case Input.Buttons.BACK:
                        return "Back";
                    default:
                        return "Unknown Button";
                }
            } else {
                return Input.Keys.toString(key);
            }
        }
        public boolean isDown(){
            if(key < 0){
                return Gdx.input.isButtonPressed(-key - 1);
            } else {
                return Gdx.input.isKeyPressed(key);
            }
        }
        public boolean isJustDown(){
            if(key < 0){
                return Gdx.input.isButtonJustPressed(-key - 1);
            } else {
                return Gdx.input.isKeyJustPressed(key);
            }
        }
        public boolean isDefault(){
            return key == defaultKey;
        }
    }
}
