package tv.camfire.media.factory

import org.webrtc.{MediaStream, PeerConnectionFactory}

/**
 * User: jonathan
 * Date: 7/18/13
 * Time: 1:32 PM
 */
class CamfirePeerConnectionFactory extends PeerConnectionFactory {

  def createDuplicatedMediaStream(mediaStream: MediaStream, identifier: String) : MediaStream = {
//    val newMediaStream = createLocalMediaStream(identifier)
//    // TODO: Get all tracks instead of just the first one!
//    val sourceVideoTrack = mediaStream.videoTracks.get(0)
//    val newSource = createVideoSourceFromVideoTrack(sourceVideoTrack)
//    val videoTrack = createVideoTrack(sourceVideoTrack.id(), newSource)
//    newMediaStream.addTrack(videoTrack)
//    newMediaStream
    mediaStream
  }

}
