package server

case class Config(mode: String = "", port: Int = -1, startStore: Boolean = false, kwargs: Map[String, String] = Map())

object App {
  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Config]("livehq-media") {
      head("livehq-media", "0.x")
      cmd("publisher") action { (_, c) =>
        c.copy(mode = "publisher")
      } text "publisher" children(
        opt[Int]("port") abbr "p" action { (x, c) =>
          c.copy(port = x)
        } text "sets port",
        opt[Boolean]("startStore") abbr "s" action { (x, c) =>
          c.copy(startStore = x)
        } text "starts store",
        checkConfig { c =>
          if (c.port < 0) failure("port must be greater than zero") else success
        }
        )
      cmd("publisher-monitor") action { (_, c) =>
        c.copy(mode = "publisher-monitor")
      } text "publisher-monitor" children(
        opt[Int]("port") abbr "p" action { (x, c) =>
          c.copy(port = x)
        } text "sets port",
        checkConfig { c =>
          if (c.port < 0) failure("port must be greater than zero") else success
        }
        )

      cmd("subscriber") action { (_, c) =>
        c.copy(mode = "subscriber")
      } text "subscriber" children(
        opt[Int]("port") abbr "p" action { (x, c) =>
          c.copy(port = x)
        } text "sets port",
        checkConfig { c =>
          if (c.port < 0) failure("port must be greater than zero") else success
        }
        )
      cmd("subscriber-monitor") action { (_, c) =>
        c.copy(mode = "subscriber-monitor")
      } text "subscriber-monitor" children(
        opt[Int]("port") abbr "p" action { (x, c) =>
          c.copy(port = x)
        } text "sets port",
        checkConfig { c =>
          if (c.port < 0) failure("port must be greater than zero") else success
        }
        )
    }
    parser.parse(args, Config()) match {
      case Some(config) =>
        if (config.mode == "publisher") {
          PublisherApp.run(config.port, config.startStore)
        } else if (config.mode == "publisher-monitor") {
          PublisherMonitorApp.run(config.port, config.startStore)
        } else if (config.mode == "subscriber") {
          SubscriberApp.run(config.port, config.startStore)
        } else if (config.mode == "subscriber-monitor") {
          SubscriberMonitorApp.run(config.port, config.startStore)
        }
      case None =>
      // arguments are bad, error message will have been displayed
    }
  }
}

