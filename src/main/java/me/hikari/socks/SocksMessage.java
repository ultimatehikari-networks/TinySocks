package me.hikari.socks;

import lombok.extern.log4j.Log4j2;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Arrays;

@Log4j2
public class SocksMessage {
    public static final byte HOST_SMALLNESS = -1;
    public static final byte AUTH_SMALLNESS = 2;
    public static final byte CONN_SMALLNESS = 4;
    public static final byte IP_PORTPOS = 8;
    public static final byte HOST_PORTPOS = -1;
    public static final byte HOST_START = 5;
    public static final byte PORT_LEN = 2;
    private static final byte SOCKS_VERSION = 0x05;
    private static final byte CONNECT = 0x01;
    private static final byte STATUS_GRANTED = 0x00;
    private static final byte NO_AUTH = 0x00;
    private static final byte IP_V4 = 0x01;
    private static final byte HOST = 0x03;
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

    public static boolean inTooSmall(SelectionKey key, byte smallness){
        if(smallness != HOST_SMALLNESS) {
            return (SocksUtils.getAttachment(key).getIn().position() < smallness);
        } else {
            var len = getHostLen(key);
            log.info("host len in smallness check: " + len);
            return (SocksUtils.getAttachment(key).getIn().position() < HOST_START + len + PORT_LEN);
        }
    }

    public static boolean wrongVersion(SelectionKey key) {
        return (SocksUtils.getAttachment(key).getIn().array()[0] != SOCKS_VERSION);
    }

    public static boolean wrongCommand(SelectionKey key) {
        return (SocksUtils.getAttachment(key).getIn().array()[1] != CONNECT);
    }

    public static boolean wrongAddrType(SelectionKey key) {
        return (!isIpv4(key)) && (!isHost(key));
    }

    public static boolean isIpv4(SelectionKey key) {
        return (SocksUtils.getAttachment(key).getIn().array()[3] == IP_V4);
    }
    public static boolean isHost(SelectionKey key) {
        return (SocksUtils.getAttachment(key).getIn().array()[3] == HOST);
    }

    public static boolean methodsReceived(SelectionKey key) {
        var in = SocksUtils.getAttachment(key).getIn();
        var methodsCount = in.array()[1];
        var pos = in.position();
        return (pos - 2 == methodsCount);
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

    public static byte[] getIpv4(SelectionKey key) {
        var data = SocksUtils.getAttachment(key).getIn().array();
        return new byte[]{data[4], data[5], data[6], data[7]};
    }

    public static int getPort(SelectionKey key, int pos){
        int actualPos = pos;
        if(pos == HOST_PORTPOS){
            actualPos = HOST_START + getHostLen(key);
        }
        var data = SocksUtils.getAttachment(key).getIn().array();
        return ((data[actualPos] & 0xff) << 8) + (data[actualPos + 1] & 0xff);
    }

    public static int getHostLen(SelectionKey key){
        return SocksUtils.getAttachment(key).getIn().array()[4];
    }

    public static String getHost(SelectionKey key){
        var data = SocksUtils.getAttachment(key).getIn().array();
        return new String(Arrays.copyOfRange(data, HOST_START, HOST_START + getHostLen(key)));
    }
}
