package com.inria.spirals.mgonzale.domain.miscinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.io.IOException;
import java.nio.file.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

public class Disk implements Injectable
{
	
	 @Value("${hd.path}")
	    private static Path LOL;

    private static final Path HCDI_FILE;
    private static final Logger LOG;
    private static final int MAX_RETRY = 1;
    private static final String HCDIFILE = "";
    
    
    public String getFilePath(){
    	String path = null;
		try {

		path =  new ClassPathResource("scripts/inject.sh").getFile().getAbsolutePath();
			
		} catch (IOException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
    	
    }
    
    @Override
    public boolean onStart(final Injection injection) {
        switch (injection.getInjection()) {
            case BURNIO: {
                return this.burn((BurnIO)injection);
            }
            case FILLDISK: {
                return this.fill((FillDisk)injection);
            }
            case RONLY: {
                return this.readonly((ReadOnly)injection);
            }
            case UNMOUNT: {
                return this.unmount((UnMount)injection);
            }
            case DDELAY: {
                return this.ddelay((DiskDelay)injection);
            }
            case DCORRUPT: {
                return this.dcorrupt((DiskCorrupt)injection);
            }
            case DFAIL: {
                return this.dfail((DiskFail)injection);
            }
            default: {
                throw new IllegalArgumentException("Do now know how to handle " + injection.getInjection());
            }
        }
    }
    
    @Override
    public boolean onStop(final Injection injection) {
        switch (injection.getInjection()) {
            case BURNIO: {
                return this.unburn((BurnIO)injection);
            }
            case FILLDISK: {
                return this.unfill((FillDisk)injection);
            }
            case RONLY: {
                return this.unreadonly((ReadOnly)injection);
            }
            case UNMOUNT: {
                return this.mount((UnMount)injection);
            }
            case DDELAY: {
                return this.unddelay((DiskDelay)injection);
            }
            case DCORRUPT: {
                return this.undcorrupt((DiskCorrupt)injection);
            }
            case DFAIL: {
                return this.undfail((DiskFail)injection);
            }
            default: {
                throw new IllegalArgumentException("Do now know how to handle " + injection.getInjection());
            }
        }
    }
    
    private boolean burn(final BurnIO injection) {
        try {
            Disk.LOG.info("Starting BurnDISK injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "-i", Long.toString(injection.getAmount()), injection.getMountPoint());
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unburn(final BurnIO injection) {
        try {
            Disk.LOG.info("Stopping BurnDISK injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "--cleanio", injection.getMountPoint());
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean fill(final FillDisk injection) {
        try {
            Disk.LOG.info("Starting FILLDISK injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "-f", injection.getAmount(), injection.getMountPoint());
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unfill(final FillDisk injection) {
        try {
            Disk.LOG.info("Stopping FillDISK injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "--cleanfill", injection.getMountPoint());
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean readonly(final ReadOnly injection) {
        try {
            Disk.LOG.info("Starting RONLY injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "-r", injection.getMountPoint());
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unreadonly(final ReadOnly injection) {
        try {
            Disk.LOG.info("Stopping RONLY injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "--cleanreadonly ", injection.getMountPoint());
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unmount(final UnMount injection) {
        try {
            Disk.LOG.info("Starting unMOUNT injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "-u", injection.getMountPoint());
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean mount(final UnMount injection) {
        try {
            Disk.LOG.info("Stopping unMOUNT injection {}", injection);
            return Util.runScriptPrintRetry(1, getFilePath(), "--cleanunmount", injection.getMountPoint());
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean ddelay(final DiskDelay injection) {
        try {
            Disk.LOG.info("Starting DDELAY injection {}", injection);
            return true;
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean unddelay(final DiskDelay injection) {
        try {
            Disk.LOG.info("Stopping DDELAY injection");
            final String line = "Sleep:" + this.getCanonicalName(injection.getPath()) + ":" + injection.getAccessMode() + ":" + injection.getProbability() + ":" + injection.getDelay();
            return Util.runCmdPrintRetry(1, ("sed --in-place '/" + line + "/d' " + "").split("\\s"));
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean dcorrupt(final DiskCorrupt injection) {
        try {
            Disk.LOG.info("Starting DCORRUPT injection {}", injection);
            return Util.runCmdPrintRetry(1, ("echo \"Corruptor:" + this.getCanonicalName(injection.getPath()) + ":" + injection.getAccessMode() + ":" + injection.getProbability() + ":" + injection.getPercentage() + "\" >> " + "").split("\\s"));
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean undcorrupt(final DiskCorrupt injection) {
        try {
            Disk.LOG.info("Stopping DCORRUPT injection {}", injection);
            Disk.LOG.info("Stopping DCORRUPT injection");
            final String line = "Corruptor:" + this.getCanonicalName(injection.getPath()) + ":" + injection.getAccessMode() + ":" + injection.getProbability() + ":" + injection.getPercentage();
            return Util.runCmdPrintRetry(1, ("sed --in-place '/" + line + "/d' " + "").split("\\s"));
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean dfail(final DiskFail injection) {
        try {
            Disk.LOG.info("Starting DFAIL injection");
            return Util.runCmdPrintRetry(1, "echo \"FailedOp:" + this.getCanonicalName(injection.getPath()) + ":" + injection.getAccessMode() + ":" + injection.getProbability() + ":" + injection.getError() + "\" >> " + "");
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private boolean undfail(final DiskFail injection) {
        try {
            Disk.LOG.info("Stopping DFAIL injection");
            final String line = "FailedOp:" + this.getCanonicalName(injection.getPath()) + ":" + injection.getAccessMode() + ":" + injection.getProbability() + ":" + injection.getError();
            return Util.runCmdPrintRetry(1, ("sed --in-place '/" + line + "/d' " + "").split("\\s"));
        }
        catch (Exception e) {
            Disk.LOG.error("Exception", e);
            return false;
        }
    }
    
    private String getCanonicalName(final String path) {
        return path.replaceAll(".", "/");
    }
    
    static {
        HCDI_FILE = LOL;
        LOG = LoggerFactory.getLogger(Disk.class);
    }
}
