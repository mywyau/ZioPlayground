package ZIO

import zio.duration._
import zio.{Exit, UIO, ZIO}

object MyApp extends zio.App {

  //  val myAppLogic =
  //    for {
  //      _ <- putStrLn("Hello! What is your name?")
  //      name <- getStrLn
  //      _ <- putStrLn(s"Hello ${name} welcome to ZIO!")
  //    } yield ()
  //
  //  def run(args: List[String]) =
  //    myAppLogic.exitCode

  // effect pattern
  // a computation of a value + and effect  i.e. a side effect

  // in pure functional programming we only care about values and keeping it pure no side effects
  // incompatible side effects with the substitution model
  // so we want to encapsulate the effect with a value so we do that with an IO-Monad (input output monad)

  // ZIO[R, E, A], types -  R = Environemnt, E = exception, A = value


  val zmolType: ZIO[Any, Nothing, Int] = ZIO.succeed(42)
  val zmolAliaseType: UIO[Int] = ZIO.succeed(42)

  //concurrency = simulate daily routine of bob

  val showerTime = ZIO.succeed("Taking a shower")
  val boilWater = ZIO.succeed("Boiling some water")
  val prepareCoffee = ZIO.succeed("Prepare some coffee")

  def printThread: String = s"[${Thread.currentThread().getName}]"

  def synchronousBobRoutine() = // this is not parallel when bob could be boiling water as he showers
    for {
      _ <- showerTime.debug(printThread)
      _ <- boilWater
      _ <- prepareCoffee
    } yield () // do not care about the yield so give back unit

  //  def run(args: List[String]) =
  //    synchronousBobRoutine.exitCode

  // a fiber = schedulable computation  we treat a fiber as a data structure, spawned or created on the heap, up to zio runtime to schedule the fibers to achieve paralellism
  // Fiber[E, A] - error lhs,  success rhs

  //  def concurrentShowerWhilstBoilingWater() =
  //  for {
  //    _ <- showerTime.debug(printThread).fork
  //    _ <- boilWater.debug(printThread)
  //    _ <- prepareCoffee.debug(printThread)
  //  } yield ()
  //
  //  def run(args: List[String]) =
  //    concurrentShowerWhilstBoilingWater.exitCode
  //

  def concurrentRoutine() =
    for {
      showerFiber <- showerTime.debug(printThread).fork
      boilingWaterFiber <- boilWater.debug(printThread).fork
      zippedFiber = showerFiber.zip(boilingWaterFiber)
      result <- zippedFiber.join.debug(printThread)
      _ <- ZIO.succeed(s"$result done").debug(printThread) *> prepareCoffee.debug(printThread)
    } yield ()

  val callFromAlice = ZIO.succeed("Call from Alice")
  val boilingWaterWithTime = boilWater.debug(printThread) *> ZIO.sleep(5.seconds) *> ZIO.succeed("Boiled water ready")

  def concurrentRoutineWithAliceCall =
    for {
      _ <- showerTime.debug(printThread)
      boilingWaterFiber <- boilingWaterWithTime.fork
      _ <- callFromAlice.debug(printThread).fork *> boilingWaterFiber.interrupt.debug(printThread)
      _ <- ZIO.succeed(s"Screw my coffee, going with Alice").debug(printThread)
    } yield ()

  val prepareCoffeeWithTime = prepareCoffee.debug(printThread) *> ZIO.sleep(5.second) *> ZIO.succeed("Coffee ready")

  def concurrentRoutineWithCoffeeAtHome() =
    for {
      _ <- showerTime.debug(printThread)
      _ <- boilWater.debug(printThread)
      coffeeFiber <- prepareCoffeeWithTime.debug(printThread).fork.uninterruptible //uninterruptible
      result <- callFromAlice.debug(printThread).fork *> coffeeFiber.interrupt.debug(printThread)
      _ <- result match {
        case Exit.Success(value) => ZIO.succeed("Sorry making breakfast at home").debug(printThread)
        case _ => ZIO.succeed("going to a cafe with Alice").debug(printThread)
      }
    } yield ()


  def run(args: List[String]) =
    concurrentRoutineWithCoffeeAtHome.exitCode


}
