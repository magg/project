package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;


import org.slf4j.*;

public class Delete extends Injection
{
    private static final Logger LOG = LoggerFactory.getLogger(Delete.class);
    private final String path;
    
    public Delete(final Faultinjection.Injection injection) {
        super(injection);
        this.path = injection.getPath();
        this.checkArguments();
    }
    
    public Delete(final Faultinjection.InjectionAction action, final String path) {
        super(Faultinjection.InjectionType.DELETE, action);
        this.path = path;
        this.checkArguments();
    }
    
    public String getPath() {
        return this.path;
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Delete(action, this.path);
    }
    
    @Override
    public String toString() {
        return "Delete{delete=" + this.path + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.setPath(this.path);
    }
    
    private void checkArguments() {
        ArgumentChecker.checkArgument(this.path != null, "Path value should not be null");
    }
    

}

