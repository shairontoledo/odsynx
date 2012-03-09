package net.hashcode.fsw.io
import net.hashcode.fsw.persistence._
import net.hashcode.fsw.models._
import java.io.File
import org.apache.commons.jci.monitor._
import org.apache.log4j.Logger
import scala.collection.mutable.Queue
object MrWatcher{
  
  def apply(changesQueue: Queue[File]) = new MrWatcher(changesQueue)
  def allows(file:File):Boolean = {
    var relative = FileEntry.relativePath(file.getAbsolutePath, Volume.mountPoint)
    return (!file.isHidden && (!relative.trim.matches("^/*\\.odx.*")))
            
  }
}

class MrWatcher(changesQueue: Queue[File]) extends FilesystemAlterationListener  {
  val log = Logger.getLogger(classOf[MrWatcher])
  val queue = changesQueue
		
		log.info("Filesystem Listener: Started")
		
  override def onDirectoryChange(dir:File) = didChange(dir)
  override def onDirectoryCreate(dir:File) = didChange(dir)
  override def onDirectoryDelete(file:File) = didChange(file)
           
  override def onFileChange(file:File) = didChange(file)  
  override def onFileCreate(file:File) = didChange(file)
  override def onFileDelete(file:File) = didChange(file)
           
  override def onStart(pObserver: FilesystemAlterationObserver) = {}
  override def onStop(pObserver: FilesystemAlterationObserver)= {}
  
  def didChange(file:File) = {
    if (MrWatcher allows file ){
      log.debug("Changed(q: %-5s) %s".format(queue size, file))
      queue += file
    }
  }
}
