import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

import cats.data.EitherT
import cats.implicits._

object Main extends App {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val creator                       = new SoundMessageCreator(new InMemorySoundMessageRepository)
}

final class SoundMessageCreator(soundMessageRepository: SoundMessageRepository)(
    implicit ec: ExecutionContext) {

  type FutureResult[A] = EitherT[Future, DomainError, A]

  def doSomethingUseful(): Future[Either[DomainError, Unit]] =
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
    EitherT(Future.successful(ConversationResponse("productId").asRight[DomainError]))

  private def searchConversationMembers(): FutureResult[ConversationMembersResponse] =
    EitherT(
      Future.successful(ConversationMembersResponse("sellerId", "buyerId").asRight[DomainError]))

  private def createMessage(productId: String,
                            sellerId: String,
                            buyerId: String): FutureResult[Unit] =
    EitherT(Future.successful(().asRight[DomainError]))
}

trait SoundMessageRepository

final class InMemorySoundMessageRepository extends SoundMessageRepository

case class ConversationResponse(productId: String)
case class ConversationMembersResponse(sellerId: String, buyerId: String)

abstract class DomainError
