import sbt._

class GameServer(info: ProjectInfo) extends DefaultProject(info) {
  override def mainClass = Some("com.philipcali.server.Main")

  override def ivyXML = 
    <publications>
      <artifact name="scetris-server" type="jar" ext="jar"/>
    </publications>
}
