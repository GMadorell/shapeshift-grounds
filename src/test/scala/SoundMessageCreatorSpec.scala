import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures

final class SoundMessageCreatorSpec extends WordSpec with ScalaFutures {

  private implicit val ec = scala.concurrent.ExecutionContext.global

  val creator = new SoundMessageCreator(new InMemorySoundMessageRepository)

  "A SoundMessageCreator" should {
    "do something useful" in {
      creator.doSomethingUseful().futureValue
    }
  }
}
