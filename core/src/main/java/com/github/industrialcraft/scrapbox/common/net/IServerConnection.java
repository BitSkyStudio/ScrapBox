package com.github.industrialcraft.scrapbox.common.net;

import java.util.ArrayList;

public interface IServerConnection {
    void send(MessageS2C message);
    ArrayList<MessageC2S> read();
}
