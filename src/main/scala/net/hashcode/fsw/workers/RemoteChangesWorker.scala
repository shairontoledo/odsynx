package net.hashcode.fsw.workers

import net.hashcode.fsw.models.Volume
import org.apache.log4j.Logger

object RemoteChangesWorker extends Thread with Runnable{
		val log = Logger.getLogger(RemoteChangesWorker.getClass)
		
		override def run = {
    log.info("Remote Changes: Started")
    
    while(true){
      try{
										Volume.pull
										Thread sleep 1000 * 30
      }catch{
								case ex:Exception => {
												log.error("Unknown error "+ex)
												if (log.isDebugEnabled) ex.printStackTrace
								}
      }
    }
  }
}
