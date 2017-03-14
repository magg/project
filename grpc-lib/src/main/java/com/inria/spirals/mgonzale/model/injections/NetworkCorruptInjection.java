package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.util.ArgumentChecker;

;
import java.util.function.*;
import java.util.stream.*;
import java.util.*;
import org.slf4j.*;

public abstract class NetworkCorruptInjection extends Injection
{
    private static final Logger LOG;
    private final List<SocketAddress> addresses;
    private final long amount;
    
    protected NetworkCorruptInjection(final Faultinjection.Injection injection) {
        super(injection);
        this.addresses = injection.getSocketsList().stream().map(SocketAddress::new).collect(Collectors.toList());
        this.amount = injection.getAmount();
        this.checkArguments();
    }
    
    protected NetworkCorruptInjection(final Faultinjection.InjectionType type, final Faultinjection.InjectionAction action, final List<SocketAddress> addresses, final long amount) {
        super(type, action);
        this.addresses = addresses;
        this.amount = amount;
        this.checkArguments();
    }
    
    protected NetworkCorruptInjection(final Faultinjection.InjectionType type, final Faultinjection.InjectionAction action, final SocketAddress source, final SocketAddress destination, final long amount) {
        this(type, action, Arrays.asList(source, destination), amount);
    }
    
    public List<SocketAddress> getAddresses() {
        return this.addresses;
    }
    
    public long getAmount() {
        return this.amount;
    }
    
    public SocketAddress getDestination() {
        return this.addresses.get(1);
    }
    
    public SocketAddress getSource() {
        return this.addresses.get(0);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), new ArrayList<SocketAddress>(this.addresses), this.amount);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetworkCorruptInjection)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final NetworkCorruptInjection that = (NetworkCorruptInjection)o;
        return this.amount == that.amount && Objects.equals(this.addresses, that.addresses);
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{, addresses=" + this.addresses + ", amount=" + this.amount + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.addAllSockets(this.addresses.stream().map(SocketAddress::create).collect(Collectors.toList())).setAmount(this.amount);
    }
    
    private List<SocketAddress> createSocketAddress(final List<String> portsList) {
        return portsList.stream().map(SocketAddress::new).collect(Collectors.toList());
    }
    
    private List<String> createAddresses(final List<SocketAddress> portsList) {
        return portsList.stream().map(SocketAddress::asString).collect(Collectors.toList());
    }
    
    private void checkArguments() {
        ArgumentChecker.checkArgument(this.addresses.size() > 0, "There needs to be a source and potentially destination address defined");
    }
    
    public static List<SocketAddress> addresses(final String... addresses) {
        return Arrays.stream(addresses).map(SocketAddress::new).collect(Collectors.toList());
    }
    
    static {
        LOG = LoggerFactory.getLogger(NetworkCorruptInjection.class);
    }
}
