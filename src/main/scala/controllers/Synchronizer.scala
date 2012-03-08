package net.hashcode.fsw.workers

import java.io.File
import net.hashcode.fsw.models.FileEntry
import net.hashcode.fsw.models.FileEntryStatus
import net.hashcode.fsw.models.Volume

import net.hashcode.fsw.persistence.CurrentDatabase
import net.hashcode.fsw.persistence._

import net.hashcode.fsw.transport._
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger

object Synchronizer {
		val log = Logger.getLogger(Synchronizer.getClass)
		
		def	syncRemoteChange(remoteEntry:FileEntry, mountPoint:String){
				val local = FileEntry(remoteEntry.filepath, mountPoint)
//				if (local != null) log.info("local : %s".format(local.toStringDebug)) else log.info("local : null")
//				if (remoteEntry != null) log.info("remote: %s".format(remoteEntry.toStringDebug)) else log.info("remote: null")
//				
				if (remoteEntry.isEquals(local)){
						log.info(FileEntryStatus.FileEntryStatusResult(remoteEntry,FileEntryStatus.Synced))
						save(remoteEntry)
						return
				}
				if (remoteEntry.isDirectory){
						val dir = new File(remoteEntry.file.getParentFile, remoteEntry.filename)
						dir.mkdirs
						
						save(remoteEntry)
						log.info(FileEntryStatus.FileEntryStatusResult(remoteEntry,FileEntryStatus.Synced))
						return
				}
				val tmpFile = Volume.tmpFile(remoteEntry)
				CurrentTransport.download(remoteEntry, tmpFile)
				tmpFile.setLastModified(remoteEntry.localModifiedTime * 1000)
				remoteEntry.file.getParentFile.mkdirs
				val destination = new File(remoteEntry.file.getParent, remoteEntry.filename)
				FileUtils.copyFile(tmpFile, destination)
				destination.setLastModified(remoteEntry.localModifiedTime * 1000)
				save(remoteEntry)
				log.info(FileEntryStatus.FileEntryStatusResult(remoteEntry,FileEntryStatus.Synced))
				
		}
		
		def	sync(file:File, mountPoint:String){
				
				val result = FileEntryStatus(file.getAbsolutePath,mountPoint) 
				val status = result.status
				val fileEntry = result.fileEntry
				log.debug("Compare(%s) exists: %s status: %s fileEntry: %s".format(file,file.exists, status, fileEntry)) 
				
				if (!fileEntry.isRoot)
				status match {
						case FileEntryStatus.New => {
										val resp = if (fileEntry.isDirectory) create(fileEntry) else upload(fileEntry)
										fileEntry.csum = resp.lastChange.csum 
										save(fileEntry)
								}
						case FileEntryStatus.Changed => {
										val resp = if (fileEntry.isDirectory) create(fileEntry) else upload(fileEntry)
										log.debug("Changed1 \nRE{%s} \nFE{%s}".format(resp.lastChange.toStringDebug, fileEntry.toStringDebug))
										fileEntry.csum = resp.lastChange.csum 
										//	fileEntry.size = 333333
										log.debug("Changed2 \nRE{%s} \nFE{%s}".format(resp.lastChange.toStringDebug, fileEntry.toStringDebug))
										save(fileEntry)
											
								}
						case FileEntryStatus.Removed => {										
										remove(fileEntry)
										delete(fileEntry)
								}
						case FileEntryStatus.Synced => None
				}
				log.info(FileEntryStatus.FileEntryStatusResult(fileEntry,FileEntryStatus.Synced))
				
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
