package net.hashcode.fsw.workers

import java.io.File
import net.hashcode.fsw.models.FileEntry
import net.hashcode.fsw.models.FileEntryStatus
import net.hashcode.fsw.models.Volume
import net.hashcode.fsw.persistence._
import net.hashcode.fsw.transport._
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger

object Synchronizer {
  val log = Logger.getLogger(Synchronizer.getClass)

  def	syncRemoteChange(remoteEntry:FileEntry, mountPoint:String){
    val local = FileEntry(remoteEntry.filepath, mountPoint)

    if (remoteEntry.deleted ){
      log.info(FileEntryStatus.FileEntryStatusResult(remoteEntry,FileEntryStatus.Removed))
      if (remoteEntry.exists){
        if (remoteEntry.file.isDirectory){
          findChildren(remoteEntry.serverPath, child => remove(child))
          FileUtils.deleteDirectory(remoteEntry.file)
        }else{
          remove(remoteEntry)
          remoteEntry.file.delete
        } 								
      }else{
        log.debug("File removed in the server but didn't find locally "+remoteEntry)
      }
      return
    }
    //it's synced
    if (remoteEntry.isEquals(local)){
      log.info(FileEntryStatus.FileEntryStatusResult(remoteEntry,FileEntryStatus.Synced))
      save(remoteEntry)
      return
    }
    //Remove dir recursively 
    if (remoteEntry.isDirectory){
      val dir = new File(remoteEntry.file.getParentFile, remoteEntry.filename)
      dir.mkdirs
      save(remoteEntry)
      log.info(FileEntryStatus.FileEntryStatusResult(remoteEntry,FileEntryStatus.Synced))
      return
    }
  }

  def	createFile(remoteEntry:FileEntry){

    val tmpFile = Volume.tmpFile(remoteEntry)
    CurrentTransport.download(remoteEntry, tmpFile)
    tmpFile.setLastModified(remoteEntry.localModifiedTime * 1000)
    //ensure that there is a directory to move file
    remoteEntry.file.getParentFile.mkdirs 

    val destination = new File(remoteEntry.file.getParent, remoteEntry.filename)
    FileUtils.copyFile(tmpFile, destination)
    destination.setLastModified(remoteEntry.localModifiedTime * 1000)
    save(remoteEntry)
    log.info(FileEntryStatus.FileEntryStatusResult(remoteEntry,FileEntryStatus.Synced))
    tmpFile.delete
  }

  def	sync(file:File, mountPoint:String){
    log debug "Syncing %s mount %s".format(file,mountPoint)
    val result = FileEntryStatus(file.getAbsolutePath, mountPoint) 
    val status = result.status
    val fileEntry = result.fileEntry
    if (fileEntry == null) return

    log.debug("Compare(%s) exists: %s status: %s fileEntry: %s".format(file,file.exists, status, fileEntry)) 
    if (!fileEntry.isRoot)
      status match {
        case FileEntryStatus.New => {
            val resp = if (fileEntry.isDirectory) create(fileEntry) else upload(fileEntry)
            fileEntry.csum = resp.lastChange.csum 
            fileEntry.id = resp.lastChange.id
            save(fileEntry)
          }
        case FileEntryStatus.Changed => {
            val resp = if (fileEntry.isDirectory) create(fileEntry) else upload(fileEntry)
            fileEntry.csum = resp.lastChange.csum 
            fileEntry.id = resp.lastChange.id
            save(fileEntry)
          }
        case FileEntryStatus.Removed => {										
            remove(fileEntry)
            delete(fileEntry)
          }
        case FileEntryStatus.Synced => None
      }
    log.info(FileEntryStatus.FileEntryStatusResult(fileEntry,FileEntryStatus.Synced))

    //Sync recursively for dirs
    if (fileEntry != null && fileEntry.isDirectory){
      findChildren(fileEntry.serverPath, fe => sync(fe.file, mountPoint))
      if (fileEntry.file != null) {
        val localFiles = fileEntry.file.listFiles
        if (localFiles != null) for(localFile <- localFiles if !localFile.isHidden){ 
          if (FileEntryStatus(localFile.getAbsoluteFile.getAbsolutePath ,mountPoint).status != FileEntryStatus.Synced )
            sync(localFile,mountPoint) 
        }
      }
    }
  }
}