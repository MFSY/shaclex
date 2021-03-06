package es.weso.shex
import es.weso.rdf.nodes.IRI


sealed trait Path
case class Direct(p: IRI) extends Path
case class Inverse(p: IRI) extends Path

object Path {
  implicit def orderingPath: Ordering[Path] = new Ordering[Path] {
    override def compare(x1: Path, x2: Path): Int = {
      x1 match {
        case Direct(p1) =>
          x2 match {
            case Direct(p2) => Ordering[String].compare(p1.str, p2.str)
            case Inverse(p2) => 1
          }
        case Inverse(p1) => {
          x2 match {
            case Direct(_) => -1
            case Inverse(p2) => Ordering[String].compare(p1.str, p2.str)
          }
        }
      }
    }
  }
}
// TODO: Handle sequence and alternative paths

