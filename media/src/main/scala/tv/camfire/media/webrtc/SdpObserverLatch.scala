package tv.camfire.media.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by jonathan on 12/16/13.
 */
class SdpObserverLatch extends SdpObserver {
  var success: Boolean = false

  var sdp: SessionDescription = null

  var error: String = null

  private val latch: CountDownLatch = new CountDownLatch(1)

  def onCreateSuccess(sdp: SessionDescription) {
    this.sdp = sdp
    onSetSuccess()
  }

  def onSetSuccess() {
    success = true
    latch.countDown()
  }

  def onCreateFailure(error: String) {
    onSetFailure(error)
  }

  def onSetFailure(error: String) {
    this.error = error
    latch.countDown()
  }

  def await(): Boolean = {
    latch.await(5, TimeUnit.SECONDS)
    success
  }
}
