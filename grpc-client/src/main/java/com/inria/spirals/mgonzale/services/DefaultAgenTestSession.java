package com.inria.spirals.mgonzale.services;

import java.net.InetSocketAddress;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;


@Service
public class DefaultAgenTestSession implements AgenTestSession
{
    private static final Logger LOG;
    private static ConcurrentMap<InetSocketAddress, GrpcClientService> sessions;

    @Autowired
    private DiscoveryService discover;


    @Scheduled(fixedDelay=30000)    
    public void findServers(){
    	System.out.println("test-22222");
    	for(ServiceInstance instance : discover.getListofServers()){
    		instance.getMetadata().keySet().forEach(key -> System.out.println(key + "->" + instance.getMetadata().get(key)));
    		
    		GrpcClientService client = get(instance.getHost(), 3000);
    	}
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


    static {
        LOG = LoggerFactory.getLogger(DefaultAgenTestSession.class);
        DefaultAgenTestSession.sessions = new ConcurrentHashMap<InetSocketAddress, GrpcClientService>();
    }
}
