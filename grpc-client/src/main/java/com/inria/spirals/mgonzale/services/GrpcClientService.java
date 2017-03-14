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
public class GrpcClientService implements ApplicationListener<ApplicationReadyEvent> {

    @GrpcClient("cloud-grpc-server")
    private Channel serverChannel;
    private String address = null;
    private int port;
    private ManagedChannel channel;
    
    private AgenTestGrpc.AgenTestBlockingStub stub;
    
    public GrpcClientService() {
    	super();
    }
    
    public GrpcClientService(final String address, final int port) {
        this.address = address;
        this.port = port;
        this.channel = ManagedChannelBuilder.forAddress(this.address, this.port).usePlaintext(true).build();
        this.stub = AgenTestGrpc.newBlockingStub(channel);
        
    }
    
    
    public GrpcClientService(final InetSocketAddress address) {
        this(address.getHostName(), address.getPort());
    }
 
    
    /**
    
    public String getAddress() {
        return this.address;
    }**/
    
    @Override
	public void onApplicationEvent(ApplicationReadyEvent event) {	
		System.out.println("timeout es: ");
		//serverChannel.
		
		
        //this.stub = AgenTestGrpc.newBlockingStub(serverChannel);

	}
    
    public String getIPAddress() {
        final Faultinjection.StringMessage response = this.stub.getIPAddress(Faultinjection.VoidMessage.getDefaultInstance());
        return response.getText();
    }
  
    /**
    public int getPort() {
        return this.port;
    }**/
    
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
    
    public Collection<Integer> listPorts(final int pid) {
        final Faultinjection.IntMessage request = Faultinjection.IntMessage.newBuilder().setInt(pid).build();
        final Faultinjection.PortsMessage response = this.stub.listPorts(request);
        return response.getPortsList();
    }
    
    public int queryPid(final String query) {
        final Faultinjection.QueryMessage queryMessage = Faultinjection.QueryMessage.newBuilder().setQuery(query).build();
        final Faultinjection.IntMessage response = this.stub.getPid(queryMessage);
        return response.getInt();
    }
    
    public CommandTuple runCommand(final String command) {
        final Faultinjection.StringMessage cmd = Faultinjection.StringMessage.newBuilder().setText(command).build();
        final Faultinjection.CommandTuple commandTuple = this.stub.execCommand(cmd);
        return new CommandTuple(commandTuple);
    }
    
    public List<Integer> queryPids(final String query) {
        final Faultinjection.QueryMessage queryMessage = Faultinjection.QueryMessage.newBuilder().setQuery(query).build();
        final Faultinjection.PidsMessage response = this.stub.queryPids(queryMessage);
        return response.getPidList();
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
  
    /**
    @Override
    public String toString() {
        return "AgenTestClient{address='" + this.address + '\'' + ", port=" + this.port + '}';
    }**/
    
    public static class CommandTuple
    {
        private final int exitCode;
        private final String stderr;
        private final String stdout;
        
        public CommandTuple(final Faultinjection.CommandTuple commandTuple) {
            this.stdout = commandTuple.getOutput();
            this.stderr = commandTuple.getError();
            this.exitCode = commandTuple.getExitCode();
        }
        
        public int getExitCode() {
            return this.exitCode;
        }
        
        public String getStderr() {
            return this.stderr;
        }
        
        public String getStdout() {
            return this.stdout;
        }
    }

}
