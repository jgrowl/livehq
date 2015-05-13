#!/bin/sh

# Make sure we are in the script directory
# http://stackoverflow.com/questions/3349105/how-to-set-current-working-directory-to-the-directory-of-the-script
#cd ${0%/*}
#./setup-consul.sh

./packer/scripts/setup-consul.sh

usermod -u $APP_UID app
groupmod -g $APP_GUID app

sudo -u app ./packer/scripts/pub.sh
#sudo -u app ./pub.sh
