package dotty.tools.dotc.core

import dotty.tools.DottyTest
import dotty.tools.dotc.core.Symbols.defn
import dotty.tools.dotc.core.TypeOps

import org.junit.jupiter.api.*
import dotty.Assertions._

class TypesTest extends DottyTest:

  @Test def tuple3TypeSize =
    val tpe = defn.TupleType(3).nn.appliedTo(List(defn.IntType, defn.BooleanType, defn.DoubleType))
    assertEquals(3, tpe.typeSize)

  @Test def tuple3ConsTypeSize =
    val tpe = TypeOps.nestedPairs(List(defn.IntType, defn.BooleanType, defn.DoubleType))
    assertEquals(3, tpe.typeSize)