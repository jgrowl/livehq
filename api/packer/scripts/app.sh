#!/usr/bin/env bash
export PATH=~app/.rbenv/bin:$PATH;
export RBENV_ROOT=~app/.rbenv;
eval "$(rbenv init -)";
gem install bundler
rbenv rehash
bundle
rbenv rehash
bundle exec rake db:migrate
rails s -b 0.0.0.0
