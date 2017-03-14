package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import java.util.*;
import org.slf4j.*;

public class Limit extends NetworkCorruptInjection
{
    private static final Logger LOG;
    
    public Limit(final Faultinjection.Injection injection) {
        super(injection);
    }
    
    public Limit(final Faultinjection.InjectionAction action, final List<SocketAddress> addresses, final long amount) {
        super(Faultinjection.InjectionType.LIMIT, action, addresses, amount);
    }
    
    public Limit(final Faultinjection.InjectionAction action, final SocketAddress source, final SocketAddress destination, final long amount) {
        super(Faultinjection.InjectionType.LIMIT, action, source, destination, amount);
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Limit(action, this.getSource(), this.getDestination(), this.getAmount());
    }
    
    static {
        LOG = LoggerFactory.getLogger(Limit.class);
    }
}
