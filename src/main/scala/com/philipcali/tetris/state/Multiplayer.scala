package com.philipcali.tetris.state

import org.newdawn.slick.state.BasicGameState
import org.newdawn.slick.state.StateBasedGame
import org.newdawn.slick.Input
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics
import org.newdawn.slick.Color
import org.newdawn.slick.geom.Rectangle
import com.philipcali.tetris.util.MessageUtil.{props, centerText, drawString, printControls => pcon}
import com.philipcali.tetris.gamedata.Storage.{saveNetwork => save, defaultnetwork => load}
import org.newdawn.slick.state.transition.FadeOutTransition
import org.newdawn.slick.state.transition.FadeInTransition
import org.newdawn.slick.state.transition.RotateTransition
import com.philipcali.tetris.game.Game
import com.philipcali.tetris.pieces.{Tetris, Block, Empty}
import com.philipcali.tetris.util.ResourceLoader.maps
import scala.actors.Actor
import scala.actors.AbstractActor
import scala.actors.Actor._
import scala.actors.remote._
import com.philipcali.client._

object Server {
  var ip = load
  def connect() = RemoteActor.select(Node(ip, 9010), 'scetris_server)
  val hwaddress = {
    /*import java.net.{NetworkInterface => ni}
    val hw = ni.getNetworkInterfaces.nextElement
    if (hw != null){
      hw.getHardwareAddress.foldRight(""){(x: Byte, y: String) =>
        ":" + String.format("%02X", x.asInstanceOf[java.lang.Byte]) + y
      }.drop(1)
    } else*/ "nick1" + System.currentTimeMillis
   }
}

class MultiplayerConf extends BasicGameState {
  def getID = States.MULTI
  
  val selector = Selector.getSelector(3)
  val choices = Map(1 -> States.CREATE_GAME, 2 -> States.JOIN_GAME, 3 -> States.MULTI_OPTIONS)
  
  def init(c: GameContainer, game: StateBasedGame) {
  }
  
  def render(c: GameContainer, game: StateBasedGame, g: Graphics) {
    centerText(g, props("multiplayer.welcome"), 250)
    drawString(g, props("multiplayer.menu.create"), 400, 300)
    drawString(g, props("multiplayer.menu.join"), 400, 330)
    drawString(g, props("main.menu.configuration"), 400, 360)
    selector.render
  }
  
  def update(c: GameContainer, game: StateBasedGame, delta: Int) {
    selector.update
    val input = c.getInput
    if (input.isKeyPressed(Input.KEY_UP)) selector.move(-30)
    if (input.isKeyPressed(Input.KEY_DOWN)) selector.move(30)
    if (input.isKeyPressed(Input.KEY_ESCAPE)) game.enterState(States.MAIN_MENU)
    if (input.isKeyPressed(Input.KEY_ENTER)) {
      game.getState(choices(selector.choice)).init(c, game)
      game.enterState(choices(selector.choice))
    }
  }
}

class MultiplayerOptions extends BasicGameState {
  def getID = States.MULTI_OPTIONS
  
  val inputBox = new Rectangle(400, 330, 130, 32) 
  var default = Server.ip
  var message = ""
  
  def init(c: GameContainer, game: StateBasedGame) {}
  def render(c: GameContainer, game: StateBasedGame, g: Graphics) {
    centerText(g, props("multiplayer.welcome"), 250)
    drawString(g, props("multiplayer.options.default"), 400, 300)
    g.setColor(Color.white)
    g.setLineWidth(2)
    g.draw(inputBox)
    drawString(g, default, 410, 335)
    centerText(g, message, 360)
  }
  
  def update(c: GameContainer, game: StateBasedGame, delta: Int) {
    val input = c.getInput
    if (input.isKeyPressed(Input.KEY_ESCAPE)) game.enterState(States.MULTI)
    if (input.isKeyPressed(Input.KEY_BACK)) default = default.take(default.size - 2)
    if (input.isKeyPressed(Input.KEY_ENTER)) {
      save(default)
      Server.ip = default
      message = "Changes saved!"
    }
  }
  
