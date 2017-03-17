package com.inria.spirals.mgonzale.services;

import java.net.InetSocketAddress;

public interface AgenTestSession
{
    GrpcClientService get(final InetSocketAddress address);
    
    GrpcClientService get(final String host, final int port);
        
    void terminate();
    
    void terminate(final InetSocketAddress address);
    
    void terminate(final String host, final int port);
    
}
