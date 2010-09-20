import sbt._

class Scetris(info: ProjectInfo) extends LWJGLProject(info) with Slick2D {

  lazy val server = project("scetris-server", "Scetris Game Server") 
  override def mainClass = Some("com.philipcali.tetris.Main")
  override def filterScalaJars = false

  override def ivyXML = 
    <publications>
      <artifact name="scetris" type="jar" ext="jar"/>
    </publications>
}
