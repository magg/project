package com.inria.spirals.mgonzale.domain.netinjection;

import java.net.*;
import java.io.*;
import org.apache.commons.logging.*;

class DoSRunner extends Socket implements Runnable
{
    private static final Log log;
    private final int port;
    
    DoSRunner(final int port) {
        this.port = port;
    }
    
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final Socket net = new Socket("127.0.0.1", this.port);
                sendRawLine("GET / HTTP/1.1", net);
                continue;
            }
            catch (UnknownHostException e) {
                DoSRunner.log.error(e);
            }
            catch (ConnectException ex) {
                continue;
            }
            catch (SocketException e2) {
                DoSRunner.log.error("Client says too many files open. This is expected in a DoS attack. Continuing...");
                DoSRunner.log.error(e2);
                continue;
            }
            catch (IOException e3) {
                DoSRunner.log.error(e3);
            }
            break;
        }
    }
    
    private static void sendRawLine(final String text, final Socket sock) {
        try {
            final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
            out.write(text);
            out.flush();
        }
        catch (IOException e) {
            DoSRunner.log.error(e);
        }
    }
    
    static {
        log = LogFactory.getLog(DoSRunner.class);
    }
}
