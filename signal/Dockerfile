FROM ruby:2.2-onbuild

ADD ./scripts/setup-consul.sh /setup-consul.sh
RUN chmod a+x /setup-consul.sh

ADD ./scripts/start.sh /start.sh
RUN chmod a+x /start.sh


CMD ["./start.sh"]

#TODO Determine if order matters here!
EXPOSE 1234
ENTRYPOINT ["/start.sh"]


