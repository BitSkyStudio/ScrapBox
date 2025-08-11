package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;
import com.github.industrialcraft.scrapbox.server.EItemType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class SetGameObjectEditUIInventory {
    public final int id;
    public final EnumMap<EItemType,Float> inventory;
    public SetGameObjectEditUIInventory(int id, EnumMap<EItemType, Float> inventory) {
        this.id = id;
        this.inventory = inventory;
    }
    public SetGameObjectEditUIInventory(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.inventory = new EnumMap<>(EItemType.class);
        int size = stream.readInt();
        for(int i = 0;i < size;i++){
            this.inventory.put(EItemType.byId(stream.readByte()), stream.readFloat());
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeInt(inventory.size());
        for(Map.Entry<EItemType, Float> entry : inventory.entrySet()){
            stream.writeByte(entry.getKey().id);
            stream.writeFloat(entry.getValue());
        }
    }
    public static MessageRegistry.MessageDescriptor<SetGameObjectEditUIInventory> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(SetGameObjectEditUIInventory.class, SetGameObjectEditUIInventory::new, SetGameObjectEditUIInventory::toStream);
    }
}
