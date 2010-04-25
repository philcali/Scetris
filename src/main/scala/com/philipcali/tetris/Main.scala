package com.philipcali.tetris;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

object Main {
	def main(args : Array[String]) {
		var fullscreen = false;
		if (args.length == 1) {
			fullscreen = true;
		}
		
		val container = new AppGameContainer(new Scetris(), 800, 600, fullscreen);
		container.setShowFPS(false);
		container.start();
	}
}
