#!/bin/bash

RED='\033[0;31m'
BLUE='\033[0;34m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color


printf "${BLUE}\nIs your user able to run docker? (sudo usermod -aG docker $USER)${NC}\n\n"

echo "stop doobie_recipies docker image..."
docker rm -f doobie_recipies
echo "start doobie_recipies docker image..."
docker run -d --name doobie_recipies -p5435:5432 -e POSTGRES_USER=postgres -e POSTGRES_DB=world -e POSTGRES_PASSWORD=postgres tpolecat/skunk-world
sleep 5
echo "test..."
sbt test
echo "stop doobie_recipies docker image..."
docker rm -f doobie_recipies
