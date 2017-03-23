package com.inria.spirals.mgonzale.services;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.UUID;
import java.net.InetSocketAddress;
import java.util.Arrays;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.inria.spirals.mgonzale.domain.dto.Failure;
import com.inria.spirals.mgonzale.grpc.lib.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;



@Service
public class DiscoveryService {

	@Autowired
	private DiscoveryClient discoveryClient;

	private static final Logger LOG = LoggerFactory.getLogger(DiscoveryService.class);


	private ConcurrentMap<Injection, UUID> injectionHM = new ConcurrentHashMap<Injection, UUID>();


	@Autowired
	private AgentSessionImpl sessions;

	public List<ServiceInstance> getListofServers(){
		List<ServiceInstance> instances = discoveryClient.getInstances("cloud-grpc-server");
		return instances;
	}




	   private Injection createInjection(Failure f, final Faultinjection.InjectionType target, final Faultinjection.InjectionAction action) {
	       final GrpcClientService client = sessions.getSessions().get(new InetSocketAddress(f.getHost(), f.getPort()));
	       System.out.println("LOL "+client.toString());

	       LOG.info("Creating network injection {}", target);
	       switch (target) {

	       /*
	           case DROP: {
	               return new Drop(action,getAddressBlocks(ip, ports));
	           }
	           case REJECT1: {
	               return new Drop(action,getAddressBlocks(ip, ports));
	           }
	           case REJECT2: {
	               return new Drop(action,getAddressBlocks(ip, ports));
	           }
	         */
	           case LOSS: {
	               return new Loss(action, f.getAmount(), f.getPeriodSec());
	           }
	           case CORRUPT: {
	               return new Corrupt(action, f.getAmount(), f.getPeriodSec());
	           }
	           case DUPLICATE: {
	               return new Duplicate(action, f.getAmount(), f.getPeriodSec());
	           }
	           case REORDER: {
	               return new Reorder(action, f.getAmount(), f.getPeriodSec());
	           }
	           case DELAY: {
	               return new Delay(action, f.getAmount(), f.getPeriodSec());
	           }
	           default: {
	               throw new IllegalArgumentException("Unknown target " + target);
	           }
	       }
	   }


	   private boolean actionHandler(Failure f, final Injection injection) {
		   final Faultinjection.InjectionAction cmd = Faultinjection.InjectionAction.valueOf(f.getCmd());
	       LOG.info(String.format("AgentAction invoked with params host = %s, cmd = %s , injection = %s", f.getHost(), cmd, injection));
	       final GrpcClientService client = sessions.getSessions().get(new InetSocketAddress(f.getHost(), f.getPort()));
	       switch (cmd) {
	           case START: {
	               final UUID token = client.trigger(injection);
	               injectionHM.put(injection, token);
	               if (token == null) {
	            	   LOG.error("Cannot inject {} to {} ", injection, client);
	                   return false;
	               }
	               return true;
	           }
	           case STOP: {
	               final UUID token = injectionHM.remove(injection);
	               if (token == null) {
	            	   LOG.error("Cannot cancel unknown injection {} from {}", injection, client);
	            	   LOG.info("Know injections: {}", injectionHM.keySet());
	                   return false;
	               }
	               return client.cancel(token);
	           }
	           case START_WAIT_STOP: {
	               //final int sleepTimeSecInt =f.getPeriodSec();
	               final UUID token = client.trigger(injection);
	               injectionHM.put(injection, token);
	               if (token == null) {
	            	   LOG.error("Cannot inject {} to {} ", injection, client);
	                   return false;
	               }
	               return true;

	           }
	       }
	       throw new IllegalArgumentException("Unknown command " + cmd);
	   }


	   private Injection createInjection(Failure f) {
	       final Faultinjection.InjectionAction action = Faultinjection.InjectionAction.valueOf(f.getCmd());
	       return this.createInjection(f, action);
	   }

	   private Injection createInjection(Failure f, final Faultinjection.InjectionAction action) {
	       final Faultinjection.InjectionType target = Faultinjection.InjectionType.valueOf(f.getType().toUpperCase());
	       switch (target) {

	           case DELETE: {
	               return new Delete(action, f.getPath(), f.getPeriodSec());
	           }
	           
	           case DOWN: {
	               return new Down(action, f.getIface(), f.getPeriodSec());
	           }
	           case DROP:
	           case LOSS:
	           case CORRUPT:
	           case DUPLICATE:
	           case REORDER:
	           case DELAY: {
	                   return this.createInjection(f, target, action);
	           }
	           default: {
	               throw new IllegalArgumentException("Unknown target " + target);
	           }
	       }
	   }


	   public boolean doAction(final Failure f) throws InterruptedException {

	       return actionHandler(f, createInjection(f));
	   }


	   //@EventListener(ContextRefreshedEvent.class)
	   public void testFail(){
		   
		   
		   sessions.getSessions().entrySet().stream().parallel().forEach(e ->  failScenario(e.getKey()) );
		   
				   
				
	/*	   
		   try {
			   TimeUnit.MINUTES.sleep(2);

			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		
		   for (ServiceInstance instance: sessions){
			   Failure f = new Failure();
			   f.setCmd("START_WAIT_STOP");
			   //f.setAmount(10);
			   f.setPeriodSec(300);
			   //f.setPath("/tmp/TEST/lol");

			   f.setType("DOWN");
			   f.setIface("eth0");
			   //f.setAmount(98);
			   f.setHost(instance.getHost());
			   //f.setProcessName("dropbox");
			   f.setPort(3000);
			   System.out.println(sessions.getSessions().toString());


				try {
				doAction(f);



				} catch (InterruptedException e){
					System.out.println("timeout es : 3");

				}


		   }
*/   
	   }
	   
	   
	   public void failScenario(InetSocketAddress address){
		   
		   Failure f = new Failure();
		   f.setCmd("START_WAIT_STOP");
		   //f.setAmount(10);
		   f.setPeriodSec(300);
		   //f.setPath("/tmp/TEST/lol");

		   f.setType("DOWN");
		   f.setIface("eth0");
		   //f.setAmount(98);
		   f.setHost(address.getAddress().getHostAddress());
		   //f.setProcessName("dropbox");
		   f.setPort(3000);

			try {
			doAction(f);



			} catch (InterruptedException e){
				System.out.println("timeout es : 3");

			}
		   
	   }






}
