package com.philipcali.tetris

import org.newdawn.slick.state.StateBasedGame
import org.newdawn.slick.GameContainer
import com.philipcali.tetris.state._
import com.philipcali.tetris.gamedata.Storage.initialize
import com.philipcali.server.Server

class Scetris extends StateBasedGame("Scetris") {
  // Java hold over; I hate this kinda crap
  var container: GameContainer = null
  
  def initStatesList(parent_container: GameContainer) {
    initialize
    this.container = parent_container
    
    container.setTargetFrameRate(100)
    container.setVSync(true)
    
    addState(new Splash)
    addState(new MainMenu)
    addState(new QuitMenu)
    addState(new GameOver)
    addState(new Level)
    addState(new LastRun)
    addState(new Help)
    addState(new Configuration)
    addState(new MultiplayerConf)
    addState(new MultiplayerCreate)
    addState(new MultiJoin)
    addState(new MultiLevel)
    addState(new MultiQuit)
    addState(new MultiplayerOptions)
  }
}
