package net.hashcode.fsw.models
import net.hashcode.fsw.io.MrWatcher
import net.hashcode.fsw.persistence.CurrentDatabase
import net.hashcode.fsw.workers.LocalChangesWorker
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor
import org.apache.log4j.Logger
import scala.collection.mutable.ListBuffer
import scala.collection._
import java.io._
import org.apache.log4j.Logger

object Volume{
  
  val log = Logger.getLogger(Volume.getClass)
  var mountPoint: String = _
  var name: String = _ 
  var configDir: String = _
  val LocalChangesQueue = mutable.Queue[File]()
  val RemoteChangesQueue = mutable.Queue[FileEntry]()
  val fam = new FilesystemAlterationMonitor
  val localWorker = new LocalChangesWorker(LocalChangesQueue)
  def init(volName: String, volPath: String) {
    log.info("Initializing [%s]:%s".format(volName, volPath))
    Volume.name = volName
    Volume.mountPoint = volPath
    Volume.configDir = new File(mountPoint,".odsync").getAbsolutePath
    log.debug("Config %s dir exists %s".format(Volume.configDir, new File(Volume.configDir).mkdir))
    CurrentDatabase.init
    
    fam.addListener(new File(mountPoint), MrWatcher(LocalChangesQueue));
    log.info("Started")
    localWorker.start
    
    fam.start
    
    
    
    
    //CurrentDatabase.init(mountPoint)
  }
  
  //def init = apply(_,_)
  
  def entries = {
    val list = new ListBuffer[FileEntry]
    fetchFiles(mountPoint, mountPoint, list)
    list.toList

  }
  
  def fetchFiles(path:String, mountPoint: String, list:ListBuffer[FileEntry] ){
  
    for (file <- new File(path).listFiles if !file.isHidden){
      list += FileEntry(file.getAbsolutePath, mountPoint)
      if (file.isDirectory)
        fetchFiles(file.getAbsolutePath, mountPoint, list)
      file.getAbsolutePath
    } 
  }
  
}

