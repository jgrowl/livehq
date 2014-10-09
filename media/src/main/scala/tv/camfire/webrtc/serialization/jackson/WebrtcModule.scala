package tv.camfire.webrtc.serialization.jackson

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationConfig, BeanDescription, JsonDeserializer, JsonSerializer}
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.deser.Deserializers.Base
import tv.camfire.webrtc.serialization.jackson.mixin.{IceCandidateMixin, SessionDescriptionMixin}


/**
 * User: jonathan
 * Date: 5/1/13
 * Time: 12:56 AM
 */
class WebrtcModule extends SimpleModule("WebrtcModule", new Version(1, 0, 0, "", "tv.camfire", "webrtc")) {
  val serializer = new LowerEnumSerializer().asInstanceOf[JsonSerializer[java.io.Serializable]]
  addSerializer(classOf[Enum[T] forSome {type T <: Enum[T]}], serializer)

  override def setupModule(context: SetupContext) {
    super.setupModule(context)

    val lowerEnumDeserializer: Base = new Deserializers.Base() {
      override def findEnumDeserializer(`type`: Class[_],
                                        config: DeserializationConfig,
                                        beanDesc: BeanDescription): JsonDeserializer[_] = {
        new LowerEnumDeserializer(`type`.asInstanceOf[Class[Enum[_]]])
      }
    }

    context.addDeserializers(lowerEnumDeserializer)
    context.setMixInAnnotations(classOf[SessionDescription], classOf[SessionDescriptionMixin])
    context.setMixInAnnotations(classOf[IceCandidate], classOf[IceCandidateMixin])
  }
}
