package net.hashcode.fsw.models
import org.specs._
import org.junit._
import Assert._
import java.io._

class FileEntryTest extends  SpecificationWithJUnit {
  "file entry should be loaded by a file" should {
    val fe = FileEntry("src/test/resources/doc/Root.html", new File("").getAbsolutePath)
    "load properly" in {
      fe must notBe(null)
    }

    "have correct serverpath" in {
      fe.serverPath must beEqualTo("/src/test/resources/doc/root.html")
    }
  
    "have correct fkey" in {
      fe.fkey must beEqualTo("7bbd0757d8c49fa27d60890f92dc94eb")
    }

    "have correct name as is(dont low case it)" in {
      fe.name must beEqualTo("Root.html")
    }
  
    "have correct parentPath(relative)" in {
      fe.parentPath must beEqualTo("/src/test/resources/doc")
    }

    "have correct parentPath(relative)" in {
      fe.parentPath must beEqualTo("/src/test/resources/doc")
    }
    
    "be a file(not a directory)" in {
      fe.isDirectory must be(false)
    }
  }
  
  "file entry should be loaded by a direcotry" should {

    val fe = FileEntry("src/test/resources/Doc", new File("").getAbsolutePath)
    "load properly" in {
      fe must notBe(null)
    }

    "have correct serverpath" in {
      fe.serverPath must beEqualTo("/src/test/resources/doc")
    }
  
    "have correct fkey" in {
      fe.fkey must beEqualTo("5726a5d39f70476f2c99b5151f5ada46")
    }

    "have correct name as is(dont low case it)" in {
      fe.name must beEqualTo("Doc")
    }
  
    "have correct parentPath(relative)" in {
      fe.parentPath must beEqualTo("/src/test/resources")
    }
    
    "be a directory" in {
      fe.isDirectory must be(true)
    }
    
  }
  "mount point" should {
    "be normalized by file entry too" in {
      val fe = FileEntry("src/test/resources/doc", new File("").getAbsolutePath)
      fe.mountPoint must beEqualTo(new File("").getAbsolutePath.toLowerCase)
      
    }
  }
}
