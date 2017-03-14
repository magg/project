package com.inria.spirals.mgonzale.model;

import java.util.*;
import org.slf4j.*;
import com.inria.spirals.mgonzale.grpc.lib.*;


public class AddressBlock
{
    private static final Logger LOG;
    private final Faultinjection.Direction direction;
    private final SocketAddress address;
    
    public AddressBlock(final Faultinjection.AddressBlock block) {
        this.address = new SocketAddress(block.getAddress());
        this.direction = block.getDirection();
        this.checkArguments();
    }
    
    public AddressBlock(final Faultinjection.Direction direction, final SocketAddress address) {
        this.direction = direction;
        this.address = address;
        this.checkArguments();
    }
    
    public AddressBlock(final String string) {
        final String[] split = string.split(":");
        this.direction = Faultinjection.Direction.valueOf(split[0]);
        this.address = new SocketAddress(split[1], Integer.valueOf(split[2]));
        this.checkArguments();
    }
    
    public SocketAddress getAddress() {
        return this.address;
    }
    
    public Faultinjection.Direction getDirection() {
        return this.direction;
    }
    
    public String getHost() {
        return this.address.getHost();
    }
    
    public String getHostName() {
        return this.address.getAddress().getHostName();
    }
    
    public int getPort() {
        return this.address.getPort();
    }
    
    public Faultinjection.AddressBlock create() {
        return Faultinjection.AddressBlock.newBuilder().setAddress(this.address.create()).setDirection(this.direction).build();
    }
    
    public String asString() {
        return this.getDirection().name() + ":" + this.address.asString();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.direction, this.address);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddressBlock)) {
            return false;
        }
        final AddressBlock that = (AddressBlock)o;
        return this.direction == that.direction && Objects.equals(this.address, that.address);
    }
    
    @Override
    public String toString() {
        return "AddressBlock{direction=" + this.direction + ", address=" + this.address + '}';
    }
    
    private void checkArguments() {
    }
    
    public static AddressBlock fromString(final String string) {
        return new AddressBlock(string);
    }
    
    static {
        LOG = LoggerFactory.getLogger(AddressBlock.class);
    }
}

