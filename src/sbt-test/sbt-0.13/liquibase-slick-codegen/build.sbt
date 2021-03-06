import ch.qos.logback.classic.{Level, Logger}
import com.github.daniel.shuy.sbt.scripted.scalatest.SbtScriptedScalaTest.FullStacks
import com.github.daniel.shuy.sbt.scripted.scalatest.ScriptedScalaTestSuiteMixin
import org.scalatest.Assertions._
import org.scalatest.WordSpec
import org.slf4j.LoggerFactory

import scala.util.Random

// scala identifiers must begin with an alphabet
def randomIdentifierName = Random.alphanumeric.dropWhile(_.isDigit).take(Random.nextInt(10) + 1).mkString

lazy val testLiquibaseSlickCodegen = project
  .in(file("."))
  .settings(
    name := "test/sbt-0.13/liquibase-slick-codegen",

    liquibaseSlickCodegenOutputPackage := randomIdentifierName,
    liquibaseSlickCodegenOutputClass := randomIdentifierName,

    liquibaseChangelog := file("changelog.xml"),

    scriptedScalaTestStacks := FullStacks,
    scriptedScalaTestSpec := Some(new WordSpec with ScriptedScalaTestSuiteMixin {
      override val sbtState: State = state.value

      // suppress non-error logging if logger core is logback
      // (SBT 0.x provides logback as a transitive dependency)
      LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) match {
        case logbackLogger: Logger => logbackLogger.setLevel(Level.ERROR)
        case _ =>
      }

      "liquibase-slick-codegen" should {
        "generate Slick database schema code" in {
          val slickCodegenFile = (scalaSource in Compile).value / liquibaseSlickCodegenOutputPackage.value / s"${liquibaseSlickCodegenOutputClass.value}.scala"

          Project.runTask(compile, state.value)

          assert(slickCodegenFile.exists(), s"$slickCodegenFile not found")
        }
      }
    })
  )
  .enablePlugins(SbtLiquibaseSlickCodegen)
