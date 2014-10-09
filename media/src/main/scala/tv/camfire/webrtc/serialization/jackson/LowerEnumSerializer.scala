package tv.camfire.webrtc.serialization.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer


class LowerEnumSerializer extends StdScalarSerializer[Enum[_]](classOf[Enum[_]], false) {
  override def serialize(value: Enum[_], jgen: JsonGenerator, provider: SerializerProvider) {
    jgen.writeString(value.name().toLowerCase)
  }
}

