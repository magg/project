
package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import org.slf4j.*;

public class Flood extends Injection
{
    private static final Logger LOG;
    private final SocketAddress address;
    
    public Flood(final Faultinjection.Injection injection) {
        super(injection);
        this.address = new SocketAddress(injection.getSockets(0));
        ArgumentChecker.checkArgument(this.getPort() > 0 && this.getPort() <= 65535, "Port out of range.");
    }
    
    public Flood(final Faultinjection.InjectionAction action, final String ipAddress, final int port) {
        this(action, new SocketAddress(ipAddress, port));
    }
    
    public Flood(final Faultinjection.InjectionAction action, final SocketAddress address) {
        super(Faultinjection.InjectionType.FLOOD, action);
        this.address = address;
        ArgumentChecker.checkArgument(this.getPort() > 0 && this.getPort() <= 65535, "Port out of range.");
    }
    
    public SocketAddress getAddress() {
        return this.address;
    }
    
    public String getIpAddress() {
        return this.address.getHost();
    }
    
    public int getPort() {
        return this.address.getPort();
    }
    
    @Override
    public Injection setAction(final Faultinjection.InjectionAction action) {
        return new Flood(action, this.getAddress());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.address);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Flood)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Flood flood = (Flood)o;
        return Objects.equals(this.address, flood.address);
    }
    
    @Override
    public String toString() {
        return "Flood{address=" + this.address + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.addAllSockets(Collections.singleton(this.address.create()));
    }
    
    static {
        LOG = LoggerFactory.getLogger(Flood.class);
    }
}
