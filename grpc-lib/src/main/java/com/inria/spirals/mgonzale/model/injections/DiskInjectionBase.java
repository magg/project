package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import org.slf4j.*;

public abstract class DiskInjectionBase extends Injection
{
    private static final Logger LOG;
    private final String path;
    private final String accessMode;
    
    public DiskInjectionBase(final Faultinjection.InjectionType type, final Faultinjection.InjectionAction action, final String path, final String accessMode) {
        super(type, action);
        this.path = path;
        this.accessMode = accessMode;
        this.checkMode();
    }
    
    public DiskInjectionBase(final Faultinjection.Injection injection) {
        super(injection);
        this.path = injection.getPath();
        this.accessMode = injection.getAccess();
        this.checkMode();
    }
    
    public String getAccessMode() {
        return this.accessMode;
    }
    
    public String getPath() {
        return this.path;
    }
    
    @Override
    public String toString() {
        return "DiskInjectionBase{path='" + this.path + '\'' + ", accessMode='" + this.accessMode + '\'' + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setPath(this.path).setAccess(this.accessMode);
    }
    
    private void checkMode() {
        if (this.accessMode == null || !this.accessMode.matches("^[RWOC]{1,4}$")) {
            throw new IllegalArgumentException("Access mode is not valid.");
        }
    }
    
    static {
        LOG = LoggerFactory.getLogger(DiskInjectionBase.class);
    }
}
