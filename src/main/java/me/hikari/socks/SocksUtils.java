package me.hikari.socks;

import java.io.IOException;
import java.net.InetAddress;
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

    public static void couple(InetAddress addr, int port, SelectionKey key) throws IOException {
        getAttachment(key).couple(addr, port, key);
    }

    public static void close(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
        SocksUtils.getAttachment(key).decouple();
    }

    public static void partiallyClose(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
    }
}
