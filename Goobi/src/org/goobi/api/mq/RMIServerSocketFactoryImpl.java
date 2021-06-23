package org.goobi.api.mq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import javax.net.ServerSocketFactory;

/**
 * taken from: https://vafer.org/blog/20061010091658/
 */

public class RMIServerSocketFactoryImpl implements RMIServerSocketFactory {

    private final InetAddress localAddress;

    public RMIServerSocketFactoryImpl(final InetAddress pAddress) {
        localAddress = pAddress;
    }

    @Override
    public ServerSocket createServerSocket(final int pPort) throws IOException {
        return ServerSocketFactory.getDefault()
                .createServerSocket(pPort, 0, localAddress);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        return obj.getClass().equals(getClass());
    }

    @Override
    public int hashCode() {
        return RMIServerSocketFactoryImpl.class.hashCode();
    }
}
