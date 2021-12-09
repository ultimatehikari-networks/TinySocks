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

    public static void clearIn(SelectionKey key){
        getAttachment(key).getIn().clear();
    }

    public static void clearOut(SelectionKey key){
        getAttachment(key).getOut().clear();
    }

    public static boolean isDecoupled(SelectionKey key){
        return getAttachment(key).isDecoupled();
    }

    public static boolean tryReadToBuffer(SelectionKey key) throws IOException {
        return (getChannel(key).read(getAttachment(key).getIn()) > 0);
    }

    public static void useInAsOut(SelectionKey key) {
        getAttachment(key).setOut(getAttachment(key).getIn());
    }
}
