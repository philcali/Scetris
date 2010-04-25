package com.philipcali.tetris.pieces

import org.newdawn.slick.Color
import org.newdawn.slick.Image
import org.newdawn.slick.Graphics
import java.util.Random
import org.newdawn.slick.geom.Rectangle

object Tetris {
  val shapes = List(new Square, new TPiece, new LPiece, new LinePiece, new ReverseLPiece, new ReverseZPiece, new ZPiece)
  val colors = List(Color.red, Color.orange, Color.yellow, Color.cyan, Color.green, Color.pink, Color.magenta)
  val random = new Random()
  var shift = 0
  val leftBounds = new Rectangle(0 + shift, -32, 128, 632)
  val rightBounds = new Rectangle(448 + shift, -32, 1, 632)
  var stage = 1
  val max_stage = 10
  val min_stage = 1
  
  val block_image = new Image("res/orig_block.png") 
  var incomingTetris = List(createPiece(), createPiece())
  
  private def createPiece() = {
    val index = random.nextInt(shapes.size)
    new Tetris(shapes(index), colors(index), stage)
  }
  
  def reset(stage: Int) {
    this.stage = stage
    incomingTetris = createPiece :: createPiece :: Nil
  }
  
  def change_shift(x: Int) {
    shift += x
    leftBounds.setX(leftBounds.getX + x)
    rightBounds.setX(rightBounds.getX + x)
  }
  
  def reset_shift() {
    change_shift(-shift)
  }
  
  def init() {
    val x = 480 + shift
    val y = 160
    for (i <- 1 to incomingTetris.size) {
      incomingTetris(i-1).init(x, y * i)
    }
  }
  
  def pop() = {
    val (nextPiece :: rest) = incomingTetris
    nextPiece.init(256 + shift, -8)
    incomingTetris = rest ::: createPiece :: Nil
    init
    nextPiece.drawShadows = true
    nextPiece
  }
  
  def empty[Tetris] = {
    new Empty
  }
  
  def create(i: Int, x: Int) = {
    val piece = new Tetris(shapes(i), colors(i), stage)
    piece.init(x, -8)
    piece.hasMoved = false
    piece.droppable = false
    piece
  }
}

trait Definition {
  val coord: List[(Int, Int)]
  def position1: List[(Int, Int)]
  def position2: List[(Int, Int)]
  def position3: List[(Int, Int)]
  def position4: List[(Int, Int)]
}
class Square extends Definition {
  val coord = List((0,0), (0, -32), (32, 0), (32, -32), (600,600))
  def position1 = coord
  def position2 = coord
  def position3 = coord
  def position4 = coord
}

class TPiece extends Definition {
  val coord = List((0, 0), (32, 0), (0, -32), (-32, 0), (600,600))
  val coord2 = List(coord(0), (0, -32), (32, 0), (0, 32), (568,568))
  val coord3 = List(coord(0), coord(1), (0, 32), coord(3), (568,568))
  val coord4 = List(coord(0), coord2(1), (-32, 0), coord2(3), (568,568))
  def position1 = coord
  def position2 = coord2
  def position3 = coord3
  def position4 = coord4
}

class LPiece extends Definition {
  val coord = List((0, 0),(0, -32), (0, 32), (32, 32), (568,568))
  val coord2 = List((0,0), (-32, 0), (32, 0), (32, -32), (600,600))
  val coord3 = List((0,0), (-32, -32), (0, -32), (0, 32), (568,568))
  val coord4 = List((0,0), (-32, 0), (-32, 32), (32, 0), (568,568))
  def position1 = coord
  def position2 = coord2
  def position3 = coord3
  def position4 = coord4
}

class LinePiece extends Definition {
  val coord = List((0, 0), (0, 32), (0, -32), (0, -64), (568,568))
  val coord2 = List((0, 0), (-32, 0), (32, 0), (64, 0), (600,600))
  def position1 = coord
  def position2 = coord2
  def position3 = coord
  def position4 = coord2
}

