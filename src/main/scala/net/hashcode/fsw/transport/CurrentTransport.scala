package net.hashcode.fsw

package object transport {
  def CurrentTransport = SHTTPTransport.asInstanceOf[EntriesTransport]

  def create = CurrentTransport.create(_)
  def upload = CurrentTransport.upload(_)
  def delete = CurrentTransport.delete(_)
}

