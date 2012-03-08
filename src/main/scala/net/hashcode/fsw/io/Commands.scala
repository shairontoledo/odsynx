package net.hashcode.fsw.io

abstract class Command
object Command {
		
		def	abort(message:String, code:Int=1) = {
				if (System.getProperty("noAbort") == null){	
						println("fatal: "+message)
						Runtime.getRuntime.exit(code)
				}
				
		}
		def	printUsage = {
				println("RTFM "+System.getProperty("test"))
				if (System.getProperty("noAbort") == null){	
						Runtime.getRuntime.exit(1)
				}
		}
		def	apply(args:Array[String]):Command = {
				if (args == null || args.size == 0) printUsage
				
				args(0).toLowerCase match {
						case "init" => Init()
						case "authenticate" => Authenticate()
						case "signup" => SignUp()
						case "pull" => Pull()
						case "push" => if (args.length > 1) Push(args(1)) else Push(null)
						case "ignore" => if (args.length > 1) {
										Ignore(args(1))
								}else{
										abort("Missing argument file for command `ignore', usage: odx ignore path/to/file.txt")
										Unknown()
								}
						case "status" => Status()
						case "sync"		=> Sync()
						case "mount"		=> Mount()
						case "set"		=> {
										if (args.length > 2){
												Command.Set(args(1),args(2))
										}else{
												abort("Missing arguments key and value to 'set' command, usage: odx key.attr value")
												Unknown()
										}
								}
						case _ => { printUsage; Unknown() }
				}
		}
		
		case class Authenticate() extends Command
		case class SignUp() extends Command
		case class Pull() extends Command
		case class Push(file:String) extends Command
		case class Ignore(file:String) extends Command
		case class Status() extends Command
		case class Sync() extends Command
		case class Mount() extends Command
		case class Init() extends Command
		case class Unknown() extends Command
		case class Set(key:String, value:String) extends Command
}