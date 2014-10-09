package tv.camfire.common.serialization

//import org.json4s.jackson.JsonMethods
import org.webrtc.{IceCandidate, SessionDescription}
import tv.camfire.common.Signal
import org.slf4j.LoggerFactory

/**
 * User: jonathan
 * Date: 5/16/13
 * Time: 10:12 PM
 */
class SerializationHelper {
//  class SerializationHelper extends JsonMethods with CamfireSerializationSupport {
  protected val _logger = LoggerFactory.getLogger(getClass)

  def createOffer(offer: SessionDescription): String = {
    createSignalString("offer", writeValueAsString(offer))
  }

  def createIceCandidate(iceCandidate: IceCandidate): String = {
    createSignalString("candidate", iceCandidate)
  }

  def createAnswer(description: SessionDescription): String = {
    createSignalString("answer", description)
  }

  private def writeValueAsString(value: Any): String = {
//    mapper.writeValueAsString(value)
    ""
  }

  private def createSignal(signalName: String, value: Any): Signal = {
    val data = writeValueAsString(value)
    new Signal(signalName, data)
  }

  private def createSignalString(signalName: String, value: Any): String = {
    writeValueAsString(createSignal(signalName, value))
  }
}
