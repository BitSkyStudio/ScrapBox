package com.github.industrialcraft.scrapbox.common.net;

import java.util.ArrayList;

public interface IConnection {
    void send(Object message);
    ArrayList<Object> read();
}