  override def keyPressed(i: Int, c: Char) {
    if (i != Input.KEY_ESCAPE && i != Input.KEY_BACK && i != Input.KEY_ENTER && i != Input.KEY_RETURN) default += c
  }
}

class MultiplayerCreate extends BasicGameState {
  def getID = States.CREATE_GAME
  
  val inputBox = new Rectangle(400, 330, 130, 32) 
  var gid = ""
  var message= ""
  
  def init(c: GameContainer, game: StateBasedGame) {}
  def render(c: GameContainer, game: StateBasedGame, g: Graphics) {
    centerText(g, props("multiplayer.welcome"), 250)
    drawString(g, props("multiplayer.menu.create"), 400, 300)
    drawString(g, message, 400, 360)
    g.setColor(Color.white)
    g.setLineWidth(2)
    g.draw(inputBox)
    drawString(g, gid, 410, 335)
  }
  
  def update(c: GameContainer, game: StateBasedGame, delta: Int) {
    val input = c.getInput
    if (input.isKeyPressed(Input.KEY_ESCAPE)) game.enterState(States.MULTI)
    if (input.isKeyPressed(Input.KEY_BACK)) gid = gid.take(gid.size - 2)
    if (input.isKeyPressed(Input.KEY_ENTER)) {
      gid = gid.take(gid.size -1)
      actor {
        val server = Server.connect
        server ! TestCreate(gid)
        receiveWithin(500) {
          case OK(id) => {
            Tetris.change_shift(-96)
            game.getState(States.MULTI_LEVEL).asInstanceOf[MultiLevel].networkGame(gid.toString, game, CreateGame(gid.toString, Server.hwaddress))
            game.enterState(States.MULTI_LEVEL, new FadeOutTransition, new FadeInTransition)
          }
          case _ => message = "Cannot create " + gid + " game!" 
        }
      }
    }
  }
  
  override def keyPressed(i: Int, c: Char) {
    if (i != Input.KEY_ESCAPE || i != Input.KEY_BACK || i != Input.KEY_ENTER){ 
      gid += c
    }
  }
}

class MultiJoin extends BasicGameState {
  def getID = States.JOIN_GAME

  var pendingGames: List[String] = List()
  var message= ""
  var selector = Selector.getSelector(1)
  
  def init(c: GameContainer, game: StateBasedGame) {}
  override def enter(c: GameContainer, game: StateBasedGame) {
    actor {
      val server = Server.connect
      server ! GetList()
      receiveWithin(500) {
        case GameList(ls) => pendingGames = ls; selector = Selector.getSelector(1 + pendingGames.size)
        case _ => message = "Cannot connect to server!"
      }
    }
  }
  def update(c: GameContainer, game: StateBasedGame, delta: Int) {
    selector.update
    val input = c.getInput
    if (input.isKeyPressed(Input.KEY_UP)) selector.move(-30)
    if (input.isKeyPressed(Input.KEY_DOWN)) selector.move(30)
    if (input.isKeyPressed(Input.KEY_ESCAPE)) game.enterState(States.MULTI)
    if (input.isKeyPressed(Input.KEY_ENTER)) {
      selector.choice match {
        case 1 => game.enterState(States.MULTI)
        case _ => {
          Tetris.change_shift(-96)
          game.getState(States.MULTI_LEVEL).asInstanceOf[MultiLevel].networkGame(pendingGames(selector.choice - 2), game, JoinGame(pendingGames(selector.choice -2), Server.hwaddress))
          game.enterState(States.MULTI_LEVEL, new FadeOutTransition, new FadeInTransition)
        }
      }
    }
  }
  def render(c: GameContainer, game: StateBasedGame, g: Graphics) {
    centerText(g, props("multiplayer.welcome"), 250)
    drawString(g, props("multiplayer.menu.join"), 400, 270)
    drawString(g, props("multiplayer.menu.back"), 400, 300)
    var y = 300
    for (gm <- pendingGames) {
      y += 30
      drawString(g, gm, 400, y)
    }
    centerText(g, message, y + 30)
    selector.render
  }
}

class MultiLevel() extends BasicGameState {
  def getID = States.MULTI_LEVEL
  val map = maps("test2.tmx")
  var game: Game = null
  var client: Actor = null
  var gid = ""
  var opponentPiece: Tetris = Tetris.empty
  var opponentBlocks: List[Block] = List()
  var opponentSwap: Tetris = Tetris.empty
  var waiting = true
  val rand = new java.util.Random()
  