class ZPiece extends Definition {
  val coord = List((0, 0), (-32, -32), (0, -32), (32, 0), (600,600))
  val coord2 = List((0 ,0) , (32, 0), (32, -32), (0, 32), (568,568))
  def position1 = coord
  def position2 = coord2
  def position3 = coord
  def position4 = coord2
}

class ReverseLPiece extends Definition {
  val coord = List((0, 0), (0, -32), (0, 32), (-32, 32),(568,568))
  val coord2 = List((0, 0), (32, 0), (-32, 0), (32, 32), (568,568))
  val coord3 = List((0, 0),  (32, -32), (0, -32), (0, 32),(568,568))
  val coord4 = List((0, 0),  (-32, 0), (32, 0), (-32, -32),(600,600))
  def position1 = coord
  def position2 = coord2
  def position3 = coord3
  def position4 = coord4
}

class ReverseZPiece extends Definition {
  val coord = List((0, 0), (0, -32), (32, -32), (-32, 0), (600,600))
  val coord2 = List((0,0), (0, -32), (32, 0), (32, 32), (568,568))
  def position1 = coord
  def position2 = coord2
  def position3 = coord
  def position4 = coord2
}

class Block(val id:Int, var rect: Rectangle, val c: Color)  {
  val image = Tetris.block_image
  var destroyed = false
  var destroying = false
  var dtimer = 0
  var max_detroy = 20
  
  def isColliding(rect: Rectangle, direction: Int) = {
    if (direction < 0){
      this.rect.getX == rect.getX + rect.getWidth && rect.getY == this.rect.getY
    } else {
      this.rect.getX + this.rect.getWidth == rect.getX && rect.getY == this.rect.getY
    }
  }
  
  def isCollidingBorders(rect: Rectangle) = {
    this.rect.intersects(rect)
  }
  
  def isDropped(rect: Rectangle) = {
    this.rect.getY == rect.getY && rect.getX == this.rect.getX
  }

  def update() {
    dtimer += 1
    
    if (dtimer >= max_detroy) {
      destroyed = true
    }
  }
  
  def drawspecial(timer: Int) {
    val amount = (timer * 10) 
    image.draw(rect.getX, rect.getY, new Color(c.getRed + amount, c.getGreen + amount, c.getBlue + amount))
  }
  
  def draw() {
    image.draw(rect.getX, rect.getY, c)
  }
}

class Tetris(val d: Definition, val c: Color, var stage:Int) {
  // shadow related
  var drawShadows = false
  var hasMoved = true
  // dropped
  var dropped = false
  var dropTimer = 0
  // AI or other player is false
  var droppable = true
  // animations
  var animating = false
  var max_animation = 20
  var animation_timer= max_animation
  // visibility
  val blocks = List(new Block(0, new Rectangle(0, 0, 32, 31), c), new Block(1, new Rectangle(0,0, 32, 31), c), new Block(2, new Rectangle(0,0,32,31), c), new Block(3, new Rectangle(0,0,32,31), c))
  val shadowblocks = List(new Block(0, new Rectangle(-32, 0, 32, 31), c), new Block(1, new Rectangle(-32,0, 32, 31), c), new Block(2, new Rectangle(-32,0,32,31), c), new Block(3, new Rectangle(-32,0,32,31),c))
  var position = 1
  var coord = d.position1 _
  
  def findShadow(l: List[Block]) {
    val default = this.coord()(4)._1
    var height = default
    val sorted_blocks = blocks.sort((b1, b2) => b1.rect.getY > b2.rect.getY)
    sorted_blocks.foreach(b => {
      val filtered = l.filter(x => x.rect.getX == b.rect.getX && x.rect.getY > b.rect.getY).sort((y1, y2) => y1.rect.getY < y2.rect.getY)
      height = filtered.foldLeft(height)((x, y) => if(y.rect.getY <= x){ val g = y.rect.getY.toInt + (-1 * this.coord()(b.id)._2); if(g<x)g else x} else x)
                })
    height = if (height > default) default else height
    for (i <- 0 until blocks.size) {
      shadowblocks(i).rect.setX(blocks(i).rect.getX)
      shadowblocks(i).rect.setY(height - 32 + this.coord()(i)._2)
    }
  }
  
