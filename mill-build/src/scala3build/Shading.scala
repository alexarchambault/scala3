package scala3build

object Shading {

  /** Replace package names in package definitions, for shading.
   * It assumes the full package def is written on a single line.
   * It does not adapt the imports accordingly.
   */
  def replacePackage(lines: List[String])(replace: PartialFunction[String, String]): (Boolean, List[String]) = {
    def recur(lines: List[String]): (Boolean, List[String]) =
      lines match {
        case head :: tail =>
          if (head.startsWith("package ")) {
            val packageName = head.stripPrefix("package ").trim
            val newPackageNameOpt = replace.lift(packageName)
            newPackageNameOpt match {
              case Some(newPackageName) =>
                (true, s"package $newPackageName" :: tail)
              case None =>
                (false, lines)
            }
          }
          else {
            val (hasChanges, tail0) = recur(tail)
            (hasChanges, head :: tail0)
          }
        case _ =>
          (false, lines)
      }
    recur(lines)
  }

  /** Insert UnsafeNulls Import after package */
  def insertUnsafeNullsImport(lines: List[String]): List[String] = {
    def recur(ls: List[String], foundPackage: Boolean): List[String] = ls match {
      case l :: rest =>
        val lt = l.trim()
        if (foundPackage) {
          if (!(lt.isEmpty || lt.startsWith("package ")))
            "import scala.language.unsafeNulls" :: ls
          else l :: recur(rest, foundPackage)
        } else {
          if (lt.startsWith("package ")) l +: recur(rest, true)
          else l :: recur(rest, foundPackage)
        }
      case _ => ls
    }
    recur(lines, false)
  }

  def csDep(org: String, name: String, version: String): coursier.Dependency =
    coursier.Dependency(
      coursier.Module(coursier.Organization(org), coursier.ModuleName(name), Map.empty),
      coursier.version.VersionConstraint(version)
    )

}
