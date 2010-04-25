package com.philipcali.tetris.state

import org.newdawn.slick.state.BasicGameState
import org.newdawn.slick.state.StateBasedGame
import org.newdawn.slick.GameContainer
import org.newdawn.slick.Graphics
import org.newdawn.slick.Color
import org.newdawn.slick.Input
import org.newdawn.slick.Image
import org.lwjgl.input.Keyboard
import org.newdawn.slick.state.transition.FadeOutTransition
import org.newdawn.slick.state.transition.FadeInTransition
import org.newdawn.slick.state.transition.RotateTransition
import com.philipcali.tetris.util.MessageUtil.{props, centerText, drawString, printControls => pcon}
import com.philipcali.tetris.pieces.{Tetris, Block, Empty}
import org.newdawn.slick.geom.Rectangle
import com.philipcali.tetris.gamedata.Storage.{loadData => load, saveData=> save, loadControls=> controls, saveControls}
import com.philipcali.tetris.game.Game
import com.philipcali.tetris.util.ResourceLoader.maps

object States {
  val LOGO = 1
  val MAIN_MENU = 2
  val QUIT_MENU = 3
  val GAME_OVER = 4
  val LEVEL = 5
  val HELP = 6
  val LAST_RUN = 7
  val CONFIGURE = 8
  val MULTI = 9
  val CREATE_GAME = 10
  val JOIN_GAME = 11
  val MULTI_LEVEL= 12
  val MULTI_LEVEL_QUIT= 13
  val MULTI_LEVEL_GAME_OVER= 14
  val MULTI_OPTIONS = 15
}

class MainMenu extends BasicGameState {
  def getID() = States.MAIN_MENU
  
  var stage = 1
  val selector = Selector.getSelector(4)
  val choices= Map(1 -> States.LEVEL, 2 -> States.LAST_RUN, 3 -> States.MULTI, 4 -> States.CONFIGURE)
  
  def update(container: GameContainer, game: StateBasedGame, delta: Int) {
    val input = container.getInput
    
    selector.update
    
    if (input.isKeyPressed(Input.KEY_ENTER)) {
      input.clearKeyPressedRecord
      // reset our level
      // reset our factory
      Tetris.reset(stage)
      Tetris.change_shift(0)
      game.getState(choices(selector.choice)).init(container, game)
      game.enterState(choices(selector.choice), new FadeOutTransition, new FadeInTransition)
    }
    
    if (input.isKeyPressed(Input.KEY_DOWN)) {
      selector.move(30)
    }
    
    if (input.isKeyPressed(Input.KEY_UP)) {
      selector.move(-30)
    }
    
    if (input.isKeyPressed(Input.KEY_LEFT) && selector.choice == 1) {
      changestage(-1)
    }
    
    if (input.isKeyPressed(Input.KEY_RIGHT) && selector.choice == 1) {
      changestage(1)
    }
    
    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      container.exit
    }
  }
  
  def changestage(value: Int) {
    if ((value > 0 && stage != Tetris.max_stage) || (value < 0 && stage != Tetris.min_stage)) {
      stage += value
    }
  }
  
  def init(container: GameContainer, game: StateBasedGame) {
    
  }
  
  def render(container: GameContainer, game: StateBasedGame, g: Graphics) {
    centerText(g, props("main.menu.welcome"), 200)
    centerText(g, props("main.menu.press_enter"), 250)
    drawString(g, props("main.menu.select.stage") + stage, 400, 300)
    drawString(g, props("last.run.header"), 400, 330)
    drawString(g, props("main.menu.multiplayer"), 400, 360)
    drawString(g, props("main.menu.configuration"), 400, 390)
    this.selector.render
  }
  
}

class QuitMenu extends BasicGameState {
  def getID() = States.QUIT_MENU
  
  val map = maps("test.tmx")
  val selector = Selector.getSelector(2)
  
  def update(container: GameContainer, game: StateBasedGame, delta: Int) {
    val input = container.getInput
    
    selector.update
    
    if (input.isKeyPressed(Input.KEY_ENTER)) {
      selector.choice match {
        case 1 => game.enterState(States.LEVEL)
        case 2 => {
          input.clearKeyPressedRecord
          game.getState(States.LEVEL).asInstanceOf[Level].quit
          game.getState(States.LAST_RUN).init(container, game)
          game.enterState(States.LAST_RUN, new FadeOutTransition, new FadeInTransition)
        }
      }
    }
    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      game.enterState(States.LEVEL)
    }
    
