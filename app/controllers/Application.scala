package controllers

import java.util.UUID

import clients.GraphClient
import com.google.inject.Inject
import models.Domain._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

class Application @Inject() (graphClient: GraphClient) extends Controller {
  import models.DTO._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  import Application._

  def createGraph = Action.async {implicit request =>

    val labels = Seq("2","3","5","7","8","9","10","11")

    val edgesToCreate = Seq(
      "3" -> "10", "3" -> "8",
      "5" -> "11",
      "7" -> "11", "7" -> "8",
      "8" -> "9",
      "11" -> "2", "11" -> "9", "11" -> "10")

    val nodesToCreate = for { label <- labels } yield Node(UUID.randomUUID(), Meta(name = label))

    val savedNodes = Future.sequence(nodesToCreate.map(node => { graphClient.createNode(node) } )).map(seq => { println(seq); seq.map(_.get) })

    val savedEdges = for {
      nodes <- savedNodes
      nameToID = nodes.map(n => n.meta.name -> n.id).toMap
      edges <- Future.sequence(edgesToCreate.map({ case (nameFrom, nameTo) =>
        val edge = Edge(from = nameToID.get(nameFrom).get, to = nameToID.get(nameTo).get, weight = Weight(1))
        Thread.sleep(100)
        graphClient.createEdge(edge)
      }))
    } yield {
      println(edges)
      edges.map(_.get)
    }

    for {
      nodes <- savedNodes
      edges <- savedEdges
    } yield {
      Ok(Json.obj("nodes" -> Json.toJson(nodes), "edges" -> Json.toJson(edges)))
    }
  }

  def findAllPaths(from: ID, to: ID) = Action.async { implicit  request =>
    findPaths(from, to, State(visited = Seq(), path = Seq())).map(paths => Ok(Json.toJson(paths.map(_.path))))
  }

  def findPaths(from: ID, to: ID, state: State): Future[Seq[State]] = {
    for {
      allEdges <- graphClient.findAllEdgesFrom(from)
      paths <- Future.sequence({ allEdges.map({
        case edge if edge.to == to => Some(Future.successful[Seq[State]]({Seq(state.copy(visited = state.visited :+ edge.from, path = state.path :+ edge))}))
        case edge if state.visited.contains(edge.to) => None
        case edge => Some(findPaths(edge.to, to, state.copy(visited = state.visited :+ edge.from, path = state.path :+ edge)))
      }).collect({case Some(future) => future }) })
    } yield {
      paths.flatten
    }
  }

  def index = Action.async { implicit request =>

    for {
        node1 <- graphClient.createNode(Node(UUID.randomUUID(), Meta(name = "first")))
        node2 <- graphClient.createNode(Node(UUID.randomUUID(), Meta(name = "second")))
        edge <- graphClient.createEdge(Edge(from = node1.get.id, to = node2.get.id, weight = Weight(distance = 1)))
        found <- graphClient.findEdge(from = node1.get.id, to = node2.get.id)
    } yield {
      Ok(Json.toJson(found))
    }
  }

}

object Application {

  case class State(visited: Seq[ID], path: Seq[Edge])
}