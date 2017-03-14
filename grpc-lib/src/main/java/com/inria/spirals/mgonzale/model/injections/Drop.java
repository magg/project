package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import java.util.*;
import org.slf4j.*;


public class Drop extends NetworkBlock
{
    private static final Logger LOG;
    
    public Drop(final Faultinjection.Injection injection) {
        super(injection);
    }
    
    public Drop(final Faultinjection.InjectionAction action, final List<AddressBlock> blocks) {
        super(Faultinjection.InjectionType.DROP, action, blocks);
    }
    
    public Drop(final Faultinjection.InjectionAction action, final AddressBlock source, final AddressBlock destination) {
        super(Faultinjection.InjectionType.DROP, action, source, destination);
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Drop(action, this.getSource(), this.getDestination());
    }
    
    static {
        LOG = LoggerFactory.getLogger(Drop.class);
    }
}
