package tv.camfire.media.config

import com.typesafe.config.ConfigFactory


/**
 * User: jonathan
 * See: http://scalaruss.wordpress.com/2013/03/08/typesafe-config/
 * Date: 7/14/13
 * Time: 4:08 PM
 */
trait Properties {
  /** ConfigFactory.load() defaults to the following in order:
    * system properties
    * application.conf
    * application.json
    * application.properties
    * reference.conf
    *
    * So a system property set in the application will override file properties
    * if it is set before ConfigFactory.load is called.
    * eg System.setProperty("environment", "production")
    */
  val envConfig = ConfigFactory.load("application")


  val environment = envConfig getString "configurableApp.environment"

  /** ConfigFactory.load(String) can load other files.
    * File extension must be conf, json, or properties.
    * The extension can be omitted in the load argument.
    */
  val config = ConfigFactory.load(environment) // eg "test" or "test.conf" etc

  def sessionKeyPrefix = config getString "app.session.redis.session_key_prefix"

  def resourcePath = config getString "app.resource_path"

  def contextPath = config getString "app.context_path"

  def sessionCookie = config getString "app.session.cookie"

  def redisHost = config getString "app.redis.host"

  def redisPort = config getInt "app.redis.port"

  def mediaServiceName = config getString "app.media_service.name"

  def signalBroadcasterName = config getString "app.broadcaster.signal.name"

  def iceProtocol = config getString "app.ice.protocol"

  def iceHost = config getString "app.ice.host"

  def icePort = config getInt "app.ice.port"

  def iceUsername = config getString "app.ice.username"

  def icePassword = config getString "app.ice.password"

  def mediaServerPort = config getInt "app.media_server.port"

  def iceUri = "%s:%s:%s".format(iceProtocol, iceHost, icePort)

  def actorSystemName = config getString "media.actor_system.name"

  def sharedJournalPort = "2551"
  def sharedJournalPath = s"akka.tcp://ClusterSystem@livehq-publisher-seed:$sharedJournalPort/user/store"
}
