package yaccoin

import akka.actor.ActorRef
import yaccoin.block.{BlockChain, MemPool}
import yaccoin.utils.future.CancellableFuture

import scala.collection.immutable.TreeSet

package object actors {

  /** Type alias for Communicator State.*/
  type CommunicatorState = (TreeSet[ActorRef], TreeSet[ActorRef])

  /** Type alias for Miner State.*/
  type MinerState = (MemPool, CancellableFuture[Unit])

  /** Type alias for Transactor State.*/
  type TransactorState = Tuple1[BlockChain]

  /** Extract data from Communicator State. */
  implicit class CommunicatorStateImpl(state: CommunicatorState) {

    /** Get Miners. */
    def miners: TreeSet[ActorRef] = state._1

    /** Get Transactors. */
    def transactors: TreeSet[ActorRef] = state._2

  }

  /** Extract data from Miner State. */
  implicit class MinerStateImpl(state: MinerState) {

    /** Get MemPool. */
    def memPool: MemPool = state._1

    /** Get Forge. */
    def forge: CancellableFuture[Unit] = state._2

  }

  /** Extract data from Transactor State. */
  implicit class TransactorStateImpl(state: TransactorState) {

    /** Get BlockChain. */
    def blockChain: BlockChain = state._1

  }

}
