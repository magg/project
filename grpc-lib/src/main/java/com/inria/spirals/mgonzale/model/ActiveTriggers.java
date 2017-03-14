package com.inria.spirals.mgonzale.model;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

import com.inria.spirals.mgonzale.grpc.lib.*;


public class ActiveTriggers
{
    private Map<UUID, TriggerStatus> map;
    
    public ActiveTriggers() {
        this.map = new ConcurrentHashMap<UUID, TriggerStatus>();
    }
    
    public ActiveTriggers(final Faultinjection.ListTriggerResponse response) {
        this.initializeActiveTriggers(response.getTriggersMap());
    }
    
    public void put(final UUID id, final TriggerStatus triggerStatus) {
        this.map.put(id, triggerStatus);
    }
    
    public TriggerStatus get(final UUID id) {
        return this.map.get(id);
    }
    
    public Stream<Map.Entry<UUID, TriggerStatus>> get(final Predicate<? super Map.Entry<UUID, TriggerStatus>> filter) {
        return this.map.entrySet().stream().filter(filter);
    }
    
    public TriggerStatus remove(final UUID id) {
        return this.map.remove(id);
    }
    
    public void conditionalRemove(final Predicate<? super Map.Entry<UUID, TriggerStatus>> predicate) {
        this.map.entrySet().removeIf(predicate);
    }
    
    public int count() {
        return this.map.size();
    }
    
    @Override
    public String toString() {
        return "ActiveTriggers{map=" + this.map + '}';
    }
    
    private void initializeActiveTriggers(final Map<String, Faultinjection.TriggerStatus> active) {
        this.map = active.entrySet().stream().collect(Collectors.toConcurrentMap(e -> UUID.fromString(e.getKey()), e -> new TriggerStatus(e.getValue())));
    }
    
    public static Faultinjection.ListTriggerResponse createResponse(final ActiveTriggers triggers) {
        return Faultinjection.ListTriggerResponse.newBuilder().putAllTriggers((Map<String, Faultinjection.TriggerStatus>)triggers.map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().createStatus()))).build();
    }
}
