import sbt._

class Plugin(info: ProjectInfo) extends PluginDefinition(info) {
  val lwjglPlugin = "com.github.philcali" % "sbt-lwjgl-plugin" % "2.0.2"
}

// vim: set ts=4 sw=4 et:
