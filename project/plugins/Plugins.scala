import sbt._

class Plugin(info: ProjectInfo) extends PluginDefinition(info) {
  val lwjgl = "calico" % "sbt-lwjgl-plugin" % "1.0"
}

// vim: set ts=4 sw=4 et:
