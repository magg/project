package com.inria.spirals.mgonzale.model;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.inria.spirals.mgonzale.grpc.lib.*;


public class SocketAddress
{
    private static final Logger LOG;
    private final InetSocketAddress address;
    
    public SocketAddress(final Faultinjection.SocketAddress socketAddress) {
        this.address = InetSocketAddress.createUnresolved(socketAddress.getHostAddress(), socketAddress.getPort());
    }
    
    public SocketAddress(final String host, final int port) {
        this.address = InetSocketAddress.createUnresolved(host, port);
    }
    
    public SocketAddress(final String address) {
        final String[] split = address.split(":");
        this.address = InetSocketAddress.createUnresolved(split[0], Integer.parseInt(split[1]));
    }
    
    public InetSocketAddress getAddress() {
        return this.address;
    }
    
    public String getHost() {
        return this.address.getHostString();
    }
    
    public int getPort() {
        return this.address.getPort();
    }
    
    public Faultinjection.SocketAddress create() {
        return Faultinjection.SocketAddress.newBuilder().setHostAddress(this.address.getHostString()).setPort(this.address.getPort()).build();
    }
    
    public String asString() {
        return this.getHost() + ":" + this.getPort();
    }
    
    @Override
    public String toString() {
        return "SocketAddress{address=" + this.address + '}';
    }
    
    static {
        LOG = LoggerFactory.getLogger(SocketAddress.class);
    }
}

