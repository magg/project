package com.inria.spirals.mgonzale.model;

import com.inria.spirals.mgonzale.grpc.lib.Faultinjection;

public class TriggerStatus
{
    private final boolean executed;
    private Injection injection;
    private boolean success;
    private String message;
    private boolean executing;
    private boolean completed;
    
    public TriggerStatus(final TriggerRequest request, final boolean executed, final boolean success, final String message) {
        this(request.getInjection(), executed, success, message);
    }
    
    private TriggerStatus(final Injection injection, final boolean executed, final boolean success, final String message) {
        this.injection = injection;
        this.executed = executed;
        this.success = success;
        this.message = message;
    }
    
    TriggerStatus(final Faultinjection.TriggerStatus status) {
        this(Injection.create(status.getInjection()), status.getExecuted(), status.getSuccess(), status.getInfo());
    }
    
    public Injection getInjection() {
        return this.injection;
    }
    
    public void setInjection(final Injection injection) {
        this.injection = injection;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public void setMessage(final String message) {
        this.message = message;
    }
    
    public boolean isExecuted() {
        return this.executed;
    }
    
    public boolean isExecuting() {
        return this.executing;
    }
    
    public void setExecuting(final boolean executing) {
        this.executing = executing;
    }
    
    public boolean isSuccess() {
        return this.success;
    }
    
    public void setSuccess(final boolean success) {
        this.success = success;
    }
    
    public void setCompleted(final boolean completed) {
        this.completed = completed;
    }
    
    @Override
    public String toString() {
        return "TriggerStatus{injection=" + this.injection + ", executed=" + this.executed + ", success=" + this.success + ", message='" + this.message + '\'' + '}';
    }
    
    Faultinjection.TriggerStatus createStatus() {
        return Faultinjection.TriggerStatus.newBuilder().setInjection(this.injection.createInjection()).setExecuted(this.executed).setSuccess(this.success).setInfo(this.message).build();
    }
}

