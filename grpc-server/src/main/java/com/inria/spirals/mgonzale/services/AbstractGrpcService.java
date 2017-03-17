package com.inria.spirals.mgonzale.services;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inria.spirals.mgonzale.util.Exec;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import net.devh.springboot.autoconfigure.grpc.server.GrpcService;

@GrpcService(AgenTestGrpc.class)
public abstract class AbstractGrpcService extends AgenTestGrpc.AgenTestImplBase  implements GrpcServerService {
	
	  //private final Server server;
	    protected ActiveTriggers triggers;
	    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	    
	    protected AbstractGrpcService(){//(final int port) throws IOException {
	        this.triggers = new ActiveTriggers();
	        //this.server = ((ServerBuilder)ServerBuilder.forPort(port).addService(this.bindService())).build().start();
	    }
	    
	    /*
	     
	     
	     
	         protected AbstractGrpcService(){
	        this.triggers = new ActiveTriggers();
	        try {
	        	  this.server = ServerBuilder.forPort(port)
			        		.addService(this.bindService())
							.build()
							.start();
	        } catch (IOException e){
	        	
	        }
		      
	    }
	     
	      
	     */
	    
	    private boolean isMac() {
	        final String os = System.getProperty("os.name");
	        LOG.info("OS {}", os);
	        return os.contains("Mac");
	    }
	    
	    public TriggerStatus get(final UUID token) {
	        return this.triggers.get(token);
	    }
	    
	    public TriggerStatus removeInjection(final UUID token) {
	        LOG.info("Removing {}", token);
	        return this.triggers.remove(token);
	    }
	    
