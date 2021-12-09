package me.hikari.socks;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.naming.ldap.SortKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;

@RequiredArgsConstructor
@Log4j2
public class TinyServer {
    private final Integer port;

    /**
     * runs until stopped
     */
    public void start() throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress("localhost", port));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);


        while (true) {
            selector.select();
            var iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                var key = iterator.next();
                iterator.remove();

                if (key.isValid()) {
                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isConnectable()) {
                            handleConnect(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        } else if (key.isWritable()) {
                            handleWrite(key);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        close(key);
                    }
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        log.info("Accept:", key);
        var channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        channel.register(key.selector(), SelectionKey.OP_READ);
    }

    private void handleConnect(SelectionKey key) throws IOException {
        log.info("Connect:", key);
        SocksUtils.getChannel(key).finishConnect();
        SocksUtils.getAttachment(key).finishCouple();
        key.interestOps(0);
    }

    private void handleRead(SelectionKey key) throws IOException {
        var attach = SocksUtils.getAttachment(key);
        if(attach == null){

        }else{
            if(!SocksUtils.tryReadToBuffer(key)){
                log.info("Channel closed, terminating connection:", key);
                close(key);
            }
            switch (attach.getType()){
                case CONN_READ -> {}
                case AUTH_READ -> {}
                default -> {} // usual read
            }
        }
    }

    private void handleWrite(SelectionKey key) {
    }

    private void close(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
        SocksUtils.getAttachment(key).decouple();
    }


}
