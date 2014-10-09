package tv.camfire.webrtc.serialization.jackson

import com.fasterxml.jackson.databind.ObjectMapper


/**
 * User: jonathan
 * Date: 7/27/13
 * Time: 3:48 PM
 */
trait WebrtcSerializationSupport {
  def mapper: ObjectMapper

  mapper.registerModule(new WebrtcModule())
}