  import java.io.FileWriter
  def debug(msg: String)(fun: => Unit) {
    val writer = new FileWriter("logs", true)
    writer.write(msg)
    writer.write("\n")
    writer.close
    fun
  }
  
  def networkGame(gid: String, g: StateBasedGame, initial: Any) {
    opponentPiece = Tetris.empty
    opponentBlocks = List()
    opponentSwap = Tetris.empty
    waiting = true
    
    this.gid = gid
    client = actor {
        val server = Server.connect
        server ! initial
        loop {
          // TODO: puased and resumed cases
          react {
            case Loser(id) => Tetris.reset_shift; g.enterState(States.MULTI, new RotateTransition, new FadeInTransition)
            case Paused() => g.enterState(States.MULTI_LEVEL_QUIT)
            case Resumed() => g.enterState(getID)
            case Winner(id) => Tetris.reset_shift; g.enterState(States.GAME_OVER, new RotateTransition, new FadeInTransition)
            case Waiting() => debug("Waiting") {waiting = true}
            case Start() => waiting= false; game.clear
            case PieceCreate(i) => debug("Opponent") {opponentPiece = Tetris.create(i, 19 * 32)}
            case PieceSwap(i) => {
              var swapped = opponentPiece
              if (opponentSwap.isInstanceOf[Empty]) opponentSwap = Tetris.create(i, 19 * 32)
              opponentPiece = opponentSwap
              game.swapLocation(opponentPiece, swapped)
              opponentSwap = swapped
              opponentSwap.init(-96, -96)
            }
            case PieceMove(coord) => debug("Opponent Move") {opponentPiece.init(coord._1 + 448, coord._2)}
            case PieceRotate(pos) => {
              debug("Rotate " + opponentPiece.blocks(0).rect.getX + " " + opponentPiece.blocks(0).rect.getY) ()
              opponentPiece.position = pos
              opponentPiece.rotate(0)
              opponentPiece.init(opponentPiece.blocks(0).rect.getX, opponentPiece.blocks(0).rect.getY)
            }
            case PieceDropped() => {
              debug("Dropped " + opponentBlocks.size) {
                opponentBlocks = opponentBlocks ::: opponentPiece.blocks
              }
              var initial_drop = 0
              opponentPiece.blocks.foreach( b => {
                //line achieved
                val (line, rest) = opponentBlocks.partition(_.rect.getY == b.rect.getY)
                if (line.size >= 10) {
                  initial_drop += 1
                  opponentBlocks = rest
                  game.dropRow(opponentBlocks, b.rect.getY)
                }
              })
              val usage =  if (initial_drop == 4) initial_drop else Math.max(0, initial_drop - 1)
              val r = rand.nextInt(10)
              for (n <- 0 until usage) { 
                game.localBlocks = game.localBlocks ::: game.addRow(game.localBlocks, 1, (1 until 11)(r)).toList
                // trigger redraw shadows
                game.currentPiece.hasMoved = true
              }
            }
            // Maybe I don't need these?
            case GameOver(id, pid) => server ! GameOver(id, Server.hwaddress)
            case Movement(id, pid, tup) => debug("Client Movement") {server ! Movement(id, pid, tup)}
            case Dropped(id, pid) => server ! Dropped(id, pid)
            case Rotate(id, pid, pos) => server ! Rotate(id, pid, pos)
            case Create(id, pid, i) => server !Create(id, pid, i)
            case Swap(id, pid, x) => server ! Swap(id, pid, x)
            case Pause(id) => debug("Client Pause") {server ! Pause(id)}
            case Resume(id) => server ! Resume(id)
            case _ => println("Signal quit; something's wrong")
          }
        }
    }
    
    game = new Game(g) {
      def gameOver() {
        if (waiting) clear else client ! GameOver(gid, Server.hwaddress)
      }
      def movementHandler() {
        val tup = (game.currentPiece.blocks(0).rect.getX.toInt, game.currentPiece.blocks(0).rect.getY.toInt)
        client ! Movement(gid, Server.hwaddress, tup)
      }
      def droppedHandler(lines: Int) {
        val usage = if (lines == 4) lines else Math.max(0, lines - 1)
        val r = rand.nextInt(10)
        if (!waiting) {
          for (n <- 0 until usage) { 
            opponentBlocks = opponentBlocks ::: addRow(opponentBlocks, 15, (15 until 25)(r)).toList
          }
          client ! Dropped(gid, Server.hwaddress)
        }
      }
      def rotateHandler() {
        client ! Rotate(gid, Server.hwaddress, game.currentPiece.position)
      }
      def creationHandler() {
        client ! Create(gid, Server.hwaddress, Tetris.colors.findIndexOf(_ == game.currentPiece.c))
      }
      def swapHandler() {
        client ! Swap(gid, Server.hwaddress ,Tetris.colors.findIndexOf(_ == game.currentPiece.c))
      }
    }
  }