    if (input.isKeyPressed(Input.KEY_UP)) {
      selector.move(-30)
    }
    
    if (input.isKeyPressed(Input.KEY_DOWN)) {
      selector.move(30)
    }
  }
  
  def init(container: GameContainer, game: StateBasedGame) {
    
  }
  
  def render(container: GameContainer, game: StateBasedGame, g: Graphics) {
    map.render(0,0)
    drawString(g, props("quit.menu.message"), 200, 275)
    drawString(g, props("quit.menu.no") , 400, 300)
    drawString(g, props("quit.menu.yes"), 400, 330)
    selector.render
  }
}

class GameOver extends BasicGameState {
  def getID() = States.GAME_OVER
  
  def update(container: GameContainer, game: StateBasedGame, delta: Int) {
    val input = container.getInput
    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      input.clearKeyPressedRecord
      game.getState(States.LAST_RUN).init(container, game)
      game.enterState(States.LAST_RUN, new FadeOutTransition, new FadeInTransition)
    }
  }
  
  def init(container: GameContainer, game: StateBasedGame) {
    
  }
  
  def render(container: GameContainer, game: StateBasedGame, g: Graphics) {
    g.setColor(Color.white)
    g.drawString(props("game.over.message"), 350, 275)
  }
}

class Level extends BasicGameState {
  def getID() = States.LEVEL
  val map = maps("test.tmx")
  var game:Game = null
  var score = 0
  var lines= 0
  var tetris = 0
  
  def init(c: GameContainer, g: StateBasedGame) {
    c.getInput.enableKeyRepeat(1, 16)
    score = 0
    lines= 0
    tetris = 0
    game = new Game(g) {
      def droppedHandler(initial_drop: Int) {
         score += 11
      
         val increment = if(initial_drop == 4) 1 else 0
         tetris += increment
      
         if (initial_drop > 0) {
           lines += initial_drop
           score += (115 * ((initial_drop) + (1 * initial_drop-1)))
         }
      
         if (score > 2000 * (1 + Tetris.stage) && Tetris.stage < Tetris.max_stage) {
           Tetris.stage += 1
         }
      }
      
      def gameOver() {
        quit
        g.enterState(States.GAME_OVER, new RotateTransition, new FadeInTransition)
      }
      
      def movementHandler() {}
      def rotateHandler() {}
      def swapHandler() {}
      def creationHandler() {}
    }
  }
  
  def update(c: GameContainer, g: StateBasedGame, delta: Int) {
    val input = c.getInput
    
    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      g.enterState(States.QUIT_MENU)
    }
    
    if (input.isKeyPressed(Input.KEY_COMMA)) {
      g.enterState(States.HELP)
    }
    
    game.update(input)
  }
  
  def render(c: GameContainer, g: StateBasedGame, gr: Graphics) {
    map.render(0,0)
    drawString(gr, props("help"), 20, 40)
    drawString(gr, props("level.lines") + " " + lines, 462, 20)
    drawString(gr, props("level.score") + " " + score, 462, 40)
    drawString(gr, props("level.stage") + " " + Tetris.stage, 550, 20)
    drawString(gr, props("level.tetris") + " " + tetris, 550, 40)
    drawString(gr, props("level.storage"), 462, 395)
    game.render(gr)
  }
  
  def quit() {
    save(score, lines, Tetris.stage, tetris ,load)
  }
}

class Help extends BasicGameState {
  def getID = States.HELP
  
  val map = maps("test.tmx")
  var con = controls
  
  def render(c: GameContainer, game: StateBasedGame, g:Graphics) {
    map.render(0,0)
    drawString(g, props("help.title"), 200, 40)
    pcon(g, con)
    drawString(g, props("help.quit")+"'esc'", 200, 310)
  }
  
  def init(c: GameContainer, game: StateBasedGame) {
    
  }
  
  def update(c:GameContainer, game: StateBasedGame, delta: Int) {
    val input = c.getInput
    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      game.enterState(States.LEVEL)
    }
  }
}

class LastRun extends BasicGameState {
  def getID = States.LAST_RUN
  
  var oldData = load
  
