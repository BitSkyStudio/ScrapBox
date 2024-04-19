package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.msg.TakeObject;

import java.util.ArrayList;

public class ToolBox {
    public final InGameScene game;
    private int width;
    private final Texture background;
    private final ArrayList<Part> parts;
    private float partScroll;
    public Tool tool;
    public ArrayList<ToolType> tools;
    private final ArrayList<String> terrainTypes;
    private int selectedTerrain;
    public ToolBox(InGameScene game) {
        this.game = game;
        this.background = new Texture("toolbox.png");
        this.width = 100;
        this.parts = new ArrayList<>();
        this.tool = Tool.Hand;
        this.tools = new ArrayList<>();
        this.tools.add(new ToolType(Tool.Hand, new Texture("mode_hand.png")));
        this.tools.add(new ToolType(Tool.DeleteJoints, new Texture("mode_destroy_joint.png")));
        this.tools.add(new ToolType(Tool.TerrainPlace, new Texture("mode_terrain_place.png")));
        this.tools.add(new ToolType(Tool.TerrainDestroy, new Texture("mode_break_terrain.png")));
        this.selectedTerrain = 0;
        this.terrainTypes = new ArrayList<>();
    }
    public void addTerrainType(String type){
        this.terrainTypes.add(type);
    }
    public int getWidth() {
        return width;
    }
    public void render(Batch batch){


        float leftOffset = Gdx.graphics.getWidth()-width;
        batch.draw(background, leftOffset, 0, width, Gdx.graphics.getHeight());
        int toolHeight = width/tools.size();

        float clampedScroll = -Math.max(0, Math.min(-this.partScroll, (parts.size()*width)-Gdx.graphics.getHeight()+toolHeight));
        this.partScroll -= (partScroll-clampedScroll) * Gdx.graphics.getDeltaTime() * 10;

        if(isTerrainSelectionOpen()){
            for (int i = 0; i < game.terrainRenderer.textures.size(); i++) {
                TextureRegion texture = game.terrainRenderer.textures.get(terrainTypes.get(i));
                if(selectedTerrain == i){
                    batch.setColor(0.3f, 0.3f, 0.3f, 1);
                } else {
                    batch.setColor(0.5f, 0.5f, 0.5f, 1);
                }
                batch.draw(texture, leftOffset, Gdx.graphics.getHeight() - (i + 1) * width - partScroll - toolHeight, width, width);
            }
        } else {
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
            batch.draw(tools.get(i).texture, leftOffset+toolHeight*i, Gdx.graphics.getHeight()-toolHeight, toolHeight, toolHeight);
        }
    }
    public boolean isTerrainSelectionOpen(){
        return tool == Tool.TerrainDestroy || tool == Tool.TerrainPlace;
    }
    public void click(Vector2 position){
        int toolHeight = width/tools.size();
        if(position.y > Gdx.graphics.getHeight() - toolHeight){
            tool = tools.get((int) ((position.x-(Gdx.graphics.getWidth()-width))/toolHeight)).tool;
            return;
        }
        float x = ((position.x + width - Gdx.graphics.getWidth()) / width * 2) - 1;
        float y = (Gdx.graphics.getHeight() - (position.y + partScroll + toolHeight)) / width;
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
        this.partScroll += -value * 40;
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
        public ToolType(Tool tool, Texture texture) {
            this.tool = tool;
            this.texture = texture;
        }
    }
    public enum Tool{
        Hand,
        DeleteJoints,
        TerrainPlace,
        TerrainDestroy
    }
}
