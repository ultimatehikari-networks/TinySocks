package me.hikari.socks;

import lombok.extern.log4j.Log4j2;
import org.xbill.DNS.Message;
import org.xbill.DNS.Section;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;

@Log4j2
public class DnsUtils {
    public static void read(SelectionKey key) throws IOException {
        log.info(key);

        var attach = SocksUtils.getAttachment(key);

        var message = new Message(attach.getIn().array());
        var maybeRecord = message.getSection(Section.ANSWER).stream().findAny();
        if (maybeRecord.isPresent()) {
            var addr = InetAddress.getByName(maybeRecord.get().rdataToString());
            log.info("Resolved: " + maybeRecord.get().rdataToString());

            SocksUtils.getAttachment(attach.getCoupled()).couple(addr, attach.getPort(), attach.getCoupled());
            SocksUtils.getAttachment(attach.getCoupled()).setType(Type.READ);
            key.interestOps(0);
            SocksUtils.partiallyClose(key);
        } else {
            log.warn(message.toString());
            SocksUtils.close(key);
            throw new RuntimeException("Host cannot be resolved");
        }
    }

    public static void write(SelectionKey key) {
        log.info(key);

        SocksUtils.clearOut(key);
        key.interestOpsOr(SelectionKey.OP_READ);
        key.interestOpsAnd(~SelectionKey.OP_WRITE);
        SocksUtils.getAttachment(key).setType(Type.DNS_READ);
    }
}
