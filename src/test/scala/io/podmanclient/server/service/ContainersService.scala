package io.podmanclient.server.service

import org.http4s.Uri
import org.http4s.HttpRoutes
import cats._
import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.podmanclient.api.uri.PodmanUri._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class ContainersService(prefix: Uri.Path) {

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  import ContainersServiceResponse._

  val list = HttpRoutes.of[IO] { case req @ GET -> prefix / "containers" / "json" =>
    log(req.toString()) >> {
      processListParams(req.params).flatMap(resp => Ok(resp.asJson))
    }
  }

  val create = HttpRoutes.of[IO] { case req @ POST -> prefix / "containers" / "create" =>
    log(req.toString()) >> {
      Created(createdContainerSuccess)
    }
  }

  val start = HttpRoutes.of[IO] { case req @ POST -> prefix / "containers" / name /  "start" =>
    log(req.toString()) >> {
      Response[IO](Status.NoContent).pure[IO]
    }
  }

  private def processListParams(params: Map[String, String]): IO[List[Json]] = {
    val all = params.getOrElse("all", "false").toBoolean

    parseFilterJson(params.get("filters")).flatMap {
      case m: Map[String, List[String]] if m.isEmpty => filterByAllParam(all)
      case m: Map[String, List[String]] => filterByStatus(m.getOrElse("status", List.empty))
    }
  }

  private def parseFilterJson(filterJson: Option[String]): IO[Map[String, List[String]]] =
    filterJson match {
      case None      => Map.empty[String, List[String]].pure[IO]
      case Some(str) => decode[Map[String, List[String]]](str).liftTo[IO]
    }

  private def filterByAllParam(all: Boolean) =
    if (all)
      allContainers
    else
      runningContainers

  private def filterByStatus(status: List[String]) =
    status match {
      case Nil => IO.raiseError(new RuntimeException("Missing status value"))
      case head :: next =>
        head match {
          case "running" => runningContainers
          case "exited"  => exitedContainers
          case _         => IO.raiseError(new RuntimeException("Invalid status provided: " + head))
        }
    }

  def endpoints: HttpRoutes[IO] = list <+> create <+> start
}

object ContainersService {

  def endpoints(prefix: Uri.Path): HttpRoutes[IO] = new ContainersService(prefix).endpoints

}

object ContainersServiceResponse {

  def createdContainerSuccess: IO[Json] = Json
    .obj(
      "Id" -> "1044553a141fa92b8404047753ba11607d564dd8eda23301ab4ba6ec41de2fcf".asJson
    )
    .pure[IO]

  def allContainers: IO[List[Json]]     = List(runningContainers, exitedContainers).flatSequence
  def runningContainers: IO[List[Json]] = List(redisContainer, postgresContainer).sequence
  def exitedContainers: IO[List[Json]]  = List(kibanaContainer).sequence

  def redisContainer    = createContainer("redis", "running")
  def postgresContainer = createContainer("postgres", "running")
  def kibanaContainer   = createContainer("kibana", "exited")

  def createContainer(name: String, status: String): IO[Json] = containerTemplate
    .map(_.replaceAll("\\$name", name).replaceAll("\\$status", status))
    .flatMap(asJsonUnsafe)

  def containerTemplate: IO[String] = loadResponse("containers/container.json")

}
