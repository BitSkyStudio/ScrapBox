package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Rectangle;
import com.github.industrialcraft.netx.MessageRegistry;
import com.github.industrialcraft.scrapbox.server.EItemType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class UpdateInventory {
    public EnumMap<EItemType, Float> inventory;
    public UpdateInventory(EnumMap<EItemType, Float> inventory) {
        this.inventory = inventory;
    }
    public UpdateInventory(DataInputStream stream) throws IOException {
        int count = stream.readInt();
        this.inventory = new EnumMap<>(EItemType.class);
        for(int i = 0;i < count;i++){
            this.inventory.put(EItemType.byId(stream.readByte()), stream.readFloat());
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(inventory.size());
        for(Map.Entry<EItemType, Float> entry : inventory.entrySet()){
            stream.writeByte(entry.getKey().id);
            stream.writeFloat(entry.getValue());
        }
    }
    public static MessageRegistry.MessageDescriptor<UpdateInventory> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(UpdateInventory.class, UpdateInventory::new, UpdateInventory::toStream);
    }
}
