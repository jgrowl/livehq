FROM redis

COPY redis.conf /usr/local/etc/redis/redis.conf

ADD start.sh /start.sh
RUN chmod a+x /start.sh

ENTRYPOINT ["/start.sh"]