package me.hikari.socks;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class SocksMessage {
    private static final byte SOCKS_VERSION = 0x05;
    private static final byte STATUS_GRANTED = 0x00;
    private static final byte NO_AUTH = 0x00;
    private static final byte IP_V4 = 0x01;
    private static final byte NOB = 0x00; // empty byte

    public static void putConnResponse(ByteBuffer in) {
        // 6 NOB spam = zero ip + zero port
        in.put(new byte[]{
                SOCKS_VERSION,
                STATUS_GRANTED,
                NOB,
                IP_V4,
                NOB, NOB, NOB, NOB,
                NOB, NOB,
        }).flip();
    }

    public static void putNoAuthResponse(SelectionKey key) {
        SocksUtils.useInAsOut(key);
        SocksUtils.clearOut(key);
        SocksUtils.getAttachment(key).getOut().put(new byte[] {SOCKS_VERSION, NO_AUTH}).flip();
    }

    public static boolean inTooSmall(SelectionKey key){
        return (SocksUtils.getAttachment(key).getIn().position() < 2);
    }

    public static boolean wrongVersion(SelectionKey key) {
        return (SocksUtils.getAttachment(key).getIn().array()[0] != SOCKS_VERSION);
    }

    public static boolean methodsReceived(SelectionKey key) {
        var data = SocksUtils.getAttachment(key).getIn().array();
        var methodsCount = data[1];
        return (data.length - 2 == methodsCount);
    }

    public static boolean noAuthNotFound(SelectionKey key) {
        var data = SocksUtils.getAttachment(key).getIn().array();
        var methodsCount = data[1];
        boolean res = true;
        for (int i = 0; i < methodsCount; i++) {
            if (data[i + 2] == NO_AUTH) {
                res = false;
                break;
            }
        }
        return res;
    }

}
