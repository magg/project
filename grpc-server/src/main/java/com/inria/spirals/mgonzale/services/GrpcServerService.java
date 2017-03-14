package com.inria.spirals.mgonzale.services;

import com.inria.spirals.mgonzale.model.*;

public interface GrpcServerService extends AutoCloseable
	{
	    String onTrigger(final TriggerRequest trigger);
	    
	    boolean onCancel(final CancelRequest request);
	    
	    boolean onRegister(final EndpointRequest service);
	}


