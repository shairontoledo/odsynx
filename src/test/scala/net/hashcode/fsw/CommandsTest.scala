package net.hashcode.fsw.io
import org.specs._
import org.junit._
import Assert._
import java.io._
import net.hashcode.fsw.persistence._

class CommandsTest extends SpecificationWithJUnit{

  "A Command " should {
    
    "detect command `init`" in {
      val args1 = Array("init")
      //specs 1 sucks to test classOf[T], using class name string instead
      Command(args1).getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Init")
    }
    
    "detect command `signup`" in {
      val args1 = Array("signup")
      
      Command(args1).getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.SignUp")
    }
    
    "detect command `push`(with no arg)" in {
      val args1 = Array("push")
      
      Command(args1).getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Push")
    }
    
    "detect command `push`(with arg)" in {
      val args1 = Array("push", "foo/bar/baz")
      val cmd = Command(args1)
      cmd.getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Push")
      val pushCmd = cmd.asInstanceOf[Command.Push]
      pushCmd.file must beEqualTo("foo/bar/baz")
    }
    
    "detect command `ignore` with no arg file" in {
      val args1 = Array("ignore")
      
      Command(args1).getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Unknown")
    }
    
    "detect command `ignore` with arg file" in {
      val args1 = Array("ignore", "foo/bar/file.txt")
      
      val cmd = Command(args1)
      cmd.getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Ignore")
      val pushCmd = cmd.asInstanceOf[Command.Ignore]
      pushCmd.file must beEqualTo("foo/bar/file.txt")
    }
    
    "detect command `status`" in {
      val args1 = Array("status")
      Command(args1).getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Status")
    }
    
    "detect command `sync`" in {
      val args1 = Array("sync")
      Command(args1).getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Sync")
    }
    
    "detect command `mount`" in {
      val args1 = Array("mount")
      Command(args1).getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Mount")
    }
    
    "detect command `set` with no key and value" in {
      val args1 = Array("set")
      
      val cmd = Command(args1)
      cmd.getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Unknown")
    }
    
    "detect command `set` with no value" in {
      val args1 = Array("set", "auth.username")
      
      val cmd = Command(args1)
      cmd.getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Unknown")	
    }
    
    "detect command `set` with key and value" in {
      val args1 = Array("set", "auth.username", "password")
      
      val cmd = Command(args1)
      cmd.getClass.getCanonicalName must beEqualTo("net.hashcode.fsw.io.Command.Set")
      val pushCmd = cmd.asInstanceOf[Command.Set]
      pushCmd.key must beEqualTo("auth.username")
      pushCmd.value must beEqualTo("password")
    }
  }
}						