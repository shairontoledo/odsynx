package net.hashcode.fsw.persistence
import net.hashcode.fsw.models.FileEntry



trait LocalDatabase {

  def retrive(fkey: String): FileEntry
  def find(serverPath: String): FileEntry
  def findChildren(parentPath: String, op:FileEntry => Unit)
  def save(fileEntry: FileEntry)
  def save(fileEntries: List[FileEntry])
  def remove(fileEntry: FileEntry)
  def fetchAll(op:FileEntry => Unit)
  def saveRevision(revision:String):String
  def loadRevision:String
  def init
}


