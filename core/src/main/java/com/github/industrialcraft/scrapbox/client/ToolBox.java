package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.github.industrialcraft.scrapbox.common.Material;
import com.github.industrialcraft.scrapbox.common.net.msg.TakeObject;
import com.github.industrialcraft.scrapbox.server.EItemType;
import com.github.industrialcraft.scrapbox.server.GameObject;
import com.github.tommyettinger.colorful.rgb.ColorfulBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class ToolBox {
    public final InGameScene game;
    private int width;
    private final Texture background;
    private final ArrayList<Part> parts;
    private ArrayList<GameObject.GameObjectConfig> partsConfig;
    private float partScroll;
    private float terrainScroll;
    private float scrollVelocity;
    public Tool tool;
    public ArrayList<ToolType> tools;
    private final ArrayList<String> terrainTypes;
    private int selectedTerrain;
    public float brushSize;
    public boolean brushRectangle;
    public ToolBox(InGameScene game) {
        this.game = game;
        this.background = new Texture("toolbox.png");
        this.width = 100;
        this.parts = new ArrayList<>();
        this.partsConfig = new ArrayList<>();
        this.tool = Tool.Hand;
        this.tools = new ArrayList<>();
        this.tools.add(new ToolType(Tool.Hand, new Texture("mode_hand.png"), null));
        this.tools.add(new ToolType(Tool.TerrainModify, new Texture("mode_terrain_modify_circle.png"), new Texture("mode_terrain_modify_rect.png")));
        this.selectedTerrain = 0;
        this.terrainTypes = new ArrayList<>();
        this.brushSize = 1;
        this.brushRectangle = false;
        this.scrollVelocity = 0;
    }
    public void addTerrainType(String type){
        this.terrainTypes.add(type);
    }
    public int getWidth() {
        return width;
    }
    public void render(Batch batch){
        if(isTerrainSelectionOpen()){
            this.terrainScroll += -scrollVelocity * 600 * Gdx.graphics.getDeltaTime();
        } else {
            this.partScroll += -scrollVelocity * 600 * Gdx.graphics.getDeltaTime();
        }
        scrollVelocity -= scrollVelocity*Gdx.graphics.getDeltaTime()*5;
        if(Math.abs(scrollVelocity) < 0.01){
            scrollVelocity = 0;
        }

        float leftOffset = Gdx.graphics.getWidth()-width;
        batch.draw(background, leftOffset-width/32f, 0, width*33f/32f, Gdx.graphics.getHeight());
        int toolHeight = width/tools.size();

        if(isTerrainSelectionOpen()){
            float clampedScroll = -Math.max(0, Math.min(-this.terrainScroll, (terrainTypes.size() * width) - Gdx.graphics.getHeight() + toolHeight));
            this.terrainScroll -= (terrainScroll - clampedScroll) * Gdx.graphics.getDeltaTime() * 10;
        } else {
            float clampedScroll = -Math.max(0, Math.min(-this.partScroll, (parts.size() * width) - Gdx.graphics.getHeight() + toolHeight));
            this.partScroll -= (partScroll - clampedScroll) * Gdx.graphics.getDeltaTime() * 10;
        }

        if(isTerrainSelectionOpen()){
            for (int i = 0; i < game.terrainRenderer.textures.size(); i++) {
                TextureRegion texture = game.terrainRenderer.textures.get(terrainTypes.get(i));
                if(selectedTerrain == i){
                    batch.setColor(0.3f, 0.3f, 0.3f, 1);
                } else {
                    batch.setColor(0.5f, 0.5f, 0.5f, 1);
                }
                batch.draw(texture, leftOffset, Gdx.graphics.getHeight() - (i + 1) * width - terrainScroll - toolHeight, width, width);
            }
        } else if(tool == Tool.Hand) {
            for (int i = 0; i < this.parts.size(); i++) {
                Part part = this.parts.get(i);
                float maxLength = Math.max(part.renderData.width, part.renderData.height);
                if(part.renderData.materialTexture != null) {
                    Color color = this.partsConfig.get(i).material.color;
                    ((ColorfulBatch)batch).setTweak(color.r, color.g, color.b, 0.5f);
                    batch.draw(part.renderData.materialTexture, leftOffset, Gdx.graphics.getHeight() - (i + 1) * width - partScroll - toolHeight, width * (part.renderData.width / maxLength), width * (part.renderData.height / maxLength));
                    ((ColorfulBatch)batch).setTweak(ColorfulBatch.TWEAK_RESET);
                }
                if(part.renderData.texture != null)
                    batch.draw(part.renderData.texture, leftOffset, Gdx.graphics.getHeight() - (i + 1) * width - partScroll - toolHeight, width * (part.renderData.width/maxLength), width * (part.renderData.height/maxLength));
            }
        }
        for(int i = 0;i < tools.size();i++){
            if(tools.get(i).tool == tool){
                batch.setColor(0.3f, 0.3f, 0.3f, 1);
            } else {
                batch.setColor(0.5f, 0.5f, 0.5f, 1);
            }
            ToolType tool = tools.get(i);
            batch.draw(brushRectangle?(tool.alternative!=null?tool.alternative:tool.texture):tool.texture, leftOffset+toolHeight*i, Gdx.graphics.getHeight()-toolHeight, toolHeight, toolHeight);
        }
        if(tool == Tool.Hand && isMouseInside()){
            float y = ((Gdx.input.getY() - partScroll + toolHeight)) / width - 1;
            if(y >= 0 && y < this.parts.size()){
                Part part = parts.get((int) y);
                if(part != null && (!game.infiniteItems || false)) {
                    EnumMap<EItemType, Float> cost = part.costCalculator.getItemCost(partsConfig.get((int) y));
                    BitmapFont font = ScrapBox.getInstance().getSkin().getFont("default-font");
                    float lineOffset = 0;
                    for (Map.Entry<EItemType, Float> entry : cost.entrySet()) {
                        font.setColor(game.inventory.getOrDefault(entry.getKey(), 0f) < entry.getValue()?Color.RED:Color.GREEN);
                        batch.draw(game.itemTextures.get(entry.getKey()), Gdx.input.getX() - width*2, Gdx.graphics.getHeight() - (Gdx.input.getY() + lineOffset) - 24, 32, 32);
                        String text = formatFloat(game.inventory.getOrDefault(entry.getKey(), 0f)) + "/" + formatFloat(entry.getValue());
                        font.draw(batch, text, Gdx.input.getX() - width*2 + 32, Gdx.graphics.getHeight() - (Gdx.input.getY() + lineOffset));
                        lineOffset += 32;
                    }
                }
            }
        }
    }
    public static String formatFloat(float number){
        number = (float) (Math.round(number * 100.0) / 100.0);
        if(number == (long) number)
            return String.valueOf((long)number);
        else
            return String.valueOf(number);
    }
    public boolean isTerrainSelectionOpen(){
        return tool == Tool.TerrainModify;
    }
    public void click(Vector2 position, boolean rightButton){
        int toolHeight = width/tools.size();
        if(position.y > Gdx.graphics.getHeight() - toolHeight){
            if(rightButton)
                return;
            Tool newTool = tools.get((int) ((position.x-(Gdx.graphics.getWidth()-width)-1)/toolHeight)).tool;
            if(newTool == tool && tool == Tool.TerrainModify){
                brushRectangle = !brushRectangle;
            }
            tool = newTool;
            return;
        }
        float x = ((position.x + width - Gdx.graphics.getWidth()) / width * 2) - 1;
        float y = (Gdx.graphics.getHeight() - (position.y + (isTerrainSelectionOpen()?terrainScroll:partScroll) + toolHeight)) / width;
        if(isTerrainSelectionOpen()){
            if(rightButton)
                return;
            if (this.terrainTypes.size() > (int) y) {
                this.selectedTerrain = (int) y;
            }
        } else {
            if (this.parts.size() > (int) y) {
                Part part = this.parts.get((int) y);
                GameObject.GameObjectConfig config = partsConfig.get((int)y);
                if (rightButton) {
                    SelectBox<String> materials = new SelectBox<>(ScrapBox.getInstance().getSkin());
                    materials.setItems(Arrays.stream(Material.values()).map((mat) -> mat.name).toArray(String[]::new));
                    materials.setSelected(config.material.name);
                    materials.setDisabled(!part.materialModification);
                    TextField sizeField = new TextField(""+config.size, ScrapBox.getInstance().getSkin());
                    sizeField.setTextFieldFilter((textField, c) -> Character.isDigit(c) || c == '.');
                    sizeField.setDisabled(!part.sizeModification);
                    TextField angleField = new TextField(""+config.angle, ScrapBox.getInstance().getSkin());
                    angleField.setTextFieldFilter((textField, c) -> Character.isDigit(c) || c == '.');
                    angleField.setDisabled(!part.angleModification);
                    Dialog dialog = new Dialog("Object Config", ScrapBox.getInstance().getSkin(), "dialog") {
                        @Override
                        protected void result(Object object) {
                            if(object instanceof String){
                                float newSize = config.size;
                                try{
                                    newSize = Float.parseFloat(sizeField.getText());
                                } catch (Exception e){}
                                float newAngle = config.angle;
                                try{
                                    newAngle = Float.parseFloat(angleField.getText());
                                } catch (Exception e){}
                                GameObject.GameObjectConfig newConfig = new GameObject.GameObjectConfig(Material.byId((byte) materials.getSelectedIndex()), newSize, newAngle);
                                partsConfig.set((int)y, newConfig);
                            }
                        }
                    };
                    dialog.getContentTable().add(new Label("material: ", ScrapBox.getInstance().getSkin()));
                    dialog.getContentTable().add(materials).row();
                    dialog.getContentTable().add(new Label("size: ", ScrapBox.getInstance().getSkin()));
                    dialog.getContentTable().add(sizeField).row();
                    dialog.getContentTable().add(new Label("angle: ", ScrapBox.getInstance().getSkin()));
                    dialog.getContentTable().add(angleField).row();
                    dialog.button("Ok", "");
                    dialog.button("Cancel");
                    dialog.show(game.stage);
                } else {
                    game.connection.send(new TakeObject(part.type, game.mouseSelector.getWorldMousePosition(), new Vector2(x * part.renderData.width, (((y % 1) * 2) - 1) * part.renderData.height), config));
                }
            }
        }
    }
    public void changeBrushSize(float amount){
        this.brushSize = (float) MathUtils.clamp(this.brushSize+(amount/10f), .025, 5);
    }
    public String getSelectedTerrainType(){
        return this.terrainTypes.get(this.selectedTerrain);
    }
    public boolean isMouseInside(){
        return Gdx.input.getX() > (Gdx.graphics.getWidth()-this.width);
    }
    public void dispose(){
        this.background.dispose();
        this.tools.forEach(toolType -> toolType.texture.dispose());
    }
    public void addPart(String type, RenderData renderData, GameObject.GameObjectCostCalculator costCalculator, boolean materialModification, boolean sizeModification, boolean angleModification){
        this.parts.add(new Part(type, renderData, costCalculator, materialModification, sizeModification, angleModification));
        this.partsConfig.add(GameObject.GameObjectConfig.DEFAULT);
    }
    public void scroll(int value){
        this.scrollVelocity += value;
    }

    public static class Part{
        public final String type;
        public final RenderData renderData;
        public final GameObject.GameObjectCostCalculator costCalculator;
        public final boolean materialModification;
        public final boolean sizeModification;
        public final boolean angleModification;
        public Part(String type, RenderData renderData, GameObject.GameObjectCostCalculator costCalculator, boolean materialModification, boolean sizeModification, boolean angleModification) {
            this.type = type;
            this.renderData = renderData;
            this.costCalculator = costCalculator;
            this.materialModification = materialModification;
            this.sizeModification = sizeModification;
            this.angleModification = angleModification;
        }
    }
    public static class ToolType{
        public final Tool tool;
        public final Texture texture;
        public final Texture alternative;
        public ToolType(Tool tool, Texture texture, Texture alternative) {
            this.tool = tool;
            this.texture = texture;
            this.alternative = alternative;
        }
    }
    public enum Tool{
        Hand,
        TerrainModify
    }
}
