package me.hikari.socks;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class SocksUtils {
    public static Attachment getAttachment(SelectionKey key){
        return (Attachment) key.attachment();
    }

    public static SocketChannel getChannel(SelectionKey key){
        return (SocketChannel) key.channel();
    }

    public static boolean tryReadToBuffer(SelectionKey key) throws IOException {
        return (getChannel(key).read(getAttachment(key).getIn()) > 0);
    }
}
