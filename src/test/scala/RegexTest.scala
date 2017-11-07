object RegexTest extends App {

  val regex = """^[ \t]*:t(ransaction)?[ \t]+([0-9]+)[ \t]+((\-?[0-9]+)(:\-?[0-9]+)*)[ \t]*$""".r
  val str = ":t 245 24:101:107:-90:-41:-122:44:-79:29:-103:90:-2:32:-65:-121:97"

  str match {
    case regex(_, amt, to, _*) => println(amt, to)
  }

}
