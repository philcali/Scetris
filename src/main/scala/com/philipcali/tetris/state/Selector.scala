package com.philipcali.tetris.state

import org.newdawn.slick.Image
import org.newdawn.slick.geom.Rectangle

object Selector {
  val image = new Image("res/selector_hand.png")
  def getSelector(max_choices : Int) = new Selector(image, new Rectangle(350, 300, 28, 28), max_choices)
}


class Selector (val image: Image, val rect: Rectangle, val max_choices: Int) {
  var animate = true
  var choice = 1
  var initial = 0.1
  var position = initial
  var direction = initial
  var yetToMove = 0
  var increment = 0
  var initialx = rect.getX
  
  def reset() {
    position = 1
    direction = initial
    rect.setX(initialx)
  }
  
  def move(y : Int) {
    val temp = if ((y < 0 && choice != 1) || (y > 0 && choice != max_choices)) {
      choice += (y / 30)
      reset
      y
    } else {
      0
    }
    rect.setY(rect.getY + temp)
  }
  
  def update () {
    if (animate) {
      rect.setX(rect.getX + (1 * Math.sin(1 + position)).toFloat)
    }
    
    if (position == 0.5 || position == 0) {
      direction = direction * -1
    }
    position += direction
    
  }
  
  def render() {
    image.draw(rect.getX, rect.getY)
  }
}