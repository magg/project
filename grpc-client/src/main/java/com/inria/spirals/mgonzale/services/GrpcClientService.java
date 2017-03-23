package com.inria.spirals.mgonzale.services;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.FutureTask;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.model.*;

import net.devh.springboot.autoconfigure.grpc.client.GrpcClient;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * User: Michael
 * Email: yidongnan@gmail.com
 * Date: 2016/11/8
 */
@Service
public class GrpcClientService  {

    @GrpcClient("cloud-grpc-server")
    private Channel serverChannel;
    private String address = null;
    private int port;
    
    private AgentGrpc.AgentBlockingStub stub;
    
    public GrpcClientService() {
    	super();
    }
    
    public GrpcClientService(final String address, final int port) {
        this.address = address;
        this.port = port;
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(this.address, this.port).usePlaintext(true).build();
        this.stub = AgentGrpc.newBlockingStub(channel);
        
    }
    
    
    public GrpcClientService(final InetSocketAddress address) {
        this(address.getAddress().getHostAddress(), address.getPort());
    }
    
    public UUID trigger(final Injection injection) {
        final Faultinjection.TriggerResponse response = this.stub.trigger(new TriggerRequest(injection).createRequest());
        final String token = response.getToken();
        return (token == null) ? null : UUID.fromString(token);
    }
    
    public boolean cancel(final UUID token) {
        final Faultinjection.CancelRequest request = Faultinjection.CancelRequest.newBuilder().setToken(token.toString()).build();
        final Faultinjection.CancelResponse response = this.stub.cancel(request);
        return response.getDone();
    }
    
    public ActiveTriggers listInjections() {
        final Faultinjection.ListTriggerRequest request = Faultinjection.ListTriggerRequest.getDefaultInstance();
        final Faultinjection.ListTriggerResponse response = this.stub.listTriggers(request);
        return new ActiveTriggers(response);
    }
    
    
    public boolean register(final EndpointRequest service) {
        final Faultinjection.EndPointResponse response = this.stub.register(service.createRequest());
        return response.getAccepted();
    }
    
    public void cleanup() {
        this.stub.cleanup(Faultinjection.VoidMessage.getDefaultInstance());
    }
    
    public void shutdownServer() {
        this.stub.shutdown(Faultinjection.VoidMessage.getDefaultInstance());
    }
  

}