  def rotate(value: Int) {
    position += value
    position = if (position == 0) 4 else position
    position = if (position == 5) 1 else position
    position match {
      case 1 => this.coord = d.position1 _
      case 2 => this.coord = d.position2 _
      case 3 => this.coord = d.position3 _
      case 4 => this.coord = d.position4 _
    }
    init(blocks(0).rect.getX, blocks(0).rect.getY)
    this.hasMoved = true
  }
  
  def inNegativeSpace() {
    // in left negative
    if(blocks.exists(b => b.rect.getX < Tetris.leftBounds.getWidth + Tetris.leftBounds.getX)){
      init((Tetris.leftBounds.getWidth + Tetris.leftBounds.getX) + 32, blocks(0).rect.getY)
    } else if (blocks.exists(b => b.rect.getX > Tetris.rightBounds.getX - 18)) {
      init(Tetris.rightBounds.getX - 64, blocks(0).rect.getY)
    }
  }
  
  def isDropped() = {
    dropped
  }
  
  def fall = {
    for(i <- 0 until blocks.size){
      blocks(i).rect.setX(shadowblocks(i).rect.getX)
      blocks(i).rect.setY(shadowblocks(i).rect.getY)
    }
    moveDown
  }
  
  // move the piece down a row
  def moveDown() {
    animating = blocks(0).isDropped(shadowblocks(0).rect)
    if (!animating && !dropped) {
      blocks.foreach(b => b.rect.setY(b.rect.getY + 32))
    }
  }
  
  def reset() {
    this.hasMoved = true
    this.drawShadows = true
  }
  
  // move the piece one column
  def moveHorizontally(x: Int, l: List[Block]) {
    if(!pieceCollide(x, l) && !borderCollide(x)) {
      blocks.foreach(b => b.rect.setX(b.rect.getX + x))
      hasMoved = true
    }
  }
  
  def borderCollide(x:Int) = {
    if (x < 0) {
      blocks.exists(b => b.isCollidingBorders(Tetris.leftBounds))
    } else {
      blocks.exists(b => b.isCollidingBorders(Tetris.rightBounds))
    }
  }
  
  def pieceCollide(x: Int, l: List[Block]) = {
    var collide = false
    blocks.foreach(b => {
      if (!collide) {
        collide = l.exists(lb => b.isColliding(lb.rect, x))
      }
    })
    collide
  }
  
  def update(l : List[Block]) {
    if (hasMoved) {
      inNegativeSpace
      hasMoved = false
      findShadow(l)
    }

    if (animating) {
      animation_timer -= 1
    }
    
    if (animation_timer <= 0) {
      dropped = true
    }
    
    if (dropTimer <= 60) {
      dropTimer += stage
    } else if (dropTimer > 60 && droppable) {
      dropTimer = 0
    }
  }
  
  // Starting point
  def init(x: Float, y: Float) {
    for(i <- 0 until blocks.size) {
      blocks(i).rect.setX(this.coord()(i)._1 + x)
      blocks(i).rect.setY(this.coord()(i)._2 + y)
    }
    
  }
  
  def render(g: Graphics) {
    blocks.foreach (b=> { 
      if (!animating) b.draw else b.drawspecial(animation_timer)
    })
    
    if (drawShadows && !animating) {
      shadowblocks.foreach (b => {
        g.setColor(b.c)
        g.setLineWidth(2)
        g.draw(b.rect)
      })
    }
  }
}

class Empty extends Tetris(new Square, Color.white, 1) {
  droppable = false
  override val blocks = List()
  
  // draw a Question mark
  override def render(g: Graphics) {
    
  }
}
