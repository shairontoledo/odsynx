package net.hashcode.fsw.persistence
import org.specs._
import org.junit._
import Assert._
import java.io._
import net.hashcode.fsw.models._

import org.apache.commons.io.FileUtils

class LuceneAsDatabaseTest extends SpecificationWithJUnit{

  "LuceneAsDatabase" should {
    doFirst{
      val f = File.createTempFile("odtest444", "indexing")
      val dir = new File(f.getParentFile, "od"+f.hashCode) 
      dir.mkdir
      (dir.exists && dir.isDirectory) must beEqualTo(true)
      FileUtils.copyDirectory(new File("src/test/resources/doc"), new File(dir, "doc"))
      new File(dir,"doc").exists must beEqualTo(true)
      Volume.init("Test volume444", dir.getAbsolutePath,false)
    }
    "create the base dir" in {
      //CurrentDatabase.init
      LuceneAsDatabase.IndexDir must notBe(null)
    }
    "index files" in {
      val files = Volume.entries
      CurrentDatabase.save(files)
      new File(LuceneAsDatabase.IndexDir).exists must beEqualTo(true)
      
      files(0).fkey must beEqualTo("1891aa645845b0dde5f2753cb62338fa")
      files(1).fkey must beEqualTo("a5922e9ab6482f00aa17473e9020437e")
      files(2).fkey must beEqualTo("cb49c90e164f264bdb53efa08ad89399")
      files(3).fkey must beEqualTo("19366998bbbfa697241dd43a884faddc")
        
    }
    "search by fkey" in {
      var fkey = "1891aa645845b0dde5f2753cb62338fa"
      val fe = CurrentDatabase.retrive(fkey)
      fe must notBeNull
      fe.fkey must beEqualTo(fkey)
      fe.isDirectory must be(true)
      
      fkey = "a5922e9ab6482f00aa17473e9020437e"
      val fe2 = CurrentDatabase.retrive(fkey)
      fe2 must notBeNull
      fe2.fkey must beEqualTo(fkey)
      fe2.isDirectory must be(false)
      fe2.filename must beEqualTo("file root.html")
      fe2.serverPath must beEqualTo("/doc/file root.html")
      fe2.localModifiedTime must notBe(0L)
      
      
    }
    "absolutelly search by server path" in {
      val path = "/doc/index/index-g.html"
      var fe = CurrentDatabase.find(path)
      fe must notBeNull
      fe.serverPath must beEqualTo(path)
      fe.fkey must beEqualTo("0adfdd1e85b54f9c6845d3eccc3ae9d2")
      
      CurrentDatabase.find("/doc").fkey must beEqualTo("1891aa645845b0dde5f2753cb62338fa")
      CurrentDatabase.find("/doc/org").fkey must beEqualTo("0278488281ca526c7ccb12326535a731")
    }
    
    "absolutelly search by fkey if any change in the file" in {
      
      val srcFile = "src/main/resources/log4j.properties"
      val file = new File(Volume.mountPoint, "testfile.txt")
      FileUtils.copyFile(new File(srcFile), file)
      
      val f = FileEntry(file.getAbsolutePath,Volume.mountPoint)
      f.fkey must beEqualTo("d6670c850cd9357b2ee617e4c21cc5eb")
      CurrentDatabase.save(f)
      
      val fdb = CurrentDatabase.retrive(f.fkey)

      fdb.fkey must beEqualTo(f.fkey)
      fdb.localModifiedTime must beEqualTo(f.localModifiedTime)  
      
      FileUtils.touch(file)
      val updatedfe = FileEntry(file.getAbsolutePath, Volume.mountPoint)
      updatedfe.fkey must beEqualTo("d6670c850cd9357b2ee617e4c21cc5eb")
      updatedfe.localModifiedTime must notBe(f.localModifiedTime)
      
      CurrentDatabase.save(updatedfe)
      val fdb2 = CurrentDatabase.retrive(f.fkey)

      fdb2.fkey must beEqualTo(f.fkey)
      fdb2.localModifiedTime must beEqualTo(updatedfe.localModifiedTime)  
      
      
    }
    
				"load a null revision" in {

      CurrentDatabase.loadRevision must beNull
    }
				
				"Save and load a revision" in {
      val rev = "a80250ca9753cea7eb0becb4cc4a5c15"
						
      CurrentDatabase.saveRevision(rev) must beEqualTo(rev)
      CurrentDatabase.loadRevision must beEqualTo(rev)
      
						
    }
				
    
  }
}
