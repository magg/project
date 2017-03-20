package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;


import org.slf4j.*;

public class Down extends Injection
{
    private static final Logger LOG = LoggerFactory.getLogger(Down.class);
    private final String iface;
    
    public Down(final Faultinjection.Injection injection) {
        super(injection);
        this.iface = injection.getIface();
        this.checkArguments();
    }
    
    public Down(final Faultinjection.InjectionAction action, final String iface) {
        super(Faultinjection.InjectionType.DOWN, action);
        this.iface = iface;
        this.checkArguments();
    }
    
    public String getIface() {
        return this.iface;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Down(action, this.iface);
    }
    
    @Override
    public String toString() {
        return "Down{down=" + this.iface + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setIface(this.iface);
    }
    
    private void checkArguments() {
        ArgumentChecker.checkArgument(this.iface != null, "iface value should not be null");
    }
    

}

