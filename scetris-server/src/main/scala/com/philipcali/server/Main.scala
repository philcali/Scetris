package com.philipcali.server

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote._
import com.philipcali.client._

import java.io.FileWriter

/** The Scetris Game Server */
class Server(val debug: Boolean) extends Actor {
  private def findOponent(player: String, tup: ((String, OutputChannel[Any]), (String, OutputChannel[Any]))) = {
    val (player1, player2) = tup
    if (player == player1._1) player2._2 else player1._2
  }
  
  private def log(details: String)(fun: => Unit) {
    if(debug) withWriter(wr => wr.write(details))
    fun
  }
  
  private def withWriter(w: FileWriter => Unit) {
    val writer = new FileWriter("logs", true)
    try {
      w(writer)
      writer.write("\n")
    } finally {
      writer.close
    }
  }
  
  /**
    * Starts the game server: listens on port 9010
    */
  def act {
    println("Scetris server started")
    
    RemoteActor.alive(9010)
    RemoteActor.register('scetris_server, this)
    
    var pending_games: Map[String, (String, OutputChannel[Any])] = Map()
    var current_game: Map[String, ((String, OutputChannel[Any]), (String, OutputChannel[Any]))] = Map()
    
    loop {
      react {
        case GetList() => log("Get List") {reply(GameList(pending_games.keys.toList))}
        case GameOver(id, pid) => {
          log ("Game Over") {
            if (current_game.get(id) == None) continue
            
            val (player1, player2) = current_game(id)
            if (player1._1 == pid) {
              player1._2 ! Loser(id)
              player2._2 ! Winner(id)
            } else {
              player2._2 ! Loser(id)
              player1._2 ! Winner(id)
            }
            current_game -= id
          }
        }
        case Pause(id) => log("Pause") {
          if (current_game.get(id) != None) {
            val (player1, player2) = current_game(id); player1._2 ! Paused(); player2._2 ! Paused()
          } else {
            pending_games(id)._2 ! Paused()
          }
        }
        case Resume(id) => log("Resume") {
          if (current_game.get(id) != None) {
            val (player1, player2) = current_game(id); player1._2 ! Resumed(); player2._2 ! Resumed()
          } else {
            pending_games(id)._2 ! Resumed()
          }
        }
        case CreateGame(id, pid) => log("Create Game"){
            if (pending_games.get(id) != None || current_game.get(id) != None) {
              sender ! BadName(id)
            } else {
              pending_games += (id -> (pid, sender))
              sender ! Waiting()
            }
        }
        case Leave(id) => log("Leave") (pending_games -= id)
        case JoinGame(id, pid) => log("Join Game") {
          current_game += (id -> (pending_games(id), (pid, sender)))
          pending_games(id)._2 ! Start()
          sender ! Start()
          pending_games -= id
        }
        case Movement(id, pid, coord) => log("Movement") {if (current_game.get(id) != None) findOponent(pid, current_game(id)) ! PieceMove(coord)}
        case Rotate(id, pid, pos) => log("Rotate") {if (current_game.get(id) != None)findOponent(pid, current_game(id)) ! PieceRotate(pos)}
        case Dropped(id, pid) => log("Dropped") {if (current_game.get(id) != None) findOponent(pid, current_game(id)) ! PieceDropped()}
        case Create(id, pid, i) => log("Create") {if (current_game.get(id) != None) findOponent(pid, current_game(id)) ! PieceCreate(i)}
        case Swap(id, pid, i) => log("Swap") {if (current_game.get(id) != None) findOponent(pid, current_game(id)) ! PieceSwap(i)}
        case TestCreate(id) => log("Testing Creation: "+ id) {if (pending_games.get(id) == None) reply(OK(id)) else reply(BadName(id))}
        case TestJoin(id) => log("Test Join") {if (pending_games.get(id) != None) reply(OK(id)) else reply(BadName(id))} 
      }
    }
  }
}

/** Launches our server */
object Main extends Application {
  val s = new Server(false)
  println("Starting Scetris Game Server:")
  s.start
}
