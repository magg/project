package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import java.util.*;
import org.slf4j.*;

public class Corrupt extends NetworkCorruptInjection
{
    private static final Logger LOG;
    
    public Corrupt(final Faultinjection.Injection injection) {
        super(injection);
    }
    
    public Corrupt(final Faultinjection.InjectionAction action, final List<SocketAddress> addresses, final long amount) {
        super(Faultinjection.InjectionType.CORRUPT, action, addresses, amount);
    }
    
    public Corrupt(final Faultinjection.InjectionAction action, final SocketAddress source, final SocketAddress destination, final long amount) {
        super(Faultinjection.InjectionType.CORRUPT, action, source, destination, amount);
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Corrupt(action, this.getSource(), this.getDestination(), this.getAmount());
    }
    
    static {
        LOG = LoggerFactory.getLogger(Corrupt.class);
    }
}
