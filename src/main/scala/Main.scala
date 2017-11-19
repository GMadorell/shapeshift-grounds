import scala.concurrent.{ExecutionContext, Future}

import cats.Monad
import cats.data.EitherT
import cats.implicits._

/*
 * @TODO
 * - be able to recover from errors and return default values -> grab an error and return a default dummy user when the user isn't found
 */

final class SoundMessageCreator[P[_]: Monad](
    soundMessageRepository: SoundMessageRepository[P],
    conversationRepository: ConversationRepository[P],
    conversationMembersRepository: ConversationMembersRepository[P]) {

  private type Result[A] = EitherT[P, SoundMessageError, A]

  def createSoundMessage(): P[Either[SoundMessageError, Unit]] =
    createSoundMessageT().value

  private def createSoundMessageT(): Result[Unit] =
    for {
      conversationResponse        <- searchConversation()
      conversationMembersResponse <- searchConversationMembers()
      createMessageResult <- createMessage(conversationResponse.productId,
                                           conversationMembersResponse.sellerId,
                                           conversationMembersResponse.buyerId)
    } yield createMessageResult

  private def searchConversation(): Result[ConversationResponse] =
    EitherT(conversationRepository.find()).leftMap {
      case ConversationNotFound => SoundConversationNotFound
    }

  private def searchConversationMembers(): Result[ConversationMembersResponse] =
    EitherT(conversationMembersRepository.find()).leftMap {
      case ConversationMembersNotFound => SoundConversationMembersNotFound
    }

  private def createMessage(productId: String, sellerId: String, buyerId: String): Result[Unit] =
    EitherT(
      soundMessageRepository
        .insert(SoundMessage(productId, sellerId, buyerId))
        .map(_.asRight[SoundMessageError]))
}

case class SoundMessage(productId: String, sellerId: String, buyerId: String)
trait SoundMessageRepository[P[_]] {
  def insert(soundMessage: SoundMessage): P[Unit]
}

case class ConversationResponse(productId: String)
trait ConversationRepository[P[_]] {
  def find(): P[Either[ConversationError, ConversationResponse]]
}

case class ConversationMembersResponse(sellerId: String, buyerId: String)
trait ConversationMembersRepository[P[_]] {
  def find(): P[Either[ConversationMembersError, ConversationMembersResponse]]
}

sealed trait ConversationError
case object ConversationNotFound extends ConversationError

sealed trait ConversationMembersError
case object ConversationMembersNotFound extends ConversationMembersError

sealed trait SoundMessageError
case object SoundConversationNotFound        extends SoundMessageError
case object SoundConversationMembersNotFound extends SoundMessageError
