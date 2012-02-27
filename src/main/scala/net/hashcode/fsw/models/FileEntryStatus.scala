
package net.hashcode.fsw.models


import net.hashcode.fsw.persistence.CurrentDatabase

object FileEntryStatus extends Enumeration{
  type Status = Value
  val New, Removed, Synced, Changed = Value
  case class FilEntryStatusResult(fileEntry: FileEntry, status: FileEntryStatus.Value)
  
  def apply(path: String, mountPoint:String): FilEntryStatusResult = {
    val physicalEntry = FileEntry(path, mountPoint)
    val dbEntry = if (physicalEntry != null) 
      CurrentDatabase.retrive(physicalEntry.fkey)
    else {
      CurrentDatabase.find(FileEntry.relativePath(path, mountPoint))
    }

    
    //new file
    if (physicalEntry != null && physicalEntry.exists && dbEntry == null){
      return FilEntryStatusResult(physicalEntry,FileEntryStatus.New)
    }
    
    //file has been removed
    if (physicalEntry == null &&  dbEntry != null && dbEntry.csum != null){
      return FilEntryStatusResult(dbEntry,FileEntryStatus.Removed)
    }
    
    if (physicalEntry != null && physicalEntry.exists && dbEntry != null){
      //synced
      if (physicalEntry.localModifiedTime == dbEntry.localModifiedTime && physicalEntry.size == dbEntry.size && dbEntry.csum != null){
        return FilEntryStatusResult(dbEntry,FileEntryStatus.Synced)
      }
      
      //New but still not synced, if true, duplicate action
      if (physicalEntry.localModifiedTime == dbEntry.localModifiedTime && physicalEntry.size == dbEntry.size && dbEntry.csum == null){
        return FilEntryStatusResult(dbEntry,FileEntryStatus.New)
      }
      
      //New but has been changed before sync
      if ( (physicalEntry.localModifiedTime != dbEntry.localModifiedTime || physicalEntry.size != dbEntry.size) && dbEntry.csum == null){
        dbEntry.localModifiedTime = physicalEntry.localModifiedTime
        dbEntry.size = physicalEntry.size
        CurrentDatabase.save(dbEntry)
        return FilEntryStatusResult(dbEntry,FileEntryStatus.New)
      }
      //File has been changed
      if ( !physicalEntry.file.isDirectory && 
          (physicalEntry.localModifiedTime != dbEntry.localModifiedTime || physicalEntry.size != dbEntry.size ) && dbEntry.csum != null ){
        dbEntry.localModifiedTime = physicalEntry.localModifiedTime
        dbEntry.size = dbEntry.size
        return FilEntryStatusResult(dbEntry, if (dbEntry.csum == null) FileEntryStatus.New else FileEntryStatus.Changed )
        
      }
      
      
    }
    FilEntryStatusResult(null,null)
    
  }

}



