package com.inria.spirals.mgonzale.model;

import com.inria.spirals.mgonzale.grpc.lib.Faultinjection;


public class CancelRequest
{
    private final String token;
    
    public CancelRequest(final Faultinjection.CancelRequest cancelRequest) {
        this(cancelRequest.getToken());
    }
    
    private CancelRequest(final String token) {
        this.token = token;
    }
    
    public String getToken() {
        return this.token;
    }
    
    public Faultinjection.CancelRequest createRequest() {
        return Faultinjection.CancelRequest.newBuilder().setToken(this.token).build();
    }
    
    @Override
    public String toString() {
        return "CancelRequest{token='" + this.token + '\'' + '}';
    }
}
