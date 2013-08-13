package code.lib

import net.liftweb.common.Logger


object Util extends Logger {

  def generateInvoices {

    if (code.model.Invoice.count.toInt == 0) {
        val inv: code.model.Invoice = code.model.Invoice.create
        inv.name("Invoice #1").status("Open").save()
        info("Invoice #1 added.")

      for (i <- 1 to 10) {
        val item: code.model.Item = code.model.Item.create

        var qty = new scala.util.Random().nextInt(10)
        if (qty == 0) qty = qty + 1
        val price = new scala.util.Random().nextDouble() * 10.0d

        item.name("Item #" + i).quantity(qty).invoice(1).price(price).save()
        info("Item #: " + i + " added to invoice #1")
      }
    }
  }


}
