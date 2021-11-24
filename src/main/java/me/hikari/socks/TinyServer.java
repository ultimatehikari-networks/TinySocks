package me.hikari.socks;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class TinyServer {
    private final Integer port;
    private Selector selector;
    private final Map<SocketChannel, SocketChannel> pairs = new HashMap<>();

    /**
     * runs until stopped
     */
    public void start() throws IOException {
        this.selector = Selector.open();

        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress("localhost", port));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            selector.select();
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isAcceptable()) {
                    registerClient(server);
                }
                if(key.isReadable()){
                    handleClient(key);
                    //TODO dns channel
                }
            }
        }
    }

    public void registerClient(ServerSocketChannel channel) throws IOException {
        var client = channel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    public void handleClient(SelectionKey client){

    }

    public void openConnection(){

    }
}
