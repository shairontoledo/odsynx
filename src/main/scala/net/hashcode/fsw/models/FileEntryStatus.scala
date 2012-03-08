
package net.hashcode.fsw.models


import net.hashcode.fsw.persistence.CurrentDatabase
import org.apache.log4j.Logger

object FileEntryStatus extends Enumeration{
		val log = Logger.getLogger(FileEntryStatus.getClass)
  type Status = Value
  val New, Removed, Synced, Changed = Value


  case class FileEntryStatusResult(fileEntry: FileEntry, status: FileEntryStatus.Value){
				override def toString = "[%-7s] %s".format(status,fileEntry)
		}
  
		def	compare(localEntry:FileEntry, dbEntry:FileEntry, updateEntry:Boolean = false):FileEntryStatusResult ={
				log.debug("STATUS: \nPH{ %s } \nDB{ %s }".format(localEntry, dbEntry))
				
    //new file
    if (dbEntry == null && localEntry != null && localEntry.exists){
						log.debug("NEW \nPH{ %s } \nDB{ %s }".format(localEntry.toStringDebug, null))
      return FileEntryStatusResult(localEntry,FileEntryStatus.New)
    }
    
    //file has been removed
    if (localEntry == null &&  dbEntry != null && dbEntry.csum != null){
						log.debug("REMOVED \nPH{ %s } \nDB{ %s }".format(null, dbEntry.toStringDebug))
      return FileEntryStatusResult(dbEntry,FileEntryStatus.Removed)
    }
    
    if (localEntry != null && localEntry.exists && dbEntry != null){
      //synced file
      if (localEntry.localModifiedTime == dbEntry.localModifiedTime && localEntry.size == dbEntry.size && dbEntry.csum != null){
								log.debug("SYNCED \nPH{ %s } \nDB{ %s }".format(localEntry.toStringDebug, dbEntry.toStringDebug))
        return FileEntryStatusResult(dbEntry,FileEntryStatus.Synced)
      }
      
						//synced dir
      if (localEntry.isDirectory && dbEntry.csum != null){
								log.debug("SYNCED \nPH{ %s } \nDB{ %s }".format(localEntry.toStringDebug, dbEntry.toStringDebug))
        return FileEntryStatusResult(dbEntry,FileEntryStatus.Synced)
      }
      
      //New but still not synced, if true, duplicate action
      if (localEntry.localModifiedTime == dbEntry.localModifiedTime && localEntry.size == dbEntry.size && dbEntry.csum == null){
								log.debug("NEW(delayed) \nPH{ %s } \nDB{ %s }".format(localEntry.toStringDebug, dbEntry.toStringDebug))
        return FileEntryStatusResult(dbEntry,FileEntryStatus.New)
      }
      
      //New but has been changed before sync
      if ( (localEntry.localModifiedTime != dbEntry.localModifiedTime || localEntry.size != dbEntry.size) && dbEntry.csum == null){
								log.debug("CHANGED(delayed) \nPH{ %s } \nDB{ %s }".format(localEntry.toStringDebug, dbEntry.toStringDebug))
        if (updateEntry){
										dbEntry.localModifiedTime = localEntry.localModifiedTime
										dbEntry.size = localEntry.size
										CurrentDatabase.save(dbEntry)
								}
        return FileEntryStatusResult(dbEntry,FileEntryStatus.New)
      }
      //File has been changed
      if ( !localEntry.file.isDirectory && 
          (localEntry.localModifiedTime != dbEntry.localModifiedTime || localEntry.size != dbEntry.size ) && dbEntry.csum != null ){
								log.debug("CHANGED \nPH{ %s } \nDB{ %s }".format(localEntry.toStringDebug, dbEntry.toStringDebug))
        return FileEntryStatusResult(localEntry, if (dbEntry.csum == null) FileEntryStatus.New else FileEntryStatus.Changed )
        
      }
      
    }
    FileEntryStatusResult(null,null)
				
		}
  def apply(path: String, mountPoint:String): FileEntryStatusResult = {
    val localEntry = FileEntry(path, mountPoint)
    val dbEntry = if (localEntry != null) 
      CurrentDatabase.retrive(localEntry.fkey)
    else {		
      CurrentDatabase.retrive(FileEntry.fkeyForRelativePath(path, mountPoint))
    }
				compare(localEntry, dbEntry, true)
				
  }

}



