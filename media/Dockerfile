FROM java:7
COPY lib/native/libjingle_peerconnection_so.so /usr/local/lib/libjingle_peerconnection_so.so

#FROM jgrowl/livehq-media:0.3

#RUN mkdir /home/app
#RUN groupadd -r app && useradd -d /home/app -r -g app app
#RUN groupadd -g 1000 -r app && useradd -d /home/app -u 1000 -r -g app app
#RUN chown app:app /home/app
#USER app
#ADD . /home/app
ADD target/scala-2.11/media-assembly-1.0.jar /app/media-assembly-1.0.jar
#rsync -ru ./media/target/scala-2.11/media-assembly-1.0.jar $PUBLISHER_USER@$PUBLISHER_HOST:~
