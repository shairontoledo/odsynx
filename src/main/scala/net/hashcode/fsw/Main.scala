package net.hashcode.fsw
import net.hashcode.fsw.persistence._
import java.io.File
import java.net.URI
import net.hashcode.fsw.io.Command
import net.hashcode.fsw.models.Volume
import net.hashcode.fsw.models._
import org.codehaus.jackson.node.ArrayNode
import net.hashcode.fsw.transport._


object Main {

  def	currentDir = FileEntry.fixedPath(new File("").getAbsoluteFile.getAbsolutePath)
  def currentDirAsFile = new File(currentDir).getAbsoluteFile

  def main(args: Array[String]):Unit = Command(args)  match{
    case Command.Authenticate() => authenticate
    case Command.SignUp() => signup
    case Command.Pull() => pull
    case Command.Push(file) => push(file)
    case Command.Status() => status
    case Command.Sync() => sync
    case Command.Mount() => mount
    case Command.Init() => init
    case Command.Set(key,value) => set(key,value)
  }

  def	init = ConfigManager.initialize(currentDir, true)

  def boot = ConfigManager.initialize(currentDir).validate

  def	initUserEnvironment(startWorkers:Boolean = false) = {
    boot
    authenticate
    Volume.initWithConfig(ConfigManager.configuration, startWorkers)
  }

  def pull = {
    initUserEnvironment(startWorkers = false)
    Volume.pull
  }

  def	set(key:String,value:String) = {
    ConfigManager.initialize(currentDir)
    ConfigManager.set(key,value)
  }

  def	push(file:String) = {
    initUserEnvironment(startWorkers = false)
    val fileToSync = if (file == null) currentDirAsFile else new File(file)
    Volume.push(fileToSync)
  }

  def	status = {
    initUserEnvironment(startWorkers = false)
    Volume.printStatus(null)
  }

  def	sync = {
    initUserEnvironment(false)
    Volume.syncAll
  }

  def	authenticate = {
    boot
    setCredentials
    val res = CurrentTransport.authenticate
    if (!res._1){
      println(res._2)
      Command.abort("Use command 'set' to fix issues or use 'signup' for new user", 4)
    }
  }

  def	mount = initUserEnvironment(startWorkers = true)

  def	signup = {
    boot
    setCredentials	
    val res = CurrentTransport.signup
    if (!res._1){
      val errors = res._2.asInstanceOf[ArrayNode]

      for (i <- 0 to errors.size-1){
        println("Error for auth.%s : %s".format(errors.get(i).get(0), errors.get(i).get(1)))
      }
      Command.abort("Use command 'set' to fix issues", 4)
    }
  }

  def setCredentials = {
    CurrentTransport.setCredentials(new URI(ConfigManager.configuration.server), 
                                    ConfigManager.configuration.username, 
                                    ConfigManager.configuration.password, 
                                    ConfigManager.configuration.email)
  }

}
