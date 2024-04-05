package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.msg.TakeObject;

import java.util.ArrayList;

public class ToolBox {
    public final InGameScene game;
    private int width;
    private final Texture background;
    private final ArrayList<Part> parts;
    private int partScroll;
    public Tool tool;
    public ArrayList<ToolType> tools;
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
    }
    public int getWidth() {
        return width;
    }
    public void render(Batch batch){
        float leftOffset = Gdx.graphics.getWidth()-width;
        batch.draw(background, leftOffset, 0, width, Gdx.graphics.getHeight());
        int toolHeight = width/tools.size();
        for(int i = 0;i < this.parts.size();i++){
            Part part = this.parts.get(i);
            batch.draw(part.renderData.texture, leftOffset, Gdx.graphics.getHeight()-(i+1)*width-partScroll-toolHeight, width, width);
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
    public void click(Vector2 position){
        int toolHeight = width/tools.size();
        if(position.y > Gdx.graphics.getHeight() - toolHeight){
            tool = tools.get((int) ((position.x-(Gdx.graphics.getWidth()-width))/toolHeight)).tool;
            return;
        }
        if(tool != Tool.Hand){
            return;
        }
        float x = ((position.x+width-Gdx.graphics.getWidth())/width*2)-1;
        float y = (Gdx.graphics.getHeight()-(position.y + partScroll + toolHeight))/width;
        if(this.parts.size() > (int)y){
            Part part = this.parts.get((int)y);
            game.connection.send(new TakeObject(part.type, game.mouseSelector.getWorldMousePosition(), new Vector2(x*part.renderData.width, (((y%1)*2)-1)*part.renderData.height)));
        }
    }
    public boolean isMouseInside(){
        return Gdx.input.getX() > (Gdx.graphics.getWidth()-this.width);
    }
    public void dispose(){
        this.background.dispose();
    }
    public void addPart(String type, RenderData renderData){
        this.parts.add(new Part(type, renderData));
    }
    public void scroll(int value){
        this.partScroll = Math.max(0, Math.min(value+(this.partScroll*10), (parts.size()*width)-Gdx.graphics.getHeight()));
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
