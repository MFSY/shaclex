package es.weso.schema

import es.weso.rdf.jena.RDFAsJenaModel
import es.weso.rdf.nodes.{IRI, RDFNode}
import org.scalatest._

import util._

class SchemaTest extends FunSpec with Matchers with EitherValues {

  describe("Simple schema") {
    it("Validates a simple Schema using ShEx") {
      val schema =
        """|prefix : <http://example.org/>
           |:S { :p . }
           |""".stripMargin
      val data =
        """|prefix :   <http://example.org/>
           |prefix sh: <http://www.w3.org/ns/shacl#>
           |:x :p 1 .
           |:S sh:targetNode :x .
           |""".stripMargin
      val schemaFormat = "SHEXC"
      val dataFormat = "TURTLE"
      val triggerMode = "TARGETDECLS"
      val schemaEngine = "SHEX"
      val node: RDFNode = IRI("http://example.org/x")
      val shape: SchemaLabel = SchemaLabel("http://example.org/S")
      val tryResult: Try[Result] = for {
        schema <- Schemas.fromString(schema,schemaFormat,schemaEngine,None)
        rdf <- RDFAsJenaModel.fromChars(data,dataFormat)
      } yield
         schema.validate(rdf,triggerMode,Map(),None,None,schema.pm)
      tryResult match {
        case Success(result) => {
          info(s"Result: ${result.serialize(Result.TEXT)}")
          info(s"Result solution: ${result.solution}")
          result.isValid should be(true)
          result.hasShapes(node) should contain only(shape)
        }
        case Failure(e) => fail(s"Error trying to validate: $e")
      }
    }
  }

  describe("Simple schema with sh:rootClass check") {
    it("Validates a simple Schema with sh:rootClass check using Shacl") {

      val schema ="""|@prefix : <http://example.org/>
                     |@prefix sh: <http://www.w3.org/ns/shacl#>
                     |@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                     |@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                     |
                     |:S a sh:NodeShape;
                     |   sh:targetNode :good1 ;
                     |   sh:property [ sh:path :p ;
                     |                 sh:rootClass :SuperClass;
                     |                 sh:minCount 1
                     |               ] .
                     |
                     |
                     |""".stripMargin
      val data =
        """|@prefix : <http://example.org/>
           |@prefix sh: <http://www.w3.org/ns/shacl#>
           |@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
           |@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
           |
           |:aClass rdfs:subClassOf :SuperClass.
           |
           |:SubClass rdfs:subClassOf :aClass.
           |:good1 :p :SubClass .
           |""".stripMargin

      val schemaFormat = "TURTLE"
      val dataFormat = "TURTLE"
      val triggerMode = "TARGETDECLS"
      val schemaEngine = "ShaClex"
      val node: RDFNode = IRI("http://example.org/good1")
      val shape: SchemaLabel = SchemaLabel("http://example.org/S")
      val tryResult: Try[Result] = for {
        schema <- Schemas.fromString(schema,schemaFormat,schemaEngine,None)
        rdf <- RDFAsJenaModel.fromChars(data,dataFormat)
      } yield
        schema.validate(rdf,triggerMode,Map(),None,None,schema.pm)
      tryResult match {
        case Success(result) => {
          info(s"Result: ${result.serialize(Result.JSON)}")
          info(s"Result solution: ${result.solution}")
          result.isValid should be(true)
          result.hasShapes(node) should contain (shape)
        }
        case Failure(e) => fail(s"Error trying to validate: $e")
      }
    }
  }
}
