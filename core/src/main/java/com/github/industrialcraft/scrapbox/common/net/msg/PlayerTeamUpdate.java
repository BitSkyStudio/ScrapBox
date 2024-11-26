package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerTeamUpdate {
    public final String team;
    public PlayerTeamUpdate(String team){
        this.team = team;
    }
    public PlayerTeamUpdate(DataInputStream stream) throws IOException {
        this.team = stream.readUTF();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeUTF(team);
    }
    public static MessageRegistry.MessageDescriptor<PlayerTeamUpdate> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(PlayerTeamUpdate.class, PlayerTeamUpdate::new, PlayerTeamUpdate::toStream);
    }
}
