package code.lib

import net.liftweb.common.Logger


object Util extends Logger {

  def generateInvoices {

    if (code.model.Invoice.count.toInt == 0) {
      //create some invoices
      for (i <- 1 to 5) {
        val inv: code.model.Invoice = code.model.Invoice.create
        inv.name("Invoice #" + i).status("Open").save()
        info("Invoice #: " + i + " added.")
      }
      for (i <- 1 to 250) {
        val item: code.model.Item = code.model.Item.create

        var qty = new scala.util.Random().nextInt(10)
        if (qty == 0) qty = qty + 1
        var invId = new scala.util.Random().nextInt(5)
        if (invId == 0) invId = invId + 1
        val price = new scala.util.Random().nextDouble() * 10.0d

        item.name("Item #" + i).quantity(qty).invoice(invId).price(price).save()
        info("Item #: " + i + " added to invoice #" + invId)
      }
    }
  }


}
