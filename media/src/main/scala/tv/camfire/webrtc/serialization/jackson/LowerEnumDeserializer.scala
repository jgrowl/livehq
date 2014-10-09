package tv.camfire.webrtc.serialization.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer

class LowerEnumDeserializer (clazz: Class[Enum[_]]) extends StdScalarDeserializer[Enum[_]](clazz) {
  override def deserialize(jp: JsonParser, ctxt: DeserializationContext): Enum[_] = {
    val text = jp.getText.toUpperCase
    val valueOfMethod = getValueClass.getDeclaredMethod("valueOf", classOf[String])
    valueOfMethod.invoke(null, text).asInstanceOf[Enum[_]]
  }
}

