package me.hikari.socks;

import java.nio.ByteBuffer;

public class SocksMessage {
    private static final byte SOCKS_VERSION = 0x05;
    private static final byte STATUS_GRANTED = 0x00;
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
        }); //TODO flip?
    }
}
