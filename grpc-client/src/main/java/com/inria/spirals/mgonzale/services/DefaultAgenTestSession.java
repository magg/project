package com.inria.spirals.mgonzale.services;

import java.net.InetSocketAddress;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;

import java.util.concurrent.*;

public class DefaultAgenTestSession implements AgenTestSession
{
    private static final Logger LOG;
    private static ConcurrentMap<InetSocketAddress, GrpcClientService> sessions;
    private final int port;
    
    @Autowired
    private DiscoveryService discover;
    
    
    
    public void findServers(){
    	for(ServiceInstance instance : discover.getListofServers()){
    		GrpcClientService client = new GrpcClientService(instance.getHost(), instance.getPort());
    	}
    	
    }
    
    
    private int getClientPort() {
        return this.port;
    }
    
    public DefaultAgenTestSession(final int port) {
        this.port = port;
    }
    
    @Override
    public GrpcClientService get(final InetSocketAddress address) {
    	GrpcClientService client = DefaultAgenTestSession.sessions.get(address);
        if (client == null) {
            DefaultAgenTestSession.LOG.info("Create new session {}", address);
            final GrpcClientService prev = DefaultAgenTestSession.sessions.putIfAbsent(address, client = new GrpcClientService(address));
            client = ((prev == null) ? client : prev);
        }
        return client;
    }
    
    @Override
    public GrpcClientService get(String host, int port) {
        return this.get(new InetSocketAddress(host, port));
    }
    
    @Override
    public GrpcClientService get(final String host) {
        return this.get(new InetSocketAddress(host, this.getClientPort()));
    }
    
    @Override
    public void terminate() {
        DefaultAgenTestSession.sessions.keySet().forEach(this::terminate);
    }
    
    @Override
    public void terminate(final InetSocketAddress address) {
        final GrpcClientService client = DefaultAgenTestSession.sessions.remove(address);
        if (client == null) {
            DefaultAgenTestSession.LOG.info("No client at {}", address);
            return;
        }
        DefaultAgenTestSession.LOG.info("Terminating {}", client);
        client.shutdownServer();
    }
    
    public ConcurrentMap<InetSocketAddress, GrpcClientService> getSessions(){
    	return sessions;
    }
    
    @Override
    public void terminate(final String host, final int port) {
        this.terminate(new InetSocketAddress(host, port));
    }
    
    @Override
    public void terminate(final String host) {
        this.terminate(host, this.getClientPort());
    }
    
    static {
        LOG = LoggerFactory.getLogger(DefaultAgenTestSession.class);
        DefaultAgenTestSession.sessions = new ConcurrentHashMap<InetSocketAddress, GrpcClientService>();
    }
}

