package net.hashcode.fsw.persistence
import net.hashcode.fsw.models.FileEntry



trait LocalDatabase {

  def retrive(fkey: String): FileEntry
  def find(serverPath: String): FileEntry
  def save(fileEntry: FileEntry)
  def save(fileEntries: List[FileEntry])
  def init
}
  

