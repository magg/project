package com.inria.spirals.mgonzale.services;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;


@Component
public class InjectionService extends AbstractGrpcService {
	
	
	private static final Logger LOG = LoggerFactory.getLogger(InjectionService.class);
	
	@Autowired
	@Qualifier(value = "taskExecutor")
	private TaskExecutor taskExecutor;
	private InjectionWorker executor;
	
	
	 @Value("${agent.dryRun}")
	 private boolean dryRun;
	 @Value("${agent.timeout}")
	 private long timeout;
	
	@EventListener(ContextRefreshedEvent.class)
	public void initAgent() {	
		System.out.println("timeout es: " +timeout);
		executor = new InjectionWorker(this,dryRun, timeout);
		FutureTask<Object> task = new FutureTask<Object> (executor);
		taskExecutor.execute(task);
	}
	
	 @Override
	    public boolean onRegister(final EndpointRequest service) {
	        return true;
	    }
	    
	    @Override
	    protected boolean onInject(final UUID token) {
	        this.executor.putInjection(token);
	        return true;
	    }
	    
	    @Override
	    protected boolean onEject(final UUID token) {
	        this.executor.invertIfStarted(token);
	        return true;
	    }
	    
	    @Override
	    protected void onCleanup() {
	        while (this.triggers.count() > 0) {
	            LOG.info("Cleanup {} injections remaining", (Object)this.triggers.count());
	            try {
	                synchronized (this) {
	                    this.triggers.get(e -> e.getValue().getInjection().getAction() == Faultinjection.InjectionAction.START).forEach(e -> this.executor.invertIfStarted(e.getKey()));
	                    this.wait();
	                }
	            }
	            catch (InterruptedException e2) {
	                LOG.error("Cleanup interrupted", e2);
	            }
	        }
	    }
	    
	    @Override
	    protected void onShutdown() {
	        LOG.info("Shutdown requested");
	        this.onCleanup();
	        this.executor.stopExecutor();
	        LOG.info("Shutdown completed");
	    }
	           

}
