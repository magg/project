package com.inria.spirals.mgonzale.services;

import java.net.InetSocketAddress;

public interface AgenTestSession
{
    GrpcClientService get(final InetSocketAddress address);
    
    GrpcClientService get(final String host, final int port);
    
    GrpcClientService get(final String host);
    
    void terminate();
    
    void terminate(final InetSocketAddress address);
    
    void terminate(final String host, final int port);
    
    void terminate(final String host);
}
