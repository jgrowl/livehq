#!/usr/bin/env bash

for filename in **/**.template; do
    dir_path=`dirname $filename`
    template_base=`basename $filename`
    base=${template_base%.template}
    echo "sed -e \"s/\\\${GID}/$GROUPS/\" -e \"s/\\\${UID}/$UID/\" $filename > `dirname $filename`/$base";
done | /bin/bash


#sed -e "s/\\\${GID}/$GROUPS/" -e "s/\\\${UID}/$UID/" $filename > `dirname $filename`/$base
#sed -e "s/\\\${GID}/$GROUPS/" -e "s/\\\${UID}/$UID/" signal/client/example/DevDockerfile.template > signal/client/example/DevDockerfile
