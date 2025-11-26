#!/usr/bin/env bash

# REV="1.8.9"
# REV+="1.9.2,1.9.4"
# REV+=",1.10.2"
# REV+=",1.11.2"
# REV+=",1.12.2"
# REV+=",1.13,1.13.2"
# REV+=",1.14.4"
# REV+=",1.15.2"
# REV+=",1.16.1,1.16.2,1.16.4"
# No 1.17 due to java 16 vulnerabilities :(
# REV+=",1.18.1,1.18.2"
# REV+=",1.19.2,1.19.3,1.19.4"
# REV+=",1.20.1,1.20.2,1.20.4,1.20.6"
# REV+=",1.21.1,1.21.3,1.21.4,1.21.5,1.21.8,1.21.10"

export REV=1.9.2
export M2_DIRECTORY="$M2_REPOSITORY"
export BUILD_DIRECTORY="$ARTIFACT_DIRECTORY"
export JAVA_VERSION="21"

REPOSITORY_DIR=$(dirname "$0")

cd "$REPOSITORY_DIR"

git submodule update --init --recursive --remote

if [ ! -d WorldBorder ]; then
    git clone https://github.com/PryPurity/WorldBorder.git

    sed -i 's/https:\/\/papermc.io\/repo/https:\/\/repo.papermc.io/' WorldBorder/pom.xml

    ./.git-submodules/spigot-docker-build-tools/run.sh mvn install -f WorldBorder -U
fi

./.git-submodules/spigot-docker-build-tools/run.sh mvn package
