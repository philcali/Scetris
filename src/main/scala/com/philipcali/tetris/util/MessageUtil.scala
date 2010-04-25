package com.philipcali.tetris.util

import scala.io.Source.{fromFile => open}
import org.newdawn.slick.Graphics
import org.newdawn.slick.Color
import org.newdawn.slick.Input
import org.newdawn.slick.util.ResourceLoader.{getResource => load}

object MessageUtil {
  val props = {
    Map() ++ open("message.properties").getLines.map(x => {
      val y = x.split("=")
      (y(0) -> y(1))
    }).toList
  }
  
  def centerText(g: Graphics, s: String, y: Float){
    val x = (800 - g.getFont().getWidth(s)) / 2
    drawString(g, s, x, y)
  }
  
  def drawString(g: Graphics, s:String, x: Float, y:Float) {
    g.setColor(Color.black)
    g.drawString(s, x + 1, y+1)
    g.setColor(Color.white)
    g.drawString(s, x, y)
  }
  
  def printControls(g:Graphics, con: Map[String, Int]) {
    drawString(g, props("help.shift") + props("help.right") + "'"+ Input.getKeyName(con.get("right").getOrElse(Input.KEY_D)) +"'", 200, 100)
    drawString(g, props("help.shift") + props("help.left")+"'"+ Input.getKeyName(con.get("left").getOrElse(Input.KEY_A)) +"'", 200, 130)
    drawString(g, props("help.drop") + "'"+ Input.getKeyName(con.get("drop").getOrElse(Input.KEY_S))+"'", 200, 160)
    drawString(g, props("help.fall")+"'"+Input.getKeyName(con.get("fall").getOrElse(Input.KEY_W))+"'", 200, 190)
    drawString(g, props("help.rotate") + "'"+Input.getKeyName(con.get("rotate").getOrElse(Input.KEY_J))+"'", 200, 220)
    drawString(g, props("help.storage")+ "'"+Input.getKeyName(con.get("storage").getOrElse(Input.KEY_L))+"'", 200, 250)
    drawString(g, props("help.shadow")+ "'"+Input.getKeyName(con.get("shadow").getOrElse(Input.KEY_B))+"'", 200, 280)
  }
}
