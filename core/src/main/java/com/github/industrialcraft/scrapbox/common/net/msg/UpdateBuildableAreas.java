package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Rectangle;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateBuildableAreas {
    public final ArrayList<Rectangle> areas;
    public UpdateBuildableAreas(ArrayList<Rectangle> areas) {
        this.areas = areas;
    }
    public UpdateBuildableAreas(DataInputStream stream) throws IOException {
        int count = stream.readInt();
        this.areas = new ArrayList<>();
        for(int i = 0;i < count;i++){
            this.areas.add(new Rectangle(stream.readFloat(), stream.readFloat(), stream.readFloat(), stream.readFloat()));
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(areas.size());
        for(Rectangle rectangle : areas){
            stream.writeFloat(rectangle.x);
            stream.writeFloat(rectangle.y);
            stream.writeFloat(rectangle.width);
            stream.writeFloat(rectangle.height);
        }
    }
    public static MessageRegistry.MessageDescriptor<UpdateBuildableAreas> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(UpdateBuildableAreas.class, UpdateBuildableAreas::new, UpdateBuildableAreas::toStream);
    }
}
