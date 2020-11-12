package web
import java.nio.charset.StandardCharsets
import java.util.UUID

import cats.data.NonEmptyList
import com.twitter.finagle.http.Status
import config.Config
import domain.journeys.{Journey, Line, HardcodedJourneyCache, Route}
import io.finch.Input
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.concurrent.Eventually
import test.TestSupport
import io.circe.syntax._
import domain.journeys._
import scala.io.Source

class JourneyCacheEndpointsSpec
    extends AnyWordSpec
    with Matchers
    with Eventually
    with BeforeAndAfterAll {
  "JourneyCacheEndpoints" should {

      "Return 200 OK" when {
        "A valid start and end postcode are submitted" in {
          TestSupport.withHardcodedJourneyCache() { cache: HardcodedJourneyCache =>
            val req = Input
              .get(
                JourneyCacheEndpointsSpec.getJourneyEndpoint,
                "start" -> "ME7 2EJ",
                "end" -> "SW1A VC1"
              )

            eventually {
              JourneyCacheEndpoints
                .getJourney(cache)(req)
                .awaitOutputUnsafe()
                .map(_.status) mustBe Some(Status.Ok)
            }
          }
        }
      }

      "Return the correct decoded JSON for the Journey" when {
        "when provided with valid postcodes" in {
          TestSupport.withHardcodedJourneyCache() { cache: HardcodedJourneyCache =>
            val req = Input
              .get(
                JourneyCacheEndpointsSpec.getJourneyEndpoint,
                "start" -> "ME7 2EJ",
                "end" -> "SW1A VC1"
              )

            eventually {
              JourneyCacheEndpoints
                .getJourney(cache)(req)
                .awaitOutputUnsafe()
                .map(
                  _.value
                ) mustEqual JourneyCacheEndpointsSpec.validPostcodesResult
            }
          }
        }
      }

  }
}

object JourneyCacheEndpointsSpec {
  val appConfig: Config = TestSupport.loadConfig

  val validPostcodesResult: Journey = Journey(
    UUID.fromString("3bde7cb0-c1dd-42ca-b1d6-a5e2d5662ef1"),
    "ME7 2EJ",
    "SW1A VC1",
    NonEmptyList
      .of(
        Route(NonEmptyList.of(Line("Victoria"), Line("Jubilee")), 24),
        Route(NonEmptyList.of(Line("Victoria"), Line("Central")), 26)
      ),
    25,
    includesNoChangeRoute = false
  )

  val getJourneyEndpoint: String = "/journey"

}