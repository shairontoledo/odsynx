package net.hashcode.fsw.workers

import java.io.File
import net.hashcode.fsw.models._
import net.hashcode.fsw.persistence.CurrentDatabase
import org.apache.log4j.Logger
import scala.collection.mutable.Queue
import net.hashcode.fsw.workers.Synchronizer._

class LocalChangesWorker(localQueue: Queue[File], mountPath:String) extends Thread with Runnable {
  val queue = localQueue
  val log = Logger.getLogger(classOf[LocalChangesWorker])
  val mountPoint = mountPath
  def work = run

  override def run = {
    log.info("Started for %s".format(mountPoint))

    while(true){
      try{
        var entry:File = queue.dequeue
        sync(entry, mountPoint)

      }catch{
        case ex: NoSuchElementException => Thread sleep 1000
        case ex:Exception =>{
            log.error("Unknown error "+ex)
            if (log.isDebugEnabled) ex.printStackTrace
          }
      }

    }
  }
}
