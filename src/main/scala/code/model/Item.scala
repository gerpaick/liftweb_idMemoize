package code.model

import net.liftweb.mapper._
import java.math.MathContext

class Item extends LongKeyedMapper[Item] with IdPK {

  def getSingleton = Item

  object name extends MappedString(this,45){
    override def dbColumnName = "name"
  }

  object price extends MappedDecimal(this, MathContext.DECIMAL64, 0) {
    override def dbColumnName = "price"
    override val scale = 2
  }
  
  object quantity extends MappedInt (this) {
    override def dbColumnName = "quantity"
  }


  object invoice extends MappedLongForeignKey(this,Invoice){
    override def dbColumnName = "invoice_id"
  }


}

object Item extends Item with LongKeyedMetaMapper[Item]{
  override def dbTableName = "items"
}
