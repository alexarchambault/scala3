package scala3build

enum Kind:
  case Hybrid
  case Pure

  def asString: String = this match {
    case Hybrid => "hybrid"
    case Pure => "pure"
  }
