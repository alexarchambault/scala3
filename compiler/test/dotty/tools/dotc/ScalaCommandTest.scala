package dotty.tools.dotc

import scala.language.unsafeNulls

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.Assertions._
import dotty.tools.dotc.config.Settings._
import core.Contexts.{Context, ContextBase}

class ScalaCommandTest:

  @Test def `Simple one parameter`: Unit = inContext {
    val settings = config.ScalaSettings
    val args = "-cp path/to/classes1:other/path/to/classes2 files".split(" ")
    val summary = ScalacCommand.distill(args, settings)()
    given SettingsState = summary.sstate
    assertEquals("path/to/classes1:other/path/to/classes2", settings.classpath.value)
    assertEquals("files" :: Nil, summary.arguments)
  }

  @Test def `Unfold @file`(@TempDir temporaryFolder: java.nio.file.Path): Unit = inContext {
    val settings = config.ScalaSettings
    val file = temporaryFolder.resolve("config").toFile
    val writer = java.io.FileWriter(file);
    writer.write("-sourceroot myNewRoot someMoreFiles");
    writer.close();
    val args = s"-cp path/to/classes1:other/path/to/classes2 @${file} someFiles".split(" ")
    val summary = ScalacCommand.distill(args, settings)()

    given SettingsState = summary.sstate
    assertEquals("path/to/classes1:other/path/to/classes2", settings.classpath.value)
    assertEquals("myNewRoot", settings.sourceroot.value)
    assertEquals("someMoreFiles" :: "someFiles" :: Nil, summary.arguments)
  }

  private def inContext(f: Context ?=> Unit) = f(using (new ContextBase).initialCtx.fresh)

  extension [T](setting: Setting[T])
    private def value(using ss: SettingsState): T = setting.valueIn(ss)
