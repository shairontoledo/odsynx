package net.hashcode.fsw.models
import java.io.File
import org.apache.commons.codec.digest.DigestUtils
import org.apache.log4j.Logger


object FileEntry{
  
  def apply(filepath: String, mountPoint: String):FileEntry = {
    val fe = new FileEntry
    fe.file = new File(filepath)
    fe.filename = fe.file.getName
    fe.size = fe.file.length
    fe.isDirectory = fe.file.isDirectory
    fe.localModifiedTime = fe.file.lastModified
    fe.filepath = filepath
    fe.mountPoint = mountPoint.toLowerCase
    fe.normalized
    
    if (!fe.file.exists) return null
    fe
  }

   def relativePath = (_:String).replace((_:String), "").toLowerCase
}

class FileEntry {
  val log = Logger.getLogger(classOf[FileEntry])
  
  var id: Long = _
  var fkey: String = _
  var filename: String = _
  var csum: String = _
  var localModifiedTime: Long = _
  var isDirectory: Boolean = _
  var size: Long = _
  var parentPath: String = _ 
  var file: File = _
  var filepath: String = _
  var serverPath: String = _
  var mountPoint: String = _
  var deleted = false
  
  def normalized = {
    if (serverPath == null && file != null && file.exists){
      serverPath = file.getAbsolutePath.toLowerCase.replace(mountPoint, "") 
      parentPath = file.getParent.toLowerCase.replace(mountPoint, "") 
    }
    
    serverPath = fixedPath(serverPath).toLowerCase
    parentPath = fixedPath(parentPath).toLowerCase
    
    if (fkey == null)
      fkey=DigestUtils.md5Hex(serverPath)
    
  }
  
  
  
  
  def fixedPath(path: String): String = {
    if (path == null || path == "") return "/"
    
    if (!path.startsWith("/"))
      return "/"+path
    
    return path
    
  }
  def exists = (filepath != null) && (file != null) && (file.exists)
  
  override def toString = "[%s][%-6s] %s %s".format(fkey, if (csum == null) "local" else "synced", if (isDirectory) "D" else "F", serverPath)
  
  
  
}
