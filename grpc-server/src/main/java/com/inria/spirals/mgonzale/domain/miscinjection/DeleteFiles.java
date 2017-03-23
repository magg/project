package com.inria.spirals.mgonzale.domain.miscinjection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.inria.spirals.mgonzale.domain.*;
import com.inria.spirals.mgonzale.support.*;
import com.inria.spirals.mgonzale.model.*;
import com.inria.spirals.mgonzale.model.injections.*;
import com.inria.spirals.mgonzale.grpc.lib.*;


public class DeleteFiles implements Injectable {
	
    private static final Log log = LogFactory.getLog(DeleteFiles.class);



	@Override
	public boolean onStart(Injection injection) {
		return move((Delete)injection);
	}

	@Override
	public boolean onStop(Injection injection) {
		return restore((Delete)injection);
	}
	
	public boolean move(Delete injection) {
		
        final String path = injection.getPath();
        log.info("Starting DELETE injection");
        return Util.runCmdPrintRetry(1, ("mv "+path+" /tmp").split("\\s"));

		
	}
	
	public boolean restore(Delete injection){
		String[] paths = injection.getPath().split("/");
		String dst =  paths[paths.length -1];
        //log.info("sdt "+ dst);

		StringBuilder tmp = new StringBuilder();
		tmp.append("/");
		for (int i = 0; i < paths.length-1; i++){
			if (paths[i] != null && !paths[i].isEmpty()){
				tmp.append(paths[i]);
				tmp.append("/");
			}

			
		}
        //log.info("tmp "+ tmp.toString());        
		
        return Util.runCmdPrintRetry(1, ("mv /tmp/"+ dst +" " + tmp.toString()).split("\\s"));

	}

}
