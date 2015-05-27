#!/bin/sh
#
# env-setup-heroku.sh
#
# command: `./env-setup-heroku.sh`

heroku config:set CACHE_HOST=
heroku config:set CACHE_PORT=
heroku config:set CACHE_PASSWORD=
heroku config:set TWITTER_APP_CONSUMER_KEY=
heroku config:set TWITTER_APP_CONSUMER_SECRET=
heroku config:set TWITTER_USER_ACCESS_TOKEN=
heroku config:set TWITTER_USER_ACCESS_SECRET=

echo "heroku env set up successfully"
