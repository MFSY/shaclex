package es.weso.shacl

import org.scalatest._
import es.weso.rdf.nodes._
import es.weso.rdf.jena.RDFAsJenaModel
import es.weso.rdf._

import util._
import Validator._
import es.weso.shacl.showShacl._
import es.weso.shacl.converter.RDF2Shacl

class ShapeValidatorTest extends
  FunSpec with Matchers with TryValues with EitherValues {

describe("Shapes") {
  it("Should validate single shape") {
    val ex = IRI("http://example.org/")
    val s = ex + "S"
    val str ="""|@prefix : <http://example.org/>
                 |@prefix sh: <http://www.w3.org/ns/shacl#>
                 |
                 |:S a sh:Shape;
                 |   sh:targetNode :x;
                 |   sh:property [sh:path :p;
                 |   sh:minCount 1] .
                 |:x :p "a" .
                 |""".stripMargin
    val attempt = for {
      rdf <- RDFAsJenaModel.parseChars(str,"TURTLE")
      schema <- RDF2Shacl.getShacl(rdf)
      shape <- schema.shape(s)
      validator = Validator(schema)
      result <- Validator.validate(schema,rdf)
    } yield (result)
    attempt match {
      case Right(result) => info(s"Result: $result")
      case Left(e) => fail(s"Failed: $e")
    }
    }
 }
}
