package com.inria.spirals.mgonzale.domain;


import com.inria.spirals.mgonzale.domain.miscinjection.*;

import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public enum Target
{
    //FLOOD(Faultinjection.InjectionType.FLOOD, (Injectable)new DoS()), 
    LOSS(Faultinjection.InjectionType.LOSS, (Injectable)new TC()),  
    CORRUPT(Faultinjection.InjectionType.CORRUPT, (Injectable)new TC()), 
    REORDER(Faultinjection.InjectionType.REORDER, (Injectable)new TC()), 
    DUPLICATE(Faultinjection.InjectionType.DUPLICATE, (Injectable)new TC()), 
    DELAY(Faultinjection.InjectionType.DELAY, (Injectable)new TC()), 
    //LIMIT(Faultinjection.InjectionType.LIMIT, (Injectable)new Tc()), 
    DELETE(Faultinjection.InjectionType.DELETE, (Injectable)new DeleteFiles()),
    DOWN(Faultinjection.InjectionType.DOWN, (Injectable)new DownInterface());


    
    private final Injectable injectable;
    private final Faultinjection.InjectionType type;
    
	private static final Logger LOG = LoggerFactory.getLogger(Target.class);

    
    private Target(final Faultinjection.InjectionType type, final Injectable injectable) {
        this.type = type;
        this.injectable = injectable;
    }
    
    public Faultinjection.InjectionType getInjectionType() {
        return this.type;
    }
    
    public static boolean handle(final Injection injection) throws Throwable {
    	//System.out.println(injection.toString());
    	
        final Target target = Arrays.stream(values()).filter(v -> v.type == injection.getInjection()).findFirst().orElseThrow(IllegalArgumentException::new);
        switch (injection.getAction()) {
        	case START_WAIT_STOP:
        			boolean b = target.injectable.onStart(injection);
        			final int sleepTimeSecInt =injection.getSleep();
        			if (b == true) {
        				
        				try {
     	            	   LOG.info("Sleeping for " + sleepTimeSecInt + " sec");
     	                   TimeUnit.SECONDS.sleep(sleepTimeSecInt);
     	                   //return client.cancel(token);
     	                   return target.injectable.onStop(injection);
     	               }
     	               catch (InterruptedException e) {
     	            	   LOG.info("Action was interrupted", e);
     	                   Thread.currentThread().interrupt();
     	               }
     	               return false;
     	               
        				
        			} else {
        				return false;
        			}

        		
            case START: {
                return target.injectable.onStart(injection);
            }
            case STOP: {
                return target.injectable.onStop(injection);
            }
            default: {
                throw new IllegalArgumentException("Unknown action " + injection.getAction());
            }
        }
    }
}
