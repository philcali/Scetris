package com.philipcali.tetris.game

import com.philipcali.tetris.pieces.{Tetris, Empty, Block}
import com.philipcali.tetris.gamedata.Storage.{loadControls=> controls}
import org.newdawn.slick.geom.Rectangle
import org.newdawn.slick.Color
import org.newdawn.slick.Graphics
import org.newdawn.slick.Input
import org.newdawn.slick.state.StateBasedGame

abstract class Game(game: StateBasedGame) {
  var currentPiece = Tetris.pop()
  var storedPiece: Tetris = new Empty() 
  var localBlocks = List[Block]()
  var destroyingblocks = List[Block]()
  var drawShadows = true
  var con = controls
  
  def update(input: Input) {
    val destroying = destroyingblocks.size != 0
    
    // handle destrucion
    if (destroying) {
      destroyingblocks.foreach(_.update)
      // destroyed so 
      if (destroyingblocks.exists(_.destroyed)) {
        val ys = for (b <- destroyingblocks) yield(b.rect.getY)
        ys.removeDuplicates.sort((a,b) => a < b).foreach(y => dropRow(localBlocks, y))
        // reset our destroying line
        destroyingblocks = List[Block]()
        //currentPiece = Tetris.pop
      }
    }
    
    if(!destroying) {
      currentPiece.update(localBlocks)
      currentPiece.drawShadows = drawShadows
    }
    
    if (currentPiece.dropTimer > 60) {
      currentPiece.moveDown
      movementHandler
    }
    
     // the piece is dropped, so trigger next piece
    if (currentPiece.dropped && currentPiece.blocks.head.rect.getY < 0) {
      // game over condition
      gameOver
    } else if (currentPiece.dropped) {
      localBlocks = localBlocks ::: currentPiece.blocks
      var initial_drop = 0
      currentPiece.blocks.foreach( b => {
        // line achieved
        val (line, rest) = localBlocks.partition(_.rect.getY == b.rect.getY)
        if (line.size >= 10) {
          initial_drop += 1
          localBlocks = rest
          
          // set to destroy
          line.foreach(_.destroying = true) 
          
          destroyingblocks = destroyingblocks ::: line
          //dropRow(b.rect.getY)
        }
      })
      droppedHandler(initial_drop)
      
      currentPiece = Tetris.pop()
      creationHandler
    }
    
    if (currentPiece.animating) {
      input.clearKeyPressedRecord
    }

    if (input.isKeyPressed(con.get("drop").getOrElse(Input.KEY_S))) {
      currentPiece.moveDown
      movementHandler
    }
    
    if (input.isKeyPressed(con.get("fall").getOrElse(Input.KEY_W))) {
      currentPiece.fall
      movementHandler
    }
    
    if (input.isKeyPressed(con.get("left").getOrElse(Input.KEY_A))) {
      currentPiece.moveHorizontally(-32, localBlocks)
      movementHandler
    }
    
    if (input.isKeyPressed(con.get("right").getOrElse(Input.KEY_D))) {
      currentPiece.moveHorizontally(32, localBlocks)
      movementHandler
    }
    
    if (input.isKeyPressed(con.get("rotate").getOrElse(Input.KEY_J))) {
      currentPiece.rotate(-1)
      rotateHandler
    }
    
    /*
    if (input.isKeyPressed(Input.KEY_K)) {
      currentPiece.rotate(1)
    }*/
    
    if (input.isKeyPressed(con.get("shadow").getOrElse(Input.KEY_B))) {
      drawShadows = !drawShadows
    }
    
    // store
    if (input.isKeyPressed(con.get("storage").getOrElse(Input.KEY_L))) {
      if (storedPiece.isInstanceOf[Empty]) {
        storedPiece = currentPiece
        currentPiece = Tetris.pop()
        creationHandler
        swapLocation(currentPiece, storedPiece)
      } else {
        var swapped = currentPiece
        currentPiece = storedPiece
        swapLocation(currentPiece, swapped)
        storedPiece = swapped
        swapHandler
      }
       storedPiece.init(480 + Tetris.shift, 160 * 3)
       storedPiece.drawShadows= false
       currentPiece.reset
       movementHandler
    }
  }
  
  def gameOver()
  
  def droppedHandler(lines: Int)
  
  def rotateHandler()
  
  def movementHandler()
  
  def creationHandler()
  
  def swapHandler()
  
  def clear() {
    currentPiece = Tetris.pop()
    creationHandler
    storedPiece = Tetris.empty
    localBlocks = List[Block]()
    destroyingblocks = List[Block]()
    drawShadows = true
  }
  
  def dropRow(localBlocks:List[Block], y: Float) {
    localBlocks.foreach(b => {
      if(b.rect.getY < y) { 
        b.rect.setY(b.rect.getY + 32)
      }
    })
  }
  
  def addRow(blocks: List[Block], start: Int, empty_x: Int) = {
    blocks.foreach(b => b.rect.setY(b.rect.getY - 32))
    val ret = for(x <- (start until start + 10).filter(_ != empty_x)) yield(new Block(0, new Rectangle(x * 32, 568, 32, 31), Color.orange))
    ret
  }
  
  def swapLocation(a: Tetris, b: Tetris) {
    a.init(b.blocks.head.rect.getX, b.blocks.head.rect.getY)
  }
  
  def render(g: Graphics) {
    currentPiece.render(g)
    storedPiece.render(g)
    Tetris.incomingTetris.foreach(t => t.render(g))
    localBlocks.foreach(b => b.draw)
    destroyingblocks.foreach(b => b.drawspecial(b.dtimer))
  }
}
