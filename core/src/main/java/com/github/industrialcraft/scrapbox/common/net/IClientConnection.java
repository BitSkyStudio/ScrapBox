package com.github.industrialcraft.scrapbox.common.net;

import java.util.ArrayList;

public interface IClientConnection {
    void send(MessageC2S message);
    ArrayList<MessageS2C> read();
}
