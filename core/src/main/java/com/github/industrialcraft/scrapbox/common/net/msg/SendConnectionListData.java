package com.github.industrialcraft.scrapbox.common.net.msg;

import com.badlogic.gdx.math.Vector2;
import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SendConnectionListData {
    public final ArrayList<Connection> connections;
    public SendConnectionListData(ArrayList<Connection> connections) {
        this.connections = connections;
    }
    public SendConnectionListData(DataInputStream stream) throws IOException {
        int count = stream.readInt();
        this.connections = new ArrayList<>(count);
        for(int i = 0;i < count;i++){
            this.connections.add(new Connection(new Vector2(stream.readFloat(), stream.readFloat()), stream.readInt(), stream.readUTF()));
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(connections.size());
        for(Connection connection : connections){
            stream.writeFloat(connection.position.x);
            stream.writeFloat(connection.position.y);
            stream.writeInt(connection.gameObjectId);
            stream.writeUTF(connection.name);
        }
    }
    public static MessageRegistry.MessageDescriptor<SendConnectionListData> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(SendConnectionListData.class, SendConnectionListData::new, SendConnectionListData::toStream);
    }
    public static class Connection{
        public final Vector2 position;
        public final int gameObjectId;
        public final String name;
        public Connection(Vector2 position, int gameObjectId, String name) {
            this.position = position;
            this.gameObjectId = gameObjectId;
            this.name = name;
        }
    }
}
