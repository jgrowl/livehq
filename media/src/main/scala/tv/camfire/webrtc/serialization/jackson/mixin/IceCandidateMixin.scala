package tv.camfire.webrtc.serialization.jackson.mixin

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * User: jonathan
 * Date: 5/1/13
 * Time: 12:51 AM
 */
abstract class IceCandidateMixin(@JsonProperty("sdpMid") sdpMid: String,
                                 @JsonProperty("sdpMLineIndex") sdpMLineIndex: Int,
                                 @JsonProperty("candidate") candidate: String) {
  @JsonProperty("candidate") private[serialization] var sdp: String = null
}
