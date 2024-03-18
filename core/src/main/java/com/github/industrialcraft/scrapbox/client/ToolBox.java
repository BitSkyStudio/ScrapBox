package com.github.industrialcraft.scrapbox.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.scrapbox.common.net.msg.TakeObject;

import java.util.ArrayList;

public class ToolBox {
    public final ScrapBox game;
    private int width;
    private Texture background;
    private ArrayList<Part> parts;
    private int partScroll;
    public ToolBox(ScrapBox game) {
        this.game = game;
        this.background = new Texture("toolbox.png");
        this.width = 100;
        this.parts = new ArrayList<>();
    }
    public void render(SpriteBatch batch){
        batch.draw(background, Gdx.graphics.getWidth()-width, 0, width, Gdx.graphics.getHeight());
        for(int i = 0;i < this.parts.size();i++){
            Part part = this.parts.get(i);
            batch.draw(part.renderData.texture, Gdx.graphics.getWidth()-width, i*width-partScroll, width, width);
        }
    }
    public void click(Vector2 position){
        float x = ((position.x+width-Gdx.graphics.getWidth())/width*2)-1;
        float y = (position.y + partScroll)/width;
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
}
