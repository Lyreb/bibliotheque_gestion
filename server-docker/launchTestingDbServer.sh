#!/bin/bash
echo "Create MMI Library Server testing network..."
docker network create mmilibtestnet

echo "Create MMI Library Server Mongo server..."
docker run -ti --rm --name test-mmilibsrv-mongo-src \
    -e MONGO_INITDB_ROOT_USERNAME=admin \
    -e MONGO_INITDB_ROOT_PASSWORD=testpwd \
    -e MONGO_INITDB_DATABASE=mmilibsrv \
    -p "127.0.0.1:27017:27017" \
    --network mmilibtestnet \
    mongo:4.2

echo "MMI Library Server Mongo server stopped."

echo "Remove MMI Library Server testing network..."
docker network rm mmilibtestnet

echo "Bye."