package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;

import java.util.Objects;

import org.slf4j.*;

public class Down extends Injection
{
    private static final Logger LOG = LoggerFactory.getLogger(Down.class);
    private final String iface;
    private final int sleep;
    
    public Down(final Faultinjection.Injection injection) {
        super(injection);
        this.iface = injection.getIface();
        this.sleep = injection.getSleep();
        this.checkArguments();
    }
    
    public Down(final Faultinjection.InjectionAction action, final String iface, final int sleep) {
        super(Faultinjection.InjectionType.DOWN, action);
        this.iface = iface;
        this.sleep = sleep;
        this.checkArguments();
    }
    
    public String getIface() {
        return this.iface;
    }
    
    public int getSleep(){
    	return this.sleep;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Down(action, this.iface, this.sleep);
    }
    
    @Override
    public String toString() {
        return "Down{iface=" + this.iface +", sleep="+this.sleep + "} " + super.toString();
    }
    
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.iface, this.sleep);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Down down = (Down)o;
        return this.sleep == down.sleep && Objects.equals(this.iface, down.iface);
    }
    
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setIface(this.iface).setSleep(this.sleep);
    }
    
    private void checkArguments() {
    	Objects.requireNonNull(this.iface, "Interface value not defined.");
    }
    

}

