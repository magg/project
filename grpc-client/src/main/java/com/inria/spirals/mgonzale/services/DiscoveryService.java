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
	private DefaultAgenTestSession sessions;

	public List<ServiceInstance> getListofServers(){
		List<ServiceInstance> instances = discoveryClient.getInstances("cloud-grpc-server");
		return instances;
	}

	private List<com.inria.spirals.mgonzale.model.SocketAddress> getAddresses(String ip, Collection<Integer> ports) {
		return ports.stream().map(p -> new com.inria.spirals.mgonzale.model.SocketAddress(ip, p)).collect(Collectors.toList());
	}


	/*
	private List<SocketAddress> getAddresses() {
	    return getListofServers().stream().map(p -> new SocketAddress(p.getHost(), p.getPort())).collect(Collectors.toList());
	}


	   private List<AddressBlock> getAddressBlocks() {
	       Faultinjection.Direction direction = Faultinjection.Direction.valueOf("INPUT");
	       AddressBlock addressBlock = null;
	       Faultinjection.Direction direction2 = null;
	       return getListofServers().stream().map(p -> {
	           new AddressBlock(direction2, new SocketAddress(p.getHost(), p.getPort()));
	           return addressBlock;
	       }).collect(Collectors.toList());
	   }




	   private List<AddressBlock> getAddressBlocks(String ip, Collection<Integer> ports, String dir) {
	       Faultinjection.Direction direction = Faultinjection.Direction.valueOf(dir);
	       AddressBlock addressBlock = null;
	       Faultinjection.Direction direction2 = null;
	       return ports.stream().map(p -> {
	           new AddressBlock(direction, new SocketAddress(ip, p));
	           return addressBlock;
	       }).collect(Collectors.toList());
	   }

	*/
	   private int name2pid(Failure f) {
	       final String findCmd = String.format("pgrep -f -o %s", f.getProcessName());
	       final GrpcClientService agenTestClient = sessions.getSessions().get(new InetSocketAddress(f.getHost(), f.getPort()));
	       return agenTestClient.queryPid(findCmd);
	   }



	   private Injection createInjection(Failure f, final Faultinjection.InjectionType target, final Faultinjection.InjectionAction action, final int pid) {
	       final GrpcClientService client = sessions.getSessions().get(new InetSocketAddress(f.getHost(), f.getPort()));
	       System.out.println("LOL "+client.toString());

	       final String ip = client.getIPAddress();
	       final Collection<Integer> ports = client.listPorts(pid);
	       LOG.info("Creating network injection {} for {} : {}", target, ip, ports);
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
	               return new Loss(action,getAddresses(ip, ports), f.getAmount());
	           }
	           case CORRUPT: {
	               return new Corrupt(action,getAddresses(ip, ports), f.getAmount());
	           }
	           case DUPLICATE: {
	               return new Duplicate(action,getAddresses(ip, ports), f.getAmount());
	           }
	           case REORDER: {
	               return new Reorder(action,getAddresses(ip, ports), f.getAmount());
	           }
	           case DELAY: {
	               return new Delay(action,getAddresses(ip, ports), f.getAmount());
	           }
	           case LIMIT: {
	               return new Limit(action,getAddresses(ip, ports), f.getAmount());
	           }
	           case FLOOD: {
	               //final int port =ensureIntParameter("port", "port is unacceptably null", new Object[0]);
	               return new Flood(action, ip,  1);
	           }
	           default: {
	               throw new IllegalArgumentException("Unknown target " + target);
	           }
	       }
	   }


	   private boolean actionHandler(Failure f, final Injection injection) {
		   final Faultinjection.InjectionAction cmd = Faultinjection.InjectionAction.valueOf(f.getCmd());
	       LOG.info(String.format("AgenTestAction invoked with params host = %s, cmd = %s , injection = %s", f.getHost(), cmd, injection));
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
	               final int sleepTimeSecInt =f.getPeriodSec();
	               final UUID token = client.trigger(injection);
	               injectionHM.put(injection, token);
	               if (token == null) {
	            	   LOG.error("Cannot inject {} to {} ", injection, client);
	                   return false;
	               }
	               try {
	            	   LOG.info("Sleeping for " + sleepTimeSecInt + " sec");
	                   TimeUnit.SECONDS.sleep(sleepTimeSecInt);
	                   return client.cancel(token);
	               }
	               catch (InterruptedException e) {
	            	   LOG.info("Action was interrupted", e);
	                   Thread.currentThread().interrupt();
	               }
	               break;
	           }
	       }
	       throw new IllegalArgumentException("Unknown command " + cmd);
	   }


	   private Injection createInjection(Failure f, int pid) {
	       final Faultinjection.InjectionAction action = Faultinjection.InjectionAction.valueOf(f.getCmd());
	       return this.createInjection(f, action, pid);
	   }

	   private Injection createInjection(Failure f, final Faultinjection.InjectionAction action, final int pid) {
	       final Faultinjection.InjectionType target = Faultinjection.InjectionType.valueOf(f.getType().toUpperCase());
	       switch (target) {
	           case BURNCPU: {
	               return new BurnCPU(action, f.getAmount());
	           }
	           case SIGSTOP: {
	               return new SigStop(action, pid);
	           }
	           case BURNIO: {
	               return new BurnIO(action, f.getAmount(), f.getMountPoint());
	           }
	           case FILLDISK: {
	               return new FillDisk(action, f.getAmount(), f.getMountPoint());
	           }
	           case CORRUPTHDFS: {
	               return new CorruptHDFS(action, f.getSize(), f.getOffset());
	           }
	           case FILLMEM: {
	               return new FillMem(action, f.getAmount());
	           }
	           case RONLY: {
	               return new ReadOnly(action, f.getMountPoint());
	           }
	           case UNMOUNT: {
	               return new UnMount(action, f.getMountPoint());
	           }

	           case DELETE: {
	               return new Delete(action, f.getPath());
	           }

	           case SUICIDE: {
	               return new Suicide(action);
	           }
	           case BLACK: {
	               return new Black(action);
	           }
	           case HANG: {
	               return new Hang(action);
	           }
	           case PANIC: {
	               return new Panic(action);
	           }
	           case DNSFAIL: {
	               return new DnsFail(action);
	           }
	           case DROP:
	           case REJECT1:
	           case REJECT2:
	           case LOSS:
	           case CORRUPT:
	           case DUPLICATE:
	           case REORDER:
	           case DELAY:
	           case LIMIT:
	           case FLOOD: {
	                   return this.createInjection(f, target, action, pid);
	           }
	           case DDELAY: {
	               return new DiskDelay(action, f.getPath(), f.getAccessMode(), f.getDelay(), f.getProbability());
	           }
	           case DCORRUPT: {
	               return new DiskCorrupt(action, f.getPath(), f.getAccessMode(), f.getProbability(), f.getPercentage());
	           }
	           case DFAIL: {
	               return new DiskFail(action, f.getPath(), f.getAccessMode(), f.getProbability(), f.getErrorCode());
	           }
	           default: {
	               throw new IllegalArgumentException("Unknown target " + target);
	           }
	       }
	   }


	   private int getPid(Failure f) {
	       int pid;
	       pid = name2pid(f);
	       return pid;

	   }

	   public boolean doAction(final Failure f) throws InterruptedException {
		   int pid = -1;
		   if (f.getProcessName() != null){
			   pid = getPid(f);
		   }

	       return actionHandler(f, createInjection(f,pid));
	   }


	   @EventListener(ContextRefreshedEvent.class)
	   public void testFail(){

		   for (ServiceInstance instance: getListofServers()){
			   Failure f = new Failure();
			   f.setCmd("START_WAIT_STOP");
			   //f.setAmount(10);
			   f.setPeriodSec(30);
			   f.setPath("/tmp/TEST/lol");

			   f.setType("DELETE");
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

	   }






}
