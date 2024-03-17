package com.github.industrialcraft.scrapbox.server.net;

import java.util.ArrayList;

public interface IClientConnection {
    void send(MessageC2S message);
    ArrayList<MessageS2C> read();
}
