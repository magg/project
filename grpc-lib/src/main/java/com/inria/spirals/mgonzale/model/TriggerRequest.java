package com.inria.spirals.mgonzale.model;

import com.inria.spirals.mgonzale.grpc.lib.Faultinjection;


public class TriggerRequest
{
    private final Injection injection;
    
    public TriggerRequest(final Injection injection) {
        this.injection = injection;
    }
    
    public TriggerRequest(final Faultinjection.TriggerRequest request) {
        this(Injection.create(request.getInjection()));
    }
    
    public Injection getInjection() {
        return this.injection;
    }
    
    public Faultinjection.TriggerRequest createRequest() {
        return Faultinjection.TriggerRequest.newBuilder().setInjection(this.injection.createInjection()).build();
    }
    
    @Override
    public String toString() {
        return "TriggerRequest{injection=" + this.injection + '}';
    }
}
