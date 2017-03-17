package com.inria.spirals.mgonzale.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Exec {
	 private static final Logger LOG;
	    
	    public static void main(final String[] args) {
	        final int pid = 435;
	        final String cmd = "lsof -i -n -P | awk '$2 == \"" + pid + "\" {print $9}'";
	        exec(Integer::valueOf, Collectors.toList(), new String[] { "lsof", "-i", "-n" }).forEach(System.out::println);
	    }
	    
	    public static <R, A> A exec(final Function<? super String, ? extends R> mapper, final Collector<? super R, ?, A> collector, final String... cmd) {
	        try {
	        	
	    		ProcessBuilder pb = new ProcessBuilder(cmd );
					pb.redirectErrorStream(true); 
			        Process proc = pb.start();
	            //final Process proc = Runtime.getRuntime().exec(cmd);
	            final int result = proc.waitFor();
	            if (result != 0) {
	                try (final BufferedReader stream = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
	                    stream.lines().forEach(Exec.LOG::error);
	                }
	                throw new RuntimeException("Cannot run command " + Arrays.toString(cmd));
	            }
	            try (final BufferedReader stream = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
	                return stream.lines().map(mapper).collect(collector);
	            }
	        }
	        catch (InterruptedException | IOException e) {
	            Exec.LOG.error("Cannot run " + Arrays.toString(cmd) + " " + e);
	            throw new RuntimeException(e);
	        }
	    }
	    
	    public static int exec(final StringBuilder stdout, final StringBuilder stderr, final String... cmd) {
	        try {
	        	
	        	
	    		ProcessBuilder pb = new ProcessBuilder(cmd );
				pb.redirectErrorStream(true); 
		        Process proc = pb.start(); 

	            //final Process proc = Runtime.getRuntime().exec(cmd);
	            final int result = proc.waitFor();
	            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
	                reader.lines().forEach(line -> stdout.append(line).append('\n'));
	            }
	            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
	                reader.lines().forEach(line -> stderr.append(line).append('\n'));
	            }
	            return result;
	        }
	        catch (InterruptedException | IOException e) {
	            Exec.LOG.error("Cannot run " + Arrays.toString(cmd) + " " + e);
	            throw new RuntimeException(e);
	        }
	    }
	    
	    static {
	        LOG = LoggerFactory.getLogger(Exec.class);
	    }
}
