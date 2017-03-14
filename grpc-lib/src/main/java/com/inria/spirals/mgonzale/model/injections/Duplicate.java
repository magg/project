package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import org.slf4j.*;

public class Duplicate extends NetworkCorruptInjection
{
    private static final Logger LOG;
    
    public Duplicate(final Faultinjection.Injection injection) {
        super(injection);
    }
    
    public Duplicate(final Faultinjection.InjectionAction action, final List<SocketAddress> addresses, final long amount) {
        super(Faultinjection.InjectionType.DUPLICATE, action, addresses, amount);
    }
    
    public Duplicate(final Faultinjection.InjectionAction action, final SocketAddress source, final SocketAddress destination, final long amount) {
        super(Faultinjection.InjectionType.DUPLICATE, action, source, destination, amount);
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Duplicate(action, this.getSource(), this.getDestination(), this.getAmount());
    }
    
    static {
        LOG = LoggerFactory.getLogger(Duplicate.class);
    }
}