	    @Override
	    public final void trigger(final Faultinjection.TriggerRequest request, final StreamObserver<Faultinjection.TriggerResponse> responseObserver) {
	        final TriggerRequest trigger = new TriggerRequest(request);
	        final String token = this.onTrigger(trigger);
	        LOG.info("Trigger {} -> {}", trigger, token);
	        final Faultinjection.TriggerResponse response = Faultinjection.TriggerResponse.newBuilder().setToken(token).build();
	        responseObserver.onNext(response);
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public final void cancel(final Faultinjection.CancelRequest request, final StreamObserver<Faultinjection.CancelResponse> responseObserver) {
	        final CancelRequest cancelRequest = new CancelRequest(request);
	        final boolean done = this.onCancel(cancelRequest);
	        LOG.info("Canceled {} -> {} ", cancelRequest, done);
	        final Faultinjection.CancelResponse response = Faultinjection.CancelResponse.newBuilder().setDone(done).build();
	        responseObserver.onNext(response);
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public final void register(final Faultinjection.EndPointRequest request, final StreamObserver<Faultinjection.EndPointResponse> responseObserver) {
	        final EndpointRequest service = new EndpointRequest(request);
	        final boolean success = this.onRegister(service);
	        LOG.info("Registered {} -> {}", service, success);
	        final Faultinjection.EndPointResponse response = Faultinjection.EndPointResponse.newBuilder().setAccepted(success).build();
	        responseObserver.onNext(response);
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void listTriggers(final Faultinjection.ListTriggerRequest request, final StreamObserver<Faultinjection.ListTriggerResponse> responseObserver) {
	        LOG.info("Listing triggers");
	        final Faultinjection.ListTriggerResponse response = ActiveTriggers.createResponse(this.triggers);
	        responseObserver.onNext(response);
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void cleanup(final Faultinjection.VoidMessage request, final StreamObserver<Faultinjection.VoidMessage> responseObserver) {
	        LOG.info("Cleanup of triggers");
	        this.onCleanup();
	        responseObserver.onNext(Faultinjection.VoidMessage.getDefaultInstance());
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void shutdown(final Faultinjection.VoidMessage request, final StreamObserver<Faultinjection.VoidMessage> responseObserver) {
	        LOG.info("Shutdown requested");
	        this.onShutdown();
	        responseObserver.onNext(Faultinjection.VoidMessage.getDefaultInstance());
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void listPorts(final Faultinjection.IntMessage request, final StreamObserver<Faultinjection.PortsMessage> responseObserver) {
	        final int pid = request.getInt();
	        LOG.info("Listing ports for pid {}", (Object)pid);
	        responseObserver.onNext(Faultinjection.PortsMessage.newBuilder().addAllPorts(this.onPortsList(pid)).build());
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void getPid(final Faultinjection.QueryMessage request, final StreamObserver<Faultinjection.IntMessage> responseObserver) {
	        final String query = request.getQuery();
	        LOG.info("Getting pid using query {} ", query);
	        responseObserver.onNext(Faultinjection.IntMessage.newBuilder().setInt(this.onPidQuery(query)).build());
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void queryPids(final Faultinjection.QueryMessage request, final StreamObserver<Faultinjection.PidsMessage> responseObserver) {
	        final String query = request.getQuery();
	        LOG.info("Getting pids using query {} ", query);
	        responseObserver.onNext(Faultinjection.PidsMessage.newBuilder().addAllPid(this.onPidsQuery(query)).build());
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void getIPAddress(final Faultinjection.VoidMessage request, final StreamObserver<Faultinjection.StringMessage> responseObserver) {
	        LOG.info("Getting the IP Address of this host");
	        responseObserver.onNext(Faultinjection.StringMessage.newBuilder().setText(this.onGetIP()).build());
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public void execCommand(final Faultinjection.StringMessage request, final StreamObserver<Faultinjection.CommandTuple> responseObserver) {
	        final String cmd = request.getText();
	        LOG.info("Running command {}", cmd);
	        responseObserver.onNext(this.onExec(cmd));
	        responseObserver.onCompleted();
	    }
	    
	    @Override
	    public String onTrigger(final TriggerRequest trigger) {
	        final UUID id = UUID.randomUUID();
	        trigger.getInjection();
	        this.triggers.put(id, new TriggerStatus(trigger, false, false, ""));
	        LOG.info("Injecting {}", id);
	        if (this.onInject(id)) {
	            return id.toString();
	        }
	        return null;
	    }
	    
	    @Override
	    public boolean onCancel(final CancelRequest request) {
	        final UUID id = UUID.fromString(request.getToken());
	        LOG.info("Cancelling {}", id);
	        return this.onEject(id);
	    }
	    
	    @Override
	    public boolean onRegister(final EndpointRequest service) {
	        return false;
	    }
	    
	    @Override
	    public void close() {
	        LOG.info("Preparing to close, triggers left to be pruned {}", (Object)this.triggers.count());
	        this.onShutdown();
	        //this.server.shutdown();
	    }
	    
	    protected Faultinjection.CommandTuple onExec(final String command) {
	        final StringBuilder stdout = new StringBuilder();
	        final StringBuilder stderr = new StringBuilder();
	        LOG.info("Running command: '{}'", command);
	        final int exitCode = Exec.exec(stdout, stderr, command.split("\\s"));
	        return Faultinjection.CommandTuple.newBuilder().setOutput(stdout.toString()).setError(stderr.toString()).setExitCode(exitCode).build();
	    }
	    
	    protected String onGetIP() {
	        final String command = this.isMac() ? "hostname" : "hostname -I";
	        final Faultinjection.CommandTuple commandTuple = this.onExec(command);
	        return commandTuple.getOutput().split("\\n")[0];
	    }
	    
	    protected abstract boolean onInject(final UUID token);
	    
	    protected abstract boolean onEject(final UUID token);
	    
	    protected abstract void onCleanup();
	    
	    protected Collection<Integer> onPortsList(final int pid) {
	        return Exec.exec(Integer::valueOf, Collectors.toList(), String.format("lsof -i -n -P | awk '$2 == \"{%d}\" {print $9}'", pid).split("\\s"));
	    }
	    
	    protected int onPidQuery(final String query) {
	        final List<Integer> exec = Exec.exec(Integer::valueOf, Collectors.toList(), query.split("\\s"));
	        return exec.get(0);
	    }
	    
	    protected List<Integer> onPidsQuery(final String query) {
	        return Exec.exec(Integer::valueOf, Collectors.toList(), query.split("\\s"));
	    }
	    
	    protected abstract void onShutdown();

}
