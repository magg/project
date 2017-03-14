package com.inria.spirals.mgonzale.model;

import com.inria.spirals.mgonzale.grpc.lib.Faultinjection;


public class EndpointRequest
{
    private final String address;
    private final int port;
    private final String kind;
    
    public EndpointRequest(final Faultinjection.EndPointRequest request) {
        this(request.getKind(), request.getAddress(), request.getPort());
    }
    
    private EndpointRequest(final String kind, final String address, final int port) {
        this.address = address;
        this.port = port;
        this.kind = kind;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public String getKind() {
        return this.kind;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public Faultinjection.EndPointRequest createRequest() {
        return Faultinjection.EndPointRequest.newBuilder().setAddress(this.address).setPort(this.port).setKind(this.kind).build();
    }
    
    @Override
    public String toString() {
        return "DefaultEndpointService{address='" + this.address + '\'' + ", port=" + this.port + ", kind='" + this.kind + '\'' + '}';
    }
}

