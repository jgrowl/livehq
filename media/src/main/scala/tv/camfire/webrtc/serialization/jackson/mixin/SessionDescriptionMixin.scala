package tv.camfire.webrtc.serialization.jackson.mixin

import com.fasterxml.jackson.annotation.JsonProperty
import org.webrtc.SessionDescription

/**
 * User: jonathan
 * Date: 5/1/13
 * Time: 12:51 AM
 */
abstract class SessionDescriptionMixin(@JsonProperty("type") `type`: SessionDescription.Type,
                                       @JsonProperty("sdp") sdp: String) {
  @JsonProperty("sdp") private[serialization] var description: String = null
}
