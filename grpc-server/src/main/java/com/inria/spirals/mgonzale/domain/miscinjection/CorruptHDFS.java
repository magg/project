package com.inria.spirals.mgonzale.domain.miscinjection;

import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.grpc.lib.*;

import java.util.*;
import java.io.*;
import org.apache.commons.logging.*;

public class CorruptHDFS implements Injectable
{
    private static final Log log;
    
    @Override
    public boolean onStart(final Injection injection) {
        return this.corrupt((com.inria.spirals.mgonzale.model.injections.CorruptHDFS)injection);
    }
    
    @Override
    public boolean onStop(final Injection injection) {
        return false;
    }
    
    private boolean corrupt(final com.inria.spirals.mgonzale.model.injections.CorruptHDFS injection) {
        return this.corrupt(this.getFileToCorrupt(injection.getSize(), injection.getOffset()), injection.getSize(), injection.getOffset());
    }
    
    private boolean corrupt(final String fileToCorrupt, final long sizePar, final long offsetPar) {
        try {
            final String targetFile = (fileToCorrupt == null) ? this.getFileToCorrupt(sizePar, offsetPar) : fileToCorrupt;
            if (targetFile == null) {
                CorruptHDFS.log.error("Unable to find HDFS block address or block files. Check if HDFS is running or the node is NameNode.");
                return false;
            }
            final RandomAccessFile tmp = new RandomAccessFile(targetFile, "r");
            tmp.seek(offsetPar);
            final byte[] buf = new byte[(int)sizePar * 1024];
            final int count = tmp.read(buf, 0, (int)sizePar * 1024);
            tmp.seek(0L);
            tmp.close();
            if (count == -1) {
                CorruptHDFS.log.error("The file chosen is smaller than the corruption size (" + sizePar + " Kbytes)");
                return false;
            }
            Arrays.fill(buf, 0, count, (byte)5);
            final RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
            raf.seek(offsetPar);
            raf.write(buf, 0, count);
            raf.seek(0L);
            raf.close();
            return true;
        }
        catch (Exception e) {
            CorruptHDFS.log.error(e);
            return false;
        }
    }
    
    private String getFileToCorrupt(final long sizePar, final long offsetPar) {
        try {
            final ArrayList<String> addresses = new ArrayList<String>();
            try (final BufferedReader outputCommand = Util.runCmdAndReturnOutput("/bin/sh", "-c", "ls -ld $(find /data*/dfs/dn/current -size +" + (sizePar + offsetPar) + "k) | grep -i 'blk' | grep -v 'meta' | awk '{print $9}'")) {
                if (outputCommand == null || !outputCommand.ready()) {
                    return null;
                }
                String line;
                while ((line = outputCommand.readLine()) != null) {
                    addresses.add(line);
                }
            }
            catch (Util.CommandException e) {
                CorruptHDFS.log.error("Failed running command", e);
                return null;
            }
            final int index = new Random().nextInt(addresses.size());
            return addresses.get(index);
        }
        catch (IOException e2) {
            CorruptHDFS.log.error(e2);
            return null;
        }
    }
    
    static {
        log = LogFactory.getLog(CorruptHDFS.class);
    }
}
