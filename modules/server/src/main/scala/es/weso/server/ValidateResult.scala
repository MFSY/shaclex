package es.weso.server

import io.circe.Json
import cats.syntax.either._
import es.weso.schema.{ErrorInfo, InfoNode, Result, Solution}
import org.http4s._
import org.http4s.circe._

import scala.xml.Utility.escape
import scala.xml.Utility.escape
import scalatags.Text.TypedTag
import scalatags.Text.all._

case class ValidateResult
 ( data: String,
   dataFormat: String,
   schema: String,
   schemaFormat: String,
   schemaEngine: String,
   triggerMode: String,
   node: Option[String],
   shape: Option[String],
   result: Json
 ) {

 def toHTML: String = {
  val sb = new StringBuilder
  sb ++= "<html>"
  sb ++= "<body>"
  sb ++= "<h1>Validation result</h1>"
  sb ++= result2HTML(result)

  sb ++= s"<h2>Schema details</h2>"
  sb ++= "<details>"
  sb ++= s"<p>Schema format: $schemaFormat/$schemaEngine</p>"
  sb ++=s"<p>Trigger mode: $triggerMode</p>"
  if (node.isDefined) {
   sb ++= s"Node: <code>${node.get}</code> "
  }
  if (shape.isDefined) {
   sb ++= s"Constraint: <code>${escape(shape.get)}</code>"
  }
  sb ++= s"<pre class='schema'>${escape(schema)}</pre></p>"
  sb ++= "</details>"
  sb ++= s"<p>Data details: <details>"
  sb ++= "<h2>Data details</h2>"
  sb ++= s"<p>Data format: $dataFormat</p>"
  sb ++= s"<pre class='data'>${escape(data)}</pre></p>"
  sb ++= "</details>"
  sb ++= "</html>"
  sb.toString
 }

 def result2HTML(jsonResult: Json): String = {
  jsonResult.as[Result].fold(
   failure => s"Failure parsing result: ${jsonResult.spaces2}",
   result => {
    val sb = new StringBuilder
    if (result.isValid) {
     sb ++= s"<p class='valid'>Valid</p>"
     for {s <- result.solutions} {
      sb ++= solution2Html(s).toString
     }
    } else {
     sb ++= s"<p class='notValid'>Not Valid</p>"
     for (e <- result.errors) {
      sb ++= errorInfo2Html(e).toString
     }
    }
    sb.toString
   }
  )
 }

 type HTMLFragment = TypedTag[String]

 def solution2Html(solution: Solution): HTMLFragment = {
   div( h3(cls := "solution")("Solution"),
    table(cls := "nodeInfoNode")
     ( tr(td("Node"),td("Info")),
       for ((node,infoNode) <- solution.nodes.toSeq) yield {
         tr(td(node.getLexicalForm),td(infoNode2Html(infoNode)))
       }
    ))
 }

 def infoNode2Html(infoNode: InfoNode): HTMLFragment = {
  val hasShapes: Seq[HTMLFragment] =
   for ((shape,explanation) <- infoNode.hasShapes) yield {
     tr(
       td("+ " + shape.str),
       td(explanation.str)
     )
   }

  val hasNoShapes: Seq[HTMLFragment] =
    for ((shape,explanation) <- infoNode.hasNoShapes) yield {
      tr(
        td("- " + shape.str),
        td(explanation.str)
      )
   }

  table(cls := "infoNode")(
   tr(td("Constraint"),td("Explanation")),
    hasShapes,
    hasNoShapes
  )
 }

 def errorInfo2Html(e: ErrorInfo): HTMLFragment = {
  div(h3("Error"),pre(escape(e.toString)))
 }
}

