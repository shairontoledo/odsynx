package net.hashcode.fsw.models
import net.hashcode.fsw.Config
import net.hashcode.fsw.io.Command
import net.hashcode.fsw.io.MrWatcher
import net.hashcode.fsw.persistence.CurrentDatabase
import net.hashcode.fsw.workers.LocalChangesWorker
import net.hashcode.fsw.workers.Synchronizer
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor
import org.apache.log4j.Logger
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer
import scala.collection._
import java.io._
import org.apache.log4j.Logger
import net.hashcode.fsw.transport._


object Volume{
  
  val log = Logger.getLogger(Volume.getClass)
  var mountPoint: String = _
  var name: String = _ 
  var configDir: String = _
  val LocalChangesQueue = mutable.Queue[File]()
  val RemoteChangesQueue = mutable.Queue[FileEntry]()
  val fam = new FilesystemAlterationMonitor
  val localWorker = new LocalChangesWorker(LocalChangesQueue)
  
		def initWithConfig(config:Config , initWorkers:Boolean=true) {
    log.info("Initializing by configuration")
				Volume.configDir = config.configFile.getAbsolutePath
				init(config.remoteFolder, config.mountPoint, initWorkers)
		}
		
  def init(volName: String, volPath: String, initWorkers:Boolean=true) {
    log.info("Initializing [%s]:%s".format(volName, volPath))
    Volume.name = volName
    Volume.mountPoint = volPath
				//TODO remove this out
    Volume.configDir = new File(mountPoint,".odx").getAbsolutePath
    log.debug("Config %s dir exists %s".format(Volume.configDir, new File(Volume.configDir).mkdir))
    CurrentDatabase.init
    
    if (initWorkers){
      fam.addListener(new File(mountPoint), MrWatcher(LocalChangesQueue));
      log.info("Started")
      localWorker.start
    
      fam.start
    }
  }

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
		
		def status(filePath:String) = FileEntryStatus(filePath,mountPoint)
		def status(fe:FileEntry) = FileEntryStatus(fe.filepath,mountPoint)
		def	status:Unit = CurrentDatabase.fetchAll(fe => status(fe))
		def	printStatus(file:File) = {
				
				val all=new HashSet[String]
				entries.foreach(e => all += e.filepath)
				
				CurrentDatabase.fetchAll( e=> if (!all.contains(e.filepath)) all += e.filepath )
				//all.foreach(e => log.info( status(e) )  )
				all.toList.sortBy(_.toString).foreach(e => log.info( status(e) )  )
				//CurrentDatabase.fetchAll(fe => log.info(status(fe)) )
		} 
		
		
		//def	syncAll = CurrentDatabase.fetchAll(sync)
		def	syncAll = {
				
		}
		//def sync(file:File) = Synchronizer.sync(file,mountPoint)
		def push(file:File):Unit = {
				var fe = FileEntry(file.getAbsolutePath, mountPoint)
				if (fe == null){
						val fkey = FileEntry.fkeyForRelativePath(file.getAbsolutePath, mountPoint)
						fe = CurrentDatabase.retrive(fkey)
				}
				if (fe == null){
						log.warn("File not found, skipping it %s".format(file))
				}else{
						push(fe)
						
				}
				
		}
		
		def push(fe:FileEntry):Unit = {
				log.info("Pushing "+fe)
				Synchronizer.sync(fe.file,mountPoint)
		}
		
		def	pull = {
				
				log.info("Pulling")
				val changeset = CurrentTransport.checkRevision()
				log.info("Remote changes "+changeset.files.size)
				for ( fe <- changeset.files){
						fe.mountPoint = mountPoint
						fe.normalized
						log.debug("Normalized remote (%s) %s".format(fe.parentPath, fe))
						//log.info(fe.toStringDebug)
						Synchronizer.syncRemoteChange(fe, mountPoint)
				}
				
		}
  
		def	tmpFile(fe:FileEntry) = new File("%s/tmp/%s".format(Volume.configDir, fe.fkey))
		
		
		
}

