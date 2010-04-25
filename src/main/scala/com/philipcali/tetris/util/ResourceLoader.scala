package com.philipcali.tetris.util

import org.newdawn.slick.tiled.TiledMap

object ResourceLoader {
  val maps = Map("test.tmx" -> new TiledMap("res/test.tmx"), "test2.tmx" -> new TiledMap("res/test2.tmx"))
}
