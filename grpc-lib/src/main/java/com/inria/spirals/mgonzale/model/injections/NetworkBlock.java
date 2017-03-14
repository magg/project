
package com.inria.spirals.mgonzale.model.injections;


import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.function.*;
import java.util.stream.*;
import java.util.*;
import org.slf4j.*;

public abstract class NetworkBlock extends Injection
{
    private static final Logger LOG;
    private final List<AddressBlock> addressBlocks;
    
    protected NetworkBlock(final Faultinjection.Injection injection) {
        super(injection);
        this.addressBlocks = injection.getBlocksList().stream().map(AddressBlock::new).collect(Collectors.toList());
    }
    
    protected NetworkBlock(final Faultinjection.InjectionType type, final Faultinjection.InjectionAction action, final List<AddressBlock> addressBlocks) {
        super(type, action);
        this.addressBlocks = addressBlocks;
        this.checkArguments();
    }
    
    protected NetworkBlock(final Faultinjection.InjectionType type, final Faultinjection.InjectionAction action, final AddressBlock source, final AddressBlock destination) {
        this(type, action, Arrays.asList(source, destination));
    }
    
    public List<AddressBlock> getAddressBlocks() {
        return this.addressBlocks;
    }
    
    public AddressBlock getDestination() {
        return this.addressBlocks.get(1);
    }
    
    public AddressBlock getSource() {
        return this.addressBlocks.get(0);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.addressBlocks);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetworkBlock)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final NetworkBlock that = (NetworkBlock)o;
        return Objects.equals(this.addressBlocks, that.addressBlocks);
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{, addressBlocks=" + this.addressBlocks + "} " + super.toString();
    }
    
    @Override
    protected Faultinjection.Injection.Builder build(final Faultinjection.Injection.Builder builder) {
        return builder.addAllBlocks(this.addressBlocks.stream().map(AddressBlock::create).collect(Collectors.toList()));
    }
    
    private void checkArguments() {
    }
    
    public static List<AddressBlock> createSocketAddress(final List<String> addressBlocks) {
        return addressBlocks.stream().map(AddressBlock::fromString).collect(Collectors.toList());
    }
    
    public static List<AddressBlock> createBlocksFromStrings(final String... addressBlocks) {
        return Arrays.stream(addressBlocks).map(AddressBlock::new).collect(Collectors.toList());
    }
    
    private static List<String> createAddresses(final List<AddressBlock> addressBlocks) {
        return addressBlocks.stream().map(AddressBlock::asString).collect(Collectors.toList());
    }
    
    static {
        LOG = LoggerFactory.getLogger(NetworkBlock.class);
    }
}
