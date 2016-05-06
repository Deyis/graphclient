package clients

import com.google.inject.Inject
import models.Domain.{Edge, ID, Node}
import play.api.Configuration
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws._

import scala.concurrent.Future


class GraphClient @Inject() (ws: WSClient, conf: Configuration) {
  import models.DTO._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  val baseUrl = conf.getString("graph.server.baseurl").getOrElse("http://localhost:9000")

  //  GET     /nodes
  def listNodes(): Future[Seq[Node]] = {
    ws.url(s"$baseUrl/nodes").get()
      .map({ response =>
        if (response.status == Status.OK) {
          response.json.asOpt[Seq[Node]].getOrElse(Seq())
        } else {
          Seq()
        }
      })
  }

  //  POST    /nodes
  def createNode(node: Node): Future[Option[Node]] = {
    ws.url(s"$baseUrl/nodes").post(Json.toJson(node))
      .map({ response =>
        println()
        println(response)
        println(response.body)
        println()
        if (response.status == Status.OK) {
          response.json.asOpt[Node]
        } else {
          None
        }
      })
  }

  //  GET     /nodes/:id
  def findNode(id: ID): Future[Option[Node]] = {
    ws.url(s"$baseUrl/nodes/$id").get()
      .map({ response =>
        if (response.status == Status.OK) {
          response.json.asOpt[Node]
        } else {
          None
        }
      })
  }

  //  PUT     /nodes/:id
  def updateNode(id: ID, node: Node): Future[Option[Node]] = {
    ws.url(s"$baseUrl/nodes/$id").put(Json.toJson(node))
      .map({ response =>
        if (response.status == Status.OK) {
          response.json.asOpt[Node]
        } else {
          None
        }
      })
  }

  //  DELETE  /nodes/:id
  def deleteNode(id: ID): Future[Boolean] = {
    ws.url(s"$baseUrl/nodes/$id").delete()
      .map({ response =>
        println()
        println(response)
        response.status == Status.OK  })
  }

  //  POST    /edges
  def createEdge(edge: Edge): Future[Option[Edge]] = {
    ws.url(s"$baseUrl/edges").post(Json.toJson(edge))
      .map({ response =>
        println()
        println(response)
        println(response.body)
        println()
        if (response.status == Status.OK) {
          response.json.asOpt[Edge]
        } else {
          None
        }
      })
  }

  //  GET     /edges/:from/:to
  def findEdge(from: ID, to: ID): Future[Option[Edge]] = {
    ws.url(s"$baseUrl/edges/$from/$to").get()
      .map({ response =>
        if (response.status == Status.OK) {
          response.json.asOpt[Edge]
        } else {
          None
        }
      })
  }

  //  GET     /edges/:from
  def findAllEdgesFrom(from: ID): Future[Seq[Edge]] = {
    ws.url(s"$baseUrl/edges/$from").get()
      .map({ response =>
        if (response.status == Status.OK) {
          response.json.asOpt[Seq[Edge]].getOrElse(Seq())
        } else {
          Seq()
        }
      })
  }

  //  PUT     /edges/:from/:to
  def updateEdge(from: ID, to: ID, edge: Edge): Future[Option[Edge]] = {
    ws.url(s"$baseUrl/edges/$from/$to").put(Json.toJson(edge))
      .map({ response =>
        if (response.status == Status.OK) {
          response.json.asOpt[Edge]
        } else {
          None
        }
      })
  }

  //  DELETE  /edges/:from/:to
  def deleteEdge(from: ID, to: ID): Future[Boolean] = {
    ws.url(s"$baseUrl/edges/$from/$to").delete()
      .map({ response => response.status == Status.OK  })
  }
}
