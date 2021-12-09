package me.hikari.socks;

import java.net.InetAddress;

public class ProxyMessage {
    public InetAddress ip = null;
    public int version;
    public int port;
    public int command;
    public String host=null;

    byte[] data;

    public ProxyMessage(int cmd, InetAddress ip, int port){

    }
}
