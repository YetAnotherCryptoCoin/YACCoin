object RegexTest extends App {

  val regex = """:txn ([0-9]+) ((\-?[0-9]+)(:\-?[0-9]+)*)""".r
  val str = ":txn 245 24:101:107:-90:-41:-122:44:-79:29:-103:90:-2:32:-65:-121:97"

  str match {
    case regex(amt, to, _*) => println(amt, to)
  }

}
