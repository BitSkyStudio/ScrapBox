package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIElement;
import com.github.industrialcraft.scrapbox.common.editui.EditorUIRow;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SetGameObjectEditUIData {
    public final int id;
    public final ArrayList<EditorUIRow> rows;
    public SetGameObjectEditUIData(int id, ArrayList<EditorUIRow> rows) {
        this.id = id;
        this.rows = rows;
    }
    public SetGameObjectEditUIData(DataInputStream stream) throws IOException {
        this.id = stream.readInt();
        this.rows = new ArrayList<>();
        int count = stream.readInt();
        for(int i = 0;i < count;i++){
            ArrayList<EditorUIElement> elements = new ArrayList<>();
            int rowLength = stream.readInt();
            for(int j = 0;j < rowLength;j++) {
                elements.add(EditorUIElement.load(stream));
            }
            rows.add(new EditorUIRow(elements));
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(id);
        stream.writeInt(rows.size());
        for(EditorUIRow row : rows){
            stream.writeInt(row.elements.size());
            for(EditorUIElement element : row.elements){
                stream.writeInt(element.getId());
                element.toStream(stream);
            }
        }
    }
    public static MessageRegistry.MessageDescriptor<SetGameObjectEditUIData> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(SetGameObjectEditUIData.class, SetGameObjectEditUIData::new, SetGameObjectEditUIData::toStream);
    }
}
