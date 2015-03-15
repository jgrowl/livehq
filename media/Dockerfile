FROM livehq/media:0.1

ADD start.sh /start.sh
RUN chmod a+x /start.sh

# forward request and error logs to docker log collector
#RUN ln -sf /dev/stdout /var/log/nginx/identity_access.log
#RUN ln -sf /dev/stderr /var/log/nginx/identity_error.log

#EXPOSE 80
ENTRYPOINT ["/start.sh"]