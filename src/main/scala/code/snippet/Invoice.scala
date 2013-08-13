package code.snippet

import net.liftweb.common._
import net.liftweb.common.Full
import net.liftweb._
import http._
import util._
import Helpers._
import js.JsCmds._
import js.jquery._
import scala.xml.NodeSeq
import net.liftweb.http.{S, SHtml}
import bootstrap.liftweb.InvoiceInfo


case class Line(guid: String, name: String, price: Double, quantity: Int)

class Invoice {

  val invoices = code.model.Invoice.findAll()

  def showAll = invoices match {
    case a :: b => ".invoices *" #> {
      invoices.map(invoice =>
        "#name * " #> invoice.name.get &
          "#status *" #> invoice.status.get &
          "#editlink [href]" #> ("/invoice/edit/" + invoice.id.get.toString))
    }
    case List() => "*" #> "there is no invoice"
  }

}

class addInvoice() {
  val invoiceStatusListSource = List("Open", "Closed")
  val invoiceStatus = invoiceStatusListSource.map(t => (t, t))

  private object Info {
    val items = ValueCell(List(newLine))
  }

  def showLines = "* *" #> (Info.items.get.flatMap(renderLine): NodeSeq)

  def addLine(ns: NodeSeq): NodeSeq = {
    val tr = S.attr("tr") openOr "where"
    SHtml.ajaxButton(ns, () => {
      val theLine = appendLine
      val guid = theLine.guid
      JqJsCmds.AppendHtml(tr, renderLine(theLine))
    })
  }

  //add lines
  private def renderLine(theLine: Line): NodeSeq =
    <tr id={theLine.guid}>
      <td>
        {SHtml.ajaxText(theLine.name,
        s => {
          mutateLine(theLine.guid) {
            l => Line(l.guid, s, l.price, l.quantity)
          }
          Noop
        })}
      </td>
      <td>
        {SHtml.ajaxText(theLine.price.toString,
        s => {
          Helpers.asDouble(s).foreach {
            d =>
              mutateLine(theLine.guid) {
                l => Line(l.guid, l.name, d, l.quantity)
              }
          }
          Noop
        })}
      </td>
      <td>
        {SHtml.ajaxText(theLine.quantity.toString,
        s => {
          Helpers.asInt(s).foreach {
            d =>
              mutateLine(theLine.guid) {
                l => Line(l.guid, l.name, l.price, d)
              }
          }
          Noop
        })}
      </td>
    </tr>

  //defaults values
  private def newLine = Line(nextFuncName, "", 0.00, 1)

  private def appendLine: Line = {
    val ret = newLine
    Info.items.set(ret :: Info.items.get)
    ret
  }

  private def mutateLine(guid: String)(f: Line => Line) {
    val all = Info.items.get
    val head = all.filter(_.guid == guid).map(f)
    val rest = all.filter(_.guid != guid)
    Info.items.set(head ::: rest)
  }

  def render = {

    val invoice = code.model.Invoice.create

    var name = ""
    var status = ""

    def process() {
        invoice.name.set(name)
        invoice.status.set(status)
        invoice.save()
        if (invoice.saved_?) {
          for (row <- Info.items.get) {
            val nl: code.model.Item = code.model.Item.create
            nl.invoice(invoice.id.get).name(row.name).price(row.price).quantity(row.quantity).save()
          }
        }
      S.notice("Invoice added")
      S.redirectTo("/invoice/listall")
    }

      "name=status" #> SHtml.select(invoiceStatus, Empty, status = _) &
      "name=name" #> SHtml.text(name, name = _) &
      "type=submit" #> SHtml.submit("Save Invoice", process)
  }

}


class editInvoice(invoiceId: InvoiceInfo) {

  val invoiceStatusListSource = List("Open", "Closed")
  val invoiceStatus = invoiceStatusListSource.map(t => (t, t))


  def render = {
    val idAsInt = asInt(invoiceId.invoiceId).getOrElse(0)

    code.model.Invoice.find(idAsInt) match {
      case Full(invoice) => {
          def process() {
            S.notice("Informations saved")
            S.redirectTo("/invoice/listall")
          }

          def deleteLine(line: code.model.Item, rend: IdMemoizeTransform)() = {
            line.delete_!
            // Re-render the output.
            rend.setHtml()
          }

        def changeLine(line: code.model.Item, rend: IdMemoizeTransform)() = {
          line.quantity.set(line.quantity.get + 1)
          line.save()
          //Re-render the output.
          rend.setHtml()
        }

            "name=status" #> invoice.status.get &
            "name=name" #> invoice.name.get &
            "#Items_lines" #> SHtml.idMemoize {
              renderer =>
//                 Retrieve lines from the db
                val lines = invoice.items.all
                lines match {
                  case a :: b => "#lines *" #> lines.map(line =>
                    "#name *" #> line.name.get &
                      "#price *" #> line.price.get &
                      "#qty *" #> line.quantity.get &
                      "#dellink [onclick]" #> SHtml.ajaxInvoke(deleteLine(line, renderer) _)  &
                    "#changelink [onclick]" #> SHtml.ajaxInvoke(changeLine(line, renderer) _) )
                  case List() => "#lines" #> "no lines yet"
                  }
            } &
            "type=submit" #> SHtml.submit("Save", process)
      }
      case _ => "*" #> "There is no invoice with this #id"
    }
  }
}