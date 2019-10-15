package zflow

import shapeless._

trait CmdStringify[A] {
  def stringify(a: A): String
}

object CmdStringify {

    def apply[A](implicit enc: CmdStringify[A]): CmdStringify[A] =
      enc
    
    def instance[A](func: A => String): CmdStringify[A] = new CmdStringify[A] {
      def stringify(value: A): String = func(value)
    }

    implicit val stringHelper: CmdStringify[String] = 
      instance(s => s)

    implicit val hnilEncoder: CmdStringify[HNil] =
      instance(_ => "")
    
    implicit def hlistEncoder[H, T <: HList]  (
      implicit
      hEncoder: Lazy[CmdStringify[H]],
      tEncoder: CmdStringify[T]
    ): CmdStringify[ H :: T] =
      instance {
        case h :: t =>
          val s1: String = hEncoder.value.stringify(h) 
          val s2: String = tEncoder.stringify(t)
          s1 + s2
      }
    
    implicit def genericPathHelper[A, R](
      implicit
      gen: Generic.Aux[A, R],
      enc: Lazy[CmdStringify[R]]
    ): CmdStringify[A] = 
      instance( a => enc.value.stringify(gen.to(a)))

  implicit val cnilEncoder: CmdStringify[CNil] =
    instance(_ => throw new Exception("Inconceivable"))

  implicit def coproductEncoder[H, T <: Coproduct](
    implicit
    hEncoder: Lazy[CmdStringify[H]],
    tEncoder: CmdStringify[T]
  ): CmdStringify[H :+: T] = instance {
    case Inl(h) => hEncoder.value.stringify(h)
    case Inr(t) => tEncoder.stringify(t)
   }

  implicit def listEncoder[A]
   (implicit enc: CmdStringify[A]): CmdStringify[List[A]] =
    instance(list => list.map(enc.stringify).mkString(" ") ) 
  /*
  implicit def listEncoder[A]: CmdStringify[List[A]] = instance { li =>
    li.map(_.stringify).mkString(" ")
  }*/

  implicit class StringifyAll[A](a: A) {
    def stringify(implicit enc: CmdStringify[A]): String = 
      enc.stringify(a)
  }
  
}
