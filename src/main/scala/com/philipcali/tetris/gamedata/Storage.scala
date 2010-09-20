package com.philipcali.tetris.gamedata

import org.newdawn.slick.Input
import java.io.File
import java.io.FileWriter
import scala.io.Source.{fromFile => open}

object Storage {
  val path = {
    // Winblows!
    val subpath = if (System.getenv("OS") == "Windows_NT") {
      System.getenv("USERPROFILE")
    } else {
      System.getenv("HOME")
    }
    subpath + "/.marcus_reemer"
  }
  
  def initialize() {
    val file = new File(path)
    ifFileNotExistsDo(file)(_.mkdir)
    
    val gameData = path + "/game_data"
    ifFileNotExistsDo(new File(gameData))(f => {
      f.createNewFile
      saveMap("/game_data", dataDefault)
    })
    
    val controls = path + "/controls"
    ifFileNotExistsDo(new File(controls))(f => {
      f.createNewFile
      saveMap("/controls", controlsDefault)
    })
    
    val network = path + "/network"
    ifFileNotExistsDo(new File(network)) (f=>{
      f.createNewFile
      withFileWriter(network) (_.write("127.0.0.1"))
    })
  }
  
  def ifFileNotExistsDo(file: File)(fun: File => Unit) {
    if(!file.exists) {
      fun(file)
    }
  }
  
  def dataDefault = Map("last.run.score" -> 0, "last.run.lines" -> 0, "last.run.stage"-> 0, "last.run.tetris"-> 0, "total.score" -> 0,
                        "total.lines" -> 0, "total.tetris" -> 0, "highest.lines" -> 0, "highest.tetris" -> 0, "highest.score" -> 0)
  
  def controlsDefault = Map("left" -> Input.KEY_A, "right" -> Input.KEY_D, "drop"-> Input.KEY_S, "fall"-> Input.KEY_W, 
                            "rotate" -> Input.KEY_J, "shadow" -> Input.KEY_B, "storage" -> Input.KEY_L)
  
  def loadData() = load(path + "/game_data")
  
  def loadControls() = load(path + "/controls")
  
  def defaultnetwork() = open(path + "/network").getLines.next
  
  private def load(s : String) = {
    Map() ++ open(s).getLines.map(l => {
      val values = l.split("=")
      (values(0) -> Integer.parseInt(values(1)))
    })
  }
  
  def saveData(score: Int, lines: Int, stage: Int,  tetris:Int, oldData: Map[String, Int]) {
    val highestscore = oldData.get("highest.score").getOrElse(0)
    val highestlines = oldData.get("highest.lines").getOrElse(0)
    val highesttetris = oldData.get("highest.tetris").getOrElse(0)
    
    val newData = Map("last.run.score" -> score,
                      "last.run.lines" -> lines,
                      "last.run.stage"-> stage,
                      "last.run.tetris"-> tetris,
                      "total.score" -> (score + oldData.get("total.score").getOrElse(0)),
                      "total.lines" -> (lines + oldData.get("total.lines").getOrElse(0)),
                      "total.tetris" -> (tetris + oldData.get("total.tetris").getOrElse(0)),
                      "highest.lines" -> (if (lines > highestlines) lines else highestlines),
                      "highest.tetris" -> (if (tetris > highesttetris) tetris else highesttetris),
                      "highest.score" -> (if (score > highestscore) score else highestscore))
    
    saveMap("/game_data", newData)
  }
  
  def saveMap(s: String, values: Map[String, Int]) {
    withFileWriter(path + s)(w => {
      for ((k, v) <- values) {
        w.write(k + "="+ v + "\n" )
      }
    })
  }
  
  def saveControls(values: Map[String, Int]) {
    saveMap("/controls", values)
  }
  
  def saveNetwork(ip:String) {
    withFileWriter(path + "/network") (_.write(ip))
  }
  
  def withFileWriter(file: String)( fun: FileWriter => Unit) {val writer = new FileWriter(file); fun(writer); writer.close()}
}
