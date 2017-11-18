import scala.concurrent.{ExecutionContext, Future}

import cats.data.EitherT
import cats.implicits._

/*
 * @TODO
 * - be able to recover from errors and return default values -> grab an error and return a default dummy user when the user isn't found
 */

final class SoundMessageCreator(
    soundMessageRepository: SoundMessageRepository,
    conversationRepository: ConversationRepository,
    conversationMembersRepository: ConversationMembersRepository)(implicit ec: ExecutionContext) {

  type FutureResult[A] = EitherT[Future, SoundMessageError, A]

  def createSoundMessage(): Future[Either[SoundMessageError, Unit]] =
    createSoundMessageT().value

  private def createSoundMessageT(): FutureResult[Unit] =
    for {
      conversationResponse        <- searchConversation()
      conversationMembersResponse <- searchConversationMembers()
      createMessageResult <- createMessage(conversationResponse.productId,
                                           conversationMembersResponse.sellerId,
                                           conversationMembersResponse.buyerId)
    } yield createMessageResult

  private def searchConversation(): FutureResult[ConversationResponse] =
    EitherT(conversationRepository.find()).leftMap {
      case ConversationNotFound => SoundConversationNotFound
    }

  private def searchConversationMembers(): FutureResult[ConversationMembersResponse] =
    EitherT(conversationMembersRepository.find()).leftMap {
      case ConversationMembersNotFound => SoundConversationMembersNotFound
    }

  private def createMessage(productId: String,
                            sellerId: String,
                            buyerId: String): FutureResult[Unit] =
    EitherT(
      soundMessageRepository
        .insert(SoundMessage(productId, sellerId, buyerId))
        .map(_.asRight[SoundMessageError]))
}

case class SoundMessage(productId: String, sellerId: String, buyerId: String)
trait SoundMessageRepository {
  def insert(soundMessage: SoundMessage): Future[Unit]
}

case class ConversationResponse(productId: String)
trait ConversationRepository {
  def find(): Future[Either[ConversationError, ConversationResponse]]
}

case class ConversationMembersResponse(sellerId: String, buyerId: String)
trait ConversationMembersRepository {
  def find(): Future[Either[ConversationMembersError, ConversationMembersResponse]]
}

sealed trait ConversationError
case object ConversationNotFound extends ConversationError

sealed trait ConversationMembersError
case object ConversationMembersNotFound extends ConversationMembersError

sealed trait SoundMessageError
case object SoundConversationNotFound        extends SoundMessageError
case object SoundConversationMembersNotFound extends SoundMessageError
