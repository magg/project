package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import org.slf4j.*;

public class Reorder extends NetworkCorruptInjection
{
    private static final Logger LOG;
    
    public Reorder(final Faultinjection.Injection injection) {
        super(injection);
    }
    
    public Reorder(final Faultinjection.InjectionAction action, final List<SocketAddress> addresses, final long amount) {
        super(Faultinjection.InjectionType.REORDER, action, addresses, amount);
    }
    
    public Reorder(final Faultinjection.InjectionAction action, final SocketAddress source, final SocketAddress destination, final long amount) {
        super(Faultinjection.InjectionType.REORDER, action, source, destination, amount);
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Reorder(action, this.getSource(), this.getDestination(), this.getAmount());
    }
    
    static {
        LOG = LoggerFactory.getLogger(Reorder.class);
    }
}
