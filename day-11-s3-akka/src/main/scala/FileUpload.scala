
import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import spray.json.DefaultJsonProtocol._
import java.nio.file.{Files, Paths}
import scala.concurrent.Future
import scala.util.{Failure, Success}


object FileUpload extends App {

  // Load configuration
  val config = ConfigFactory.load()
  val bucketName = "akka-bucket-scala"
  val region = "eu-north-1"
  val accessKeyId = "AKIAQ3EGV4V3LMJMLVVF"
  val secretAccessKey = "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"

  // Setup S3 Client
  val awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey)
  val s3Client = S3Client.builder()
    .region(Region.of(region))
    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
    .build()

  implicit val system: ActorSystem = ActorSystem("file-upload-service")
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext = system.dispatcher

  // Define the JSON payload
  case class FileNamePayload(fileName: String)
  object FileNameJsonProtocol {
    implicit val fileNameFormat = jsonFormat1(FileNamePayload)
  }

  import FileNameJsonProtocol._

  val route =
    path("upload") {
      post {
        entity(as[FileNamePayload]) { payload =>
          val filePath = Paths.get(payload.fileName)

          if (Files.exists(filePath)) {
            val fileName = filePath.getFileName.toString

            val futureResponse: Future[PutObjectResponse] = Future {
              val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build()

              val requestBody = software.amazon.awssdk.core.sync.RequestBody.fromFile(filePath.toFile)
              s3Client.putObject(putObjectRequest, requestBody)
            }

            onComplete(futureResponse) {
              case Success(response) =>
                complete(s"File uploaded successfully with ETag: ${response.eTag()}")
              case Failure(ex) =>
                complete(s"File upload failed: ${ex.getMessage}")
            }
          } else {
            complete(s"File not found: ${payload.fileName}")
          }
        }
      }
    }

  Http().newServerAt("localhost", 8081).bind(route)
}