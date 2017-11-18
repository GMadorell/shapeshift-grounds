import scala.concurrent.{ExecutionContext, Future}

import cats.data.EitherT
import cats.implicits._

/*
 * @TODO
 * - assume repos return eithers of different error types -> be able to transform those errors into the sound domain
 * - be able to recover from errors and return default values -> grab an error and return a default dummy user when the user isn't found
 */

final class SoundMessageCreator(
    soundMessageRepository: SoundMessageRepository,
    conversationRepository: ConversationRepository,
    conversationMembersRepository: ConversationMembersRepository)(implicit ec: ExecutionContext) {

  type FutureResult[A] = EitherT[Future, DomainError, A]

  def createSoundMessage(): Future[Either[DomainError, Unit]] =
    doSomethingUsefulEitherT().value

  private def doSomethingUsefulEitherT(): FutureResult[Unit] =
    for {
      conversationResponse        <- searchConversation()
      conversationMembersResponse <- searchConversationMembers()
      createMessageResult <- createMessage(conversationResponse.productId,
                                           conversationMembersResponse.sellerId,
                                           conversationMembersResponse.buyerId)
    } yield createMessageResult

  private def searchConversation(): FutureResult[ConversationResponse] =
    EitherT(conversationRepository.find())

  private def searchConversationMembers(): FutureResult[ConversationMembersResponse] =
    EitherT(conversationMembersRepository.find())

  private def createMessage(productId: String,
                            sellerId: String,
                            buyerId: String): FutureResult[Unit] =
    EitherT(
      soundMessageRepository
        .insert(SoundMessage(productId, sellerId, buyerId))
        .map(_.asRight[DomainError]))
}

case class SoundMessage(productId: String, sellerId: String, buyerId: String)
trait SoundMessageRepository {
  def insert(soundMessage: SoundMessage): Future[Unit]
}

case class ConversationResponse(productId: String)
trait ConversationRepository {
  def find(): Future[Either[DomainError, ConversationResponse]]
}

case class ConversationMembersResponse(sellerId: String, buyerId: String)
trait ConversationMembersRepository {
  def find(): Future[Either[DomainError, ConversationMembersResponse]]
}

abstract class DomainError

case object ConversationNotFound extends DomainError

case object ConversationMembersNotFound extends DomainError
