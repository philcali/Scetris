import sbt._

class Scetris(info: ProjectInfo) extends DefaultProject(info) {

  val slickRepo = "Slick 2D Game Library" at "http://slick.cokeandcode.com/mavenrepo"
  val lwjglRepo = "LWJGL repo @ Nifty UI" at "http://nifty-gui.sourceforge.net/nifty-maven-repo"
  val slick = "slick" % "slick" % "239"
  val lwjgl = "lwjgl" % "lwjgl" % "2.0.1"

  lazy val server = project("scetris-server", "Scetris Game Server") 

  override def mainClass = Some("com.philipcali.tetris.Main")
  override def filterScalaJars = false

  override def ivyXML = 
    <publications>
      <artifact name="scetris" type="jar" ext="jar"/>
    </publications>
}
