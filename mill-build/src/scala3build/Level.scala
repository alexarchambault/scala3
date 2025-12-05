package scala3build

enum Level:
  case Bootstrapping
  case Final

  def asString: String = this match {
    case Bootstrapping => "bootstrapping"
    case Final => "final"
  }
