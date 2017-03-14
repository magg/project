package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;


import org.slf4j.*;

public class SigStop extends Injection
{
    private static final int PID_MAX = 4194303;
    private static final Logger LOG;
    private final int pid;
    
    public SigStop(final Faultinjection.Injection injection) {
        super(injection);
        this.pid = injection.getPid();
        this.checkArguments();
    }
    
    public SigStop(final Faultinjection.InjectionAction action, final int pid) {
        super(Faultinjection.InjectionType.SIGSTOP, action);
        this.pid = pid;
    }
    
    public int getPid() {
        return this.pid;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new SigStop(action, this.pid);
    }
    
    @Override
    public String toString() {
        return "SIGSTOP{pid=" + this.pid + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setPid(this.pid);
    }
    
    private void checkArguments() {
        ArgumentChecker.checkArgument(this.pid > 1 && this.pid <= 4194303, "injection not valid.");
    }
    
    static {
        LOG = LoggerFactory.getLogger(SigStop.class);
    }
}