  def init(c: GameContainer, g: StateBasedGame) {
    game = new Game(g) {
      def gameOver() {
        clear
      }
      def droppedHandler(lines: Int) {}
      def movementHandler() {}
      def rotateHandler() {}
      def swapHandler() {}
      def creationHandler() {}
    }
  }
  
  def update(c: GameContainer, g: StateBasedGame, delta: Int) {
    val input = c.getInput
    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      client ! Pause(gid)
    }
    game.update(c.getInput)
  }
  
  def render(c: GameContainer, g: StateBasedGame, gr: Graphics){
    map.render(0,0)
    game.render(gr)
    opponentPiece.render(gr)
    opponentBlocks.foreach(_.draw)
  }
}

class MultiQuit extends BasicGameState {
  def getID = States.MULTI_LEVEL_QUIT
  val map = maps("test2.tmx")
  val selector = Selector.getSelector(2)
  
  def init(c: GameContainer, game: StateBasedGame) {}
  def update(c: GameContainer, game: StateBasedGame, delta: Int){
    selector.update
    val input = c.getInput
    if (input.isKeyPressed(Input.KEY_UP)) selector.move(-30)
    if (input.isKeyPressed(Input.KEY_DOWN)) selector.move(30)
    if (input.isKeyPressed(Input.KEY_ENTER)) {
      val multi = game.getState(States.MULTI_LEVEL).asInstanceOf[MultiLevel]
      selector.choice match {
        case 1 => multi.client ! Resume(multi.gid)
        case 2 => {
          Tetris.reset_shift
          if (multi.waiting) {
            Server.connect ! Leave(multi.gid)
            game.enterState(States.MULTI)
          } else {
            multi.client ! GameOver(multi.gid, Server.hwaddress)
          }
        }
      }
    }
  }
  def render(c: GameContainer, game: StateBasedGame, g: Graphics) {
    map.render(0,0)
    drawString(g, props("quit.menu.message"), 200, 275)
    drawString(g, props("quit.menu.no") , 400, 300)
    drawString(g, props("quit.menu.yes"), 400, 330)
    selector.render
  }
}
