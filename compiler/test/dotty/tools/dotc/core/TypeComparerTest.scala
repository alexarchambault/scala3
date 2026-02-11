package dotty.tools
package dotc
package core

import Contexts.*, Decorators.*, Denotations.*, SymDenotations.*, Symbols.*, Types.*
import printing.Formatting.Show

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TypeComparerTest extends DottyTest:
  val LongType = defn.LongType

  // Ensure glb and lub give lower and upper bounds when one of the inputs is WildcardType
  // and that glb and lub honours left identity and right identity, and thus is commutative with WildcardType
  @Test def glbWildcardL = identityL("glb", glb)(LongType, id = WildcardType)
  @Test def glbWildcardR = identityR("glb", glb)(LongType, id = WildcardType)
  @Test def lubWildcardL = identityL("lub", lub)(LongType, id = WildcardType)
  @Test def lubWildcardR = identityR("lub", lub)(LongType, id = WildcardType)

  def identityL[A: Show](op: String, fn: (A, A) => A)(a: A, id: A) =
    val x = fn(id, a)
    assertEquals(a, x, i"$op(id=$id, $a) = $x, expected $a (left identity)")

  def identityR[A: Show](op: String, fn: (A, A) => A)(a: A, id: A) =
    val x = fn(a, id)
    assertEquals(a, x, i"$op($a, id=$id) = $x, expected $a (right identity)")

  // glb(a, b) = x such that x <: a, x <: b, & forAll y, y <: a, y <: b ==> y <: x
  def glb(a: Type, b: Type) =
    val x = TypeComparer.glb(a, b)
    assertTrue(x <:< a, i"glb($a, $b) = $x, but $x !<: $a")
    assertTrue(x <:< b, i"glb($a, $b) = $x, but $x !<: $b")
    x

  // lub(a, b) = x such that a <: x, b <: x, & forAll y, a <: y, b <: y ==> x <: y
  def lub(a: Type, b: Type) =
    val x = TypeComparer.lub(a, b)
    assertTrue(a <:< x, i"lub($a, $b) = $x, but $a !<: $x")
    assertTrue(b <:< x, i"lub($a, $b) = $x, but $b !<: $x")
    x
end TypeComparerTest
