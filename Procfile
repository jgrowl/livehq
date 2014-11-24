redis: redis-server
signal: sleep 1; sh -c 'cd ./signal/ && ruby server.rb'
api: sh -c 'cd ./api/ && rails s'
web: sh -c 'cd ./web/ && pub serve'

#media: sleep 1; sh -c 'cd ./media/ && sbt run server.WebRtcApp'
