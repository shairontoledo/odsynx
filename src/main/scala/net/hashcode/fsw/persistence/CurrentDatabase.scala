package net.hashcode.fsw
import net.hashcode.fsw.models.FileEntry

package object persistence {
  def CurrentDatabase = LuceneAsDatabase.asInstanceOf[LocalDatabase]
  def save = CurrentDatabase.save(_:List[FileEntry])
  def save(fe:FileEntry) = CurrentDatabase.save(fe)
  def retrive = CurrentDatabase.retrive(_)
  def find = CurrentDatabase.find(_)
  def remove = CurrentDatabase.remove(_)
  def findChildren = CurrentDatabase.findChildren(_, _)
  def loadRevision = CurrentDatabase.loadRevision
  def saveRevision = CurrentDatabase.saveRevision(_)
}


