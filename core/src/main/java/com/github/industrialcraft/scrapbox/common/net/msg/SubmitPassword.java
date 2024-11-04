package com.github.industrialcraft.scrapbox.common.net.msg;

import com.github.industrialcraft.netx.MessageRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SubmitPassword {
    public final String password;
    public SubmitPassword(String password){
        this.password = password;
    }
    public SubmitPassword(DataInputStream stream) throws IOException {
        this.password = stream.readUTF();
    }
    public void toStream(DataOutputStream stream) throws IOException {
        stream.writeUTF(password);
    }
    public static MessageRegistry.MessageDescriptor<SubmitPassword> createDescriptor(){
        return new MessageRegistry.MessageDescriptor<>(SubmitPassword.class, SubmitPassword::new, SubmitPassword::toStream);
    }
}