  def render(c: GameContainer, game: StateBasedGame, g:Graphics) {
    centerText(g, props("last.run.header"), 40)
    drawString(g, props("last.run") + props("level.lines") + oldData.get("last.run.lines").getOrElse(0), 128, 128)
    drawString(g, props("last.run") + props("level.score") + oldData.get("last.run.score").getOrElse(0), 128, 158)
    drawString(g, props("last.run") + props("level.tetris") + oldData.get("last.run.tetris").getOrElse(0), 128, 188)
    drawString(g, props("last.run") + props("level.stage") + oldData.get("last.run.stage").getOrElse(0), 128, 218)
    drawString(g, props("total.run") + props("level.lines") + oldData.get("total.lines").getOrElse(0), 465, 128)
    drawString(g, props("total.run") + props("level.score") + oldData.get("total.score").getOrElse(0), 465, 158)
    drawString(g, props("total.run") + props("level.tetris") + oldData.get("total.tetris").getOrElse(0), 465, 188)
    centerText(g, props("highest.run") + props("level.lines") + oldData.get("highest.lines").getOrElse(0), 258)
    centerText(g, props("highest.run") + props("level.score") + oldData.get("highest.score").getOrElse(0), 288)
    centerText(g, props("highest.run") + props("level.tetris") + oldData.get("highest.tetris").getOrElse(0), 318)
  }
  
  def init(c: GameContainer, game: StateBasedGame) {
    oldData = load
  }
  
  def update(c:GameContainer, game: StateBasedGame, delta: Int) {
    val input = c.getInput
    if (input.isKeyPressed(Input.KEY_ESCAPE)) {
      input.clearKeyPressedRecord
      game.enterState(States.MAIN_MENU, new FadeOutTransition, new FadeInTransition)
    }
  }
}

class Configuration() extends BasicGameState {
  def getID = States.CONFIGURE
  
  var oldControls = controls
  val map = maps("test.tmx")
  val selector = Selector.getSelector(7)
  var waiting = false
  val controlMap = Map(1 -> "right", 2-> "left", 3->"drop", 4->"fall", 5->"rotate", 6-> "storage", 7->"shadow")
  
  def update(c:GameContainer, game: StateBasedGame, delta: Int) {
    val input = c.getInput
    
    selector.update
    
    if (input.isKeyPressed(Input.KEY_DOWN)) {
      selector.move(30)
    }
   
    if (input.isKeyPressed(Input.KEY_UP)) {
      selector.move(-30)
    }
    
    if (input.isKeyPressed(Input.KEY_ENTER)) {
      waiting = true
      selector.animate = true
    }
    
    if (input.isKeyPressed(Input.KEY_ESCAPE) && !waiting) {
      game.enterState(States.MAIN_MENU, new FadeOutTransition, new FadeInTransition)
    }
  }
  
  
  override def keyPressed (i: Int, c: Char) {
    if (waiting && i != Input.KEY_ESCAPE) {
      val key = controlMap(selector.choice)
      val old = for((k, v) <- oldControls; if(k != key)) yield((k , v))
      oldControls = Map((key -> i)) ++ old.toList
      waiting = false
      selector.animate = false
      selector.reset
    }
  }
  
  override def leave(c: GameContainer, game: StateBasedGame) {
    saveControls(oldControls)
  }
  
  def init(c: GameContainer, game: StateBasedGame) {
    selector.animate = false
    selector.choice = 1
    selector.initialx = 165
    selector.rect.setX(165)
    selector.rect.setY(100)
  }
  
  def render(c: GameContainer, game: StateBasedGame, g:Graphics) {
    map.render(0,0)
    drawString(g, props("main.menu.configuration"), 200, 40)
    pcon(g, oldControls)
    selector.render
  }
}

class Splash extends BasicGameState {
  def getID = States.LOGO
  
  val logo = new Image("res/slick_logo.gif")
  var timer = 200
  
  def init (c: GameContainer, game: StateBasedGame) {
    
  }
  def render(c: GameContainer, game: StateBasedGame, g:Graphics) {
    centerText(g, props("company"), 200)
    centerText(g, props("powered"), 230)
    logo.draw(300, 275)
  }
  def update(c:GameContainer, game: StateBasedGame, delta: Int) {
    val input = c.getInput
    if (input.isKeyPressed(Input.KEY_ESCAPE) || input.isKeyPressed(Input.KEY_ENTER)) {
      timer = 0
    }
    
    if (timer <= 0) {
      game.enterState(States.MAIN_MENU, new FadeOutTransition, new FadeInTransition)
    }
    
    timer -= 1
  }
}
