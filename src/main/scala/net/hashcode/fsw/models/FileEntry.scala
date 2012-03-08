package net.hashcode.fsw.models
import java.io.File
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger


object FileEntry{
  
  def apply(filepath: String, mountPoint: String):FileEntry = {
    val fe = new FileEntry
    fe.file = new File(fixedPath(filepath))
    fe.filename = fe.file.getName
    fe.size = fe.file.length
    fe.isDirectory = fe.file.isDirectory
    fe.localModifiedTime = fe.file.lastModified
    fe.filepath = fe.file.getAbsolutePath
    fe.mountPoint = mountPoint.toLowerCase
    fe.normalized
    
    if (!fe.file.exists) return null
    fe
  }

		def relativePath = (_:String).replace((_:String), "").toLowerCase
		def	generateFKey = DigestUtils.md5Hex(_:String)
  def	fkeyForRelativePath(path:String, mountPath:String) = generateFKey(relativePath(path,mountPath))
		def fixedPath(path: String): String = {
    if (path == null || path == "") return "/"
    
				return FilenameUtils.normalize(path.split("/").filter(_ != "").mkString("/","/","")).toLowerCase
    //return 
  }
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
    
    serverPath = FileEntry.fixedPath(serverPath).toLowerCase
    parentPath = FileEntry.fixedPath(parentPath).toLowerCase
    if (filepath == null && file == null){
						file = new File(FileEntry.fixedPath(mountPoint+serverPath)).getAbsoluteFile
						filepath = file.getAbsolutePath
				}
				if (localModifiedTime != 0 && localModifiedTime.toString.length == 13)
						localModifiedTime =  localModifiedTime/ 1000
//				if (serverPath== "/")
//						parentPath = null
//						
    if (fkey == null)
      fkey=FileEntry.generateFKey(serverPath)
  }
  
  def exists = (filepath != null) && (file != null) && (file.exists)
  def	isEquals(that:FileEntry) = {
				(that != null && this.size == that.size && this.localModifiedTime == that.localModifiedTime && this.serverPath == that.serverPath )		
		}
//  override def toString = "[%s][%-6s] %s %s".format(fkey, if (csum == null) "local" else "synced", if (isDirectory) "D" else "F", serverPath)
  override def toString = "[%s] %s %s".format(fkey,  if (isDirectory) "D" else "F", serverPath)
  def toStringDebug = "[%s] %s %s exists: %s size: %s time: %s csum: %s".format(fkey,  if (isDirectory) "D" else "F", serverPath,exists, size,localModifiedTime, csum)
  def	isRoot = serverPath == "/"
}
