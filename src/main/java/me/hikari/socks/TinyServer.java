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

    private void handleRead(SelectionKey key) throws Exception {
        var attach = SocksUtils.getAttachment(key);
        if(attach == null){
            prepareAttachment(key);
        }else{
            if(!SocksUtils.tryReadToBuffer(key)){
                log.info("Channel closed, terminating connection:", key);
                close(key);
            }
            switch (attach.getType()){
                case AUTH_READ -> replySocksAuth(key);
                case CONN_READ -> replySocksConn(key);
                case DNS_READ -> DnsUtils.read(key);
                default -> prepareCoupledWrite(key);
            }
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        var attach = SocksUtils.getAttachment(key);
        switch(attach.getType()){
            case AUTH_WRITE -> authWrite(key);
            case DNS_WRITE -> DnsUtils.write(key);
            default -> prepareCoupledRead(key);
        }
    }

    private void prepareAttachment(SelectionKey key) {
        log.info("Preparing attachment for client");
        key.attach(new Attachment(Type.AUTH_READ));
    }

    private void prepareCoupledWrite(SelectionKey key) throws IOException {
        // couple exists 'cause of conn earlier in switch
        // disable read + enable coupled read
        log.info("Preparing coupled to write");
        SocksUtils.clearIn(key);
        SocksUtils.getAttachment(key).addCoupledWrite();
        key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
    }

    private void prepareCoupledRead(SelectionKey key) throws IOException {
        if(SocksUtils.isDecoupled(key)){
            close(key);
            return;
        }
        // disable write + enable coupled read
        log.info("Preparing coupled to read");
        SocksUtils.clearOut(key);
        SocksUtils.getAttachment(key).addCoupledRead();
        key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
    }

    private void authWrite(SelectionKey key) {
        SocksUtils.clearOut(key);
        key.interestOps(SelectionKey.OP_READ);
        SocksUtils.getAttachment(key).setType(Type.CONN_READ);
    }

    private void replySocksConn(SelectionKey key) {
        // TODO: stub
    }

    private void replySocksAuth(SelectionKey key) throws Exception {
        var at = SocksUtils.getAttachment(key);

        if(SocksMessage.inTooSmall(key)){
            log.warn("auth: buffer too small; waiting");
            return;
        }
        if(SocksMessage.wrongVersion(key)){
            // yea i hate other users of proxy, wdya
            throw new Exception("Wrong version, SOCKS5 expected");
        }
        if(!SocksMessage.methodsReceived(key)){
            log.warn("auth: waiting for additional methods");
            return;
        }
        if(SocksMessage.noAuthNotFound(key)){
            // hate intensifies
            throw new Exception("auth: noAuth method not found");
        }

        SocksMessage.putNoAuthResponse(key);
        SocksUtils.getAttachment(key).setType(Type.AUTH_WRITE);
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void close(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
        SocksUtils.getAttachment(key).decouple();
    }


}
