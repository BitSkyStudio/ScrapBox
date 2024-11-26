package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PlayerTeamList {
    public final ArrayList<String> teams;
    public PlayerTeamList(ArrayList<String> teams){
        this.teams = teams;
    }
    public PlayerTeamList(DataInputStream stream) throws IOException {
        int count = stream.readInt();
        this.teams = new ArrayList<>(count);
        for(int i = 0;i < count;i++){
            this.teams.add(stream.readUTF());
        }
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeInt(this.teams.size());
        for(String team : teams){
            stream.writeUTF(team);
        }
    }
    public static MessageRegistry.MessageDescriptor<PlayerTeamList> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(PlayerTeamList.class, PlayerTeamList::new, PlayerTeamList::toStream);
    }
}
