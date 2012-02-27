package net.hashcode.fsw.persistence
import net.hashcode.fsw.models.FileEntry
import net.hashcode.fsw.models.Volume
import org.apache.lucene.analysis.KeywordAnalyzer
import org.apache.lucene.document._
import org.apache.lucene.index._
import org.apache.lucene.search._
import org.apache.lucene.store._
import org.apache.lucene.util._
import java.io._
import org.apache.lucene.document.Field._
import org.apache.lucene.queryParser.QueryParser

import org.apache.log4j.Logger

object LuceneAsDatabase extends LocalDatabase{
  var IndexDir: String = _
  
  val log = Logger.getLogger(classOf[LocalDatabase])
  
  override def retrive(fkey: String) = findBy("fkey", fkey)
  override def find(serverPath: String) = findBy("server_path", serverPath)

  def findBy(field:String, fieldValue:String): FileEntry = {
    withSearcher( is => for (scoredoc <- is.search(luceneQuery(field,fieldValue), 10).scoreDocs if is.doc(scoredoc.doc).get(field) == fieldValue) 
      return asFileEntry(is.doc(scoredoc.doc))
    )
    return null
  }
  
  
  override def save(fileEntries: List[FileEntry]) = withIndexer( indexer => fileEntries.foreach( saveOrReplace(indexer, _)))

  override def save(fileEntry: FileEntry) = withIndexer(saveOrReplace(_,fileEntry) )
  
  def luceneQuery = new QueryParser(Version.LUCENE_29, (_:String), new KeywordAnalyzer()).parse( (_:String))
  
  def withSearcher(idx: IndexSearcher => Unit){
    val is = new IndexSearcher(LuceneAsDatabase.directory);
    synchronized{
      try{
        idx(is)
      }finally{
        is.close
      }
    }
  }
  
  def withIndexer(idx: IndexWriter => Unit){
    val dirExists = new File(IndexDir).exists
    val writer = new IndexWriter( directory, new KeywordAnalyzer(),!dirExists, IndexWriter.MaxFieldLength.LIMITED)
    try{
      idx(writer)
    }
    finally{
      writer.commit
      writer.close
    }
  }

  def saveOrReplace(indexer:IndexWriter, fe: FileEntry): Unit = {
    if (fe == null) return 
    log.debug("%s Saving ".format(fe))
    indexer.deleteDocuments(luceneQuery("fkey",fe.fkey))
    indexer.addDocument(luceneDocument(fe))
    log.debug("%s Saved".format(fe))
    
  }
  
  def directory = FSDirectory.open(new File(IndexDir))
  
  def luceneDocument(fe: FileEntry): Document = {
    val doc = new Document();
    
//    doc.add(new NumericField("id", Store.YES,true).setLongValue(fe.id));
    doc.add(new Field("server_path", fe.serverPath, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("parent_path", fe.parentPath, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("filename", fe.filename, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    if (fe.csum != null)
      doc.add(new Field("csum", fe.csum+"", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("local_modified_time", fe.localModifiedTime + "", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("is_directory", fe.isDirectory+"", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("fkey", fe.fkey, Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
    doc.add(new Field("size", fe.size+"", Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

    doc
    
  }
  
  def asFileEntry(doc: Document): FileEntry ={
    val fe = new FileEntry
//    if (doc.get("id") != null )
//    fe.id = doc.get("id").toLong
    fe.serverPath = doc.get("server_path")
    fe.parentPath = doc.get("parent_path")
    fe.filename = doc.get("filename")
    fe.csum = doc.get("csum")
    fe.localModifiedTime = doc.get("local_modified_time").toLong
    fe.isDirectory = doc.get("is_directory").toBoolean
    fe.fkey = doc.get("fkey")
    fe.size = doc.get("size").toLong
    fe
    
  }
  
  def init = { 
    IndexDir = new File(Volume.configDir, "db").getAbsolutePath 
    withIndexer{ indexer => null}
  }
    
    
  
}

