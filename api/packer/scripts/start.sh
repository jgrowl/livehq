#!/usr/bin/env bash
./bin/bundle
./bin/rake db:migrate
./bin/rails s -b 0.0.0.0
