package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import org.slf4j.*;

public class Reject2 extends NetworkBlock
{
    private static final Logger LOG;
    
    public Reject2(final Faultinjection.Injection injection) {
        super(injection);
    }
    
    public Reject2(final Faultinjection.InjectionAction action, final List<AddressBlock> blocks) {
        super(Faultinjection.InjectionType.REJECT2, action, blocks);
    }
    
    public Reject2(final Faultinjection.InjectionAction action, final AddressBlock source, final AddressBlock destination) {
        super(Faultinjection.InjectionType.REJECT2, action, source, destination);
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Reject2(action, this.getSource(), this.getDestination());
    }
    
    static {
        LOG = LoggerFactory.getLogger(Reject2.class);
    }
}