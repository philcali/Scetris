/** Contains all the client/server events necessary for
  * Scetris to play over the wire
  */
package com.philipcali.client

case class GetList()
case class GameOver(gid: String, pid: String)
case class TestCreate(gid: String)
case class TestJoin(gid : String)
case class Leave(gid: String)
case class Pause(gid: String)
case class Resume(gid: String)
// movement is rotate, left, right, down, fall
case class Movement(gid: String, pid: String, coord:(Int, Int))
case class Rotate(gid: String, pid: String, position: Int)
case class Dropped(gid: String, pid: String)
case class Create(gid: String, pid: String, i :Int)
case class Swap(gid: String, pid: String, i: Int)
//case class Lines(gid: String, player: Actor, lines: Int)
case class CreateGame(gid: String, pid: String)
case class JoinGame(gid: String, pid: String)

// Client signals
case class GameList(gl: List[String])
case class Winner(gid: String)
case class Loser(gid: String)
case class Joined(gid: String)
case class Waiting()
case class Start()
case class Paused()
case class Resumed()
case class OK(gid: String)
case class BadName(name: String)
case class PieceMove(coord: (Int, Int))
case class PieceRotate(x: Int)
case class PieceDropped()
case class PieceCreate(i: Int)
case class PieceSwap(i: Int)

