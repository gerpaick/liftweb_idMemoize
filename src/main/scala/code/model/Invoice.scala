package code.model

import net.liftweb.mapper._
import code.model._

class Invoice extends LongKeyedMapper[Invoice] with IdPK with OneToMany[Long, Invoice] {

  def getSingleton = Invoice

  object name extends MappedString(this, 50) {
    override def dbColumnName = "name"
  }

 object status extends MappedString(this, 25) {
    override def dbColumnName = "status"
  }

  object items extends MappedOneToMany(Item,Item.invoice)


}

object Invoice extends Invoice with LongKeyedMetaMapper[Invoice]{
  override def dbTableName = "invoices"
}
