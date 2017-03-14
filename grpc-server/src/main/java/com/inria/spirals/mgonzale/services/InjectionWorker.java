package com.inria.spirals.mgonzale.services;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.domain.Target;
import com.inria.spirals.mgonzale.grpc.lib.*;



@Component
@Scope("prototype")
public class InjectionWorker implements Callable<Object> {
	
    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private BlockingQueue<UUID> injectionsQueue;
    private long timeout;
    private long POLL_FREQUENCY = 250L;
    private boolean dryRun;
    private volatile boolean isRunning;
    private InjectionService service;
    
    InjectionWorker( InjectionService service, boolean dryRun, long timeout) {
    	this.service = service;
        this.injectionsQueue = new LinkedBlockingQueue<UUID>();
        this.isRunning = false;
        this.dryRun = dryRun;
        this.timeout = timeout;
    }
    
    public UUID getInjection() throws InterruptedException {
        final UUID token = this.injectionsQueue.poll(250L, TimeUnit.MILLISECONDS);
        if (token == null) {
            return null;
        }
        return token;
    }

	@Override
	public  Object call() {
		 this.isRunning = true;
	        long counter = 0L;
	        LOG.debug("Starting Executor!");
	        TriggerStatus injectionStatus = null;
	        while (this.isRunning) {
	            try {
	                final UUID id = this.getInjection();
	                if (id == null) {
	                	/**
	                    if (counter > this.timeout) {
	                        LOG.info("AgenTEST did not receive any command in the last " + counter + "ms. Maybe the node is not reachable. Shutting down AgenTEST.");
	                        System.exit(0);
	                    }**/
	                    counter += 250L;
	                }
	                else {
	                    injectionStatus = this.service.get(id);
	                    injectionStatus.setExecuting(true);
	                    counter = 0L;
	                    final Injection inj = injectionStatus.getInjection();
	                    if (inj.getAction() == Faultinjection.InjectionAction.STOP) {
	                        this.service.removeInjection(id);
	                    }
	                    LOG.info("Dealing with: " + injectionStatus.toString());
	                    boolean result = false;
	                    if (this.dryRun) {
	                        LOG.info("Dry run: {}", inj);
	                        result = true;
	                    }
	                    else {
	                        try {
								result = Target.handle(inj);
							} catch (Throwable e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                    }
	                    injectionStatus.setSuccess(result);
	                }
	            }
	            catch (InterruptedException e2) {
	                this.isRunning = false;
	            }
	            catch (IllegalArgumentException | UnsupportedOperationException e) {
	                LOG.error("Caught exception", e);
	                if (injectionStatus != null) {
	                    injectionStatus.setMessage(e.getMessage());
	                    injectionStatus.setSuccess(false);
	                }
	            }
	            finally {
	                if (injectionStatus != null) {
	                    injectionStatus.setCompleted(true);
	                }
	                synchronized (this.service) {
	                    this.service.notifyAll();
	                }
	            }
	        }
	        
	        return null;
	}
	
    void invertIfStarted(final UUID token) {
        final boolean remove = this.injectionsQueue.remove(token);
        if (remove) {
            return;
        }
        final TriggerStatus triggerStatus = this.service.get(token);
        if (triggerStatus != null) {
            try {
                final Injection injection = triggerStatus.getInjection().invert();
                triggerStatus.setExecuting(false);
                triggerStatus.setSuccess(false);
                triggerStatus.setCompleted(false);
                triggerStatus.setInjection(injection);
                this.injectionsQueue.put(token);
            }
            catch (InterruptedException e) {
                LOG.error("Cannot invert injection {}: {}", triggerStatus, e);
            }
        }
    }
    
    void stopExecutor() {
        this.isRunning = false;
    }
    
    void putInjection(final UUID token) {
        this.injectionsQueue.offer(token);
    }
	
}
