package me.hikari;

import lombok.extern.log4j.Log4j2;
import me.hikari.socks.TinyServer;

import java.io.IOException;

@Log4j2
public class App
{
    private static final Integer MIN_PORT = 1024;
    private static final Integer MAX_PORT = 65535;

    private static Integer parsePort(String portString){
        var port = Integer.parseInt(portString);
        if(port < MIN_PORT || port > MAX_PORT){
            log.error("Incorrect port");
            System.exit(-1);
        }
        return port;
    }

    public static void main( String[] args ) throws IOException {
        if(args.length < 1){
            log.error("TinySOCKSServer args : port");
        }
        var port = parsePort(args[0]);

        log.info( "Tiny server starting on port" );
        TinyServer server = new TinyServer(port);
        server.start();
    }
}
