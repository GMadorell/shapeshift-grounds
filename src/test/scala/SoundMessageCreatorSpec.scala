import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import cats.implicits._

final class SoundMessageCreatorSpec
    extends WordSpec
    with ScalaFutures
    with Matchers
    with MockFactory {

  private implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  val soundMessageRepository: SoundMessageRepository = mock[SoundMessageRepository]
  val conversationRepository: ConversationRepository = mock[ConversationRepository]
  val conversationMembersRepository: ConversationMembersRepository =
    mock[ConversationMembersRepository]

  val creator = new SoundMessageCreator(soundMessageRepository,
                                        conversationRepository,
                                        conversationMembersRepository)

  "A SoundMessageCreator" should {
    "create a sound message" in {
      val productId = s"productId-${Random.nextInt()}"
      val sellerId  = s"sellerId-${Random.nextInt()}"
      val buyerId   = s"buyerId-${Random.nextInt()}"

      shouldFindConversationResponse(productId)
      shouldFindConversationMembers(sellerId, buyerId)
      shouldInsertSoundMessage(SoundMessage(productId, sellerId, buyerId))

      val result = creator.createSoundMessage().futureValue

      result.isRight shouldBe true
    }

    "fail when the conversation does not exist" in {
      shouldNotFindConversationResponse()

      val result = creator.createSoundMessage().futureValue
      result.isLeft shouldBe true
      result.leftMap(_ shouldBe SoundConversationNotFound)
    }

    "fail when the conversation members do not exist" in {
      shouldFindConversationResponse(s"productId-${Random.nextInt()}")
      shouldNotFindConversationMembers()

      val result = creator.createSoundMessage().futureValue
      result.isLeft shouldBe true
      result.leftMap(_ shouldBe SoundConversationMembersNotFound)
    }
  }

  def shouldInsertSoundMessage(soundMessage: SoundMessage): Unit =
    (soundMessageRepository.insert _)
      .expects(soundMessage)
      .once()
      .returning(Future.successful({}))

  def shouldFindConversationResponse(productId: String): Unit =
    (conversationRepository.find _)
      .expects()
      .once()
      .returning(Future.successful(ConversationResponse(productId).asRight[ConversationError]))

  def shouldNotFindConversationResponse(): Unit =
    (conversationRepository.find _)
      .expects()
      .once()
      .returning(Future.successful(ConversationNotFound.asLeft[ConversationResponse]))

  def shouldFindConversationMembers(sellerId: String, buyerId: String): Unit =
    (conversationMembersRepository.find _)
      .expects()
      .once()
      .returning(Future.successful(
        ConversationMembersResponse(sellerId, buyerId).asRight[ConversationMembersError]))

  def shouldNotFindConversationMembers(): Unit =
    (conversationMembersRepository.find _)
      .expects()
      .once()
      .returning(Future.successful(ConversationMembersNotFound.asLeft[ConversationMembersResponse]))
}
