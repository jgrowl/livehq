package server

/**
 * Created by jonathan on 12/10/14.
 */
object Utils {
  def stripNewline(string: String): String = {
    string.replaceAll("\\r\\n", "")
  }
}
