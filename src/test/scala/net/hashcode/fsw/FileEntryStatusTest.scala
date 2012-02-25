package net.hashcode.fsw.models
import org.specs._
import org.junit._
import Assert._
import java.io._
import net.hashcode.fsw.persistence._

import org.apache.commons.io.FileUtils

class FileEntryStatusTest extends SpecificationWithJUnit{

  "File Entry Status " should {
    doFirst{
      val f = File.createTempFile("odtest2", "indexing")
      val dir = new File(f.getParentFile, "od"+f.hashCode) 
      dir.mkdir
      (dir.exists && dir.isDirectory) must beEqualTo(true)
      FileUtils.copyDirectory(new File("src/test/resources/doc"), new File(dir, "doc"))
      new File(dir,"doc").exists must beEqualTo(true)
      Volume.init("Test volume2", dir.getAbsolutePath)
      
    }
    
    "detect as status New" in {
      val f = new File(Volume.mountPoint,"doc/lib/arrow-down.png" )
      f.exists must beEqualTo(true)
      val fe = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe must notBeNull
      fe.file.exists must beEqualTo(true)
      
      val result = FileEntryStatus(f.getAbsolutePath, Volume.mountPoint)
      result must notBeNull
      result.status must beEqualTo(FileEntryStatus.New)
    }
    "detect as status `null` because file is doesnt exists and also it's not in db" in {
      val f = new File(Volume.mountPoint,"doc/lib/doestexists" )
      f.exists must beEqualTo(false)
      val fe = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe must beNull
      
      
      val result = FileEntryStatus(f.getAbsolutePath, Volume.mountPoint)
      result must notBeNull
      result.status must beNull
      result.fileEntry must beNull
    }
    "detect as status Removed" in {
      val f = new File(Volume.mountPoint,"doc/lib/template.js" )
      f.exists must beEqualTo(true)
      val fe = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe must notBeNull
      fe.checksum = "yes it has" //mock checksum
      CurrentDatabase.save(fe)
      f.delete
      val fe2 = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe2 must beNull
      
      val result = FileEntryStatus(f.getAbsolutePath, Volume.mountPoint)
      result must notBeNull
      result.status must beEqualTo(FileEntryStatus.Removed)
      result.fileEntry must notBeNull
    }
    
    "detect as status Synced" in {
      val f = new File(Volume.mountPoint,"doc/lib/tools.tooltip.js" )
      f.exists must beEqualTo(true)
      val fe = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe must notBeNull
      fe.checksum = "yes it has" //mock checksum
      CurrentDatabase.save(fe)
     
      val fe2 = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe2 must notBeNull
      
      val result = FileEntryStatus(f.getAbsolutePath, Volume.mountPoint)
      result must notBeNull
      result.status must beEqualTo(FileEntryStatus.Synced)
      result.fileEntry must notBeNull
    }
    "detect as New but still not synced, duplicate action" in {
      val f = new File(Volume.mountPoint,"doc/lib/ownerbg.gif" )
      f.exists must beEqualTo(true)
      val fe = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe must notBeNull
      //fe.checksum = 
      CurrentDatabase.save(fe)

      val fe2 = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe2 must notBeNull
      
      val result = FileEntryStatus(f.getAbsolutePath, Volume.mountPoint)
      result must notBeNull
      result.status must beEqualTo(FileEntryStatus.New)
      result.fileEntry must notBeNull
      
    }
    "detect as New but has been changed before sync" in {
      val f = new File(Volume.mountPoint,"doc/lib/packagesbg.gif" )
      f.exists must beEqualTo(true)
      val fe = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe must notBeNull
      //fe.checksum = "yes it has" //mock checksum
      CurrentDatabase.save(fe)
      Thread.sleep(300)
      FileUtils.touch(f)
      val fe2 = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe2 must notBeNull
      
      val result = FileEntryStatus(f.getAbsolutePath, Volume.mountPoint)
      result must notBeNull
      result.status must beEqualTo(FileEntryStatus.New)
      result.fileEntry must notBeNull
      
      result.fileEntry.localModifiedTime must beGreaterThan(fe.localModifiedTime)
    }
    "detect as Changed" in {
      val f = new File(Volume.mountPoint,"doc/index/index-a.html" )
      f.exists must beEqualTo(true)
      val fe = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe must notBeNull
      fe.checksum = "yes it has" //mock checksum
      CurrentDatabase.save(fe)
      Thread.sleep(300)
      FileUtils.touch(f)
      val fe2 = FileEntry(f.getAbsolutePath, Volume.mountPoint)
      fe2 must notBeNull
      
      val result = FileEntryStatus(f.getAbsolutePath, Volume.mountPoint)
      result must notBeNull
      result.status must beEqualTo(FileEntryStatus.Changed)
      result.fileEntry must notBeNull
      
      result.fileEntry.localModifiedTime must beGreaterThan(fe.localModifiedTime)
    }
  }
}
