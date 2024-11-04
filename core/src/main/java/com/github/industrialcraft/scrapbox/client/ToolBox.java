package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.msg.TakeObject;

import java.util.ArrayList;

public class ToolBox {
    public final InGameScene game;
    private int width;
    private final Texture background;
    private final ArrayList<Part> parts;
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
    }
    public boolean isTerrainSelectionOpen(){
        return tool == Tool.TerrainModify;
    }
    public void click(Vector2 position){
        int toolHeight = width/tools.size();
        if(position.y > Gdx.graphics.getHeight() - toolHeight){
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
            if (this.terrainTypes.size() > (int) y) {
                this.selectedTerrain = (int) y;
            }
        } else {
            if (this.parts.size() > (int) y) {
                Part part = this.parts.get((int) y);
                game.connection.send(new TakeObject(part.type, game.mouseSelector.getWorldMousePosition(), new Vector2(x * part.renderData.width, (((y % 1) * 2) - 1) * part.renderData.height)));
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
    public void addPart(String type, RenderData renderData){
        this.parts.add(new Part(type, renderData));
    }
    public void scroll(int value){
        this.scrollVelocity += value;
    }

    public static class Part{
        public final String type;
        public final RenderData renderData;
        public Part(String type, RenderData renderData) {
            this.type = type;
            this.renderData = renderData;
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
