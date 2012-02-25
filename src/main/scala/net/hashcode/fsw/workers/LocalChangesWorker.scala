package net.hashcode.fsw.workers

import java.io.File
import net.hashcode.fsw.models._
import net.hashcode.fsw.persistence.CurrentDatabase
import org.apache.log4j.Logger
import scala.collection.mutable.Queue

class LocalChangesWorker(localQueue: Queue[File]) extends Thread{
  val queue = localQueue
  val log = Logger.getLogger(classOf[LocalChangesWorker])
  
  def work = run
    
  override def run = {
    log.info("Started 2")
    
    while(true){
      try{
        var entry = queue dequeue
        var result = FileEntryStatus(entry.getAbsolutePath, Volume.mountPoint) 
        log.debug("Dequeue q:%-4s s:%-7s %s".format(queue.size, result.status, result.fileEntry))
//         result.status match {
//           case FileEntryStatus.New => println("foo")
//           case _ => println("bar")
//         }
        
        if (result.status == FileEntryStatus.New){
          result.fileEntry.checksum = "HARDCODED"
          CurrentDatabase.save(result.fileEntry)
          
        }

      }catch{
        case ex: NoSuchElementException => Thread sleep 1000
      }
      
    }
  }
}
