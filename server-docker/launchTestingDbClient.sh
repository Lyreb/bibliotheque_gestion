#!/bin/bash
echo "Attempt to connect to MMI Library Server Mongo server..."
docker run -ti --rm --name test-mmilibsrv-mongo-cli \
    --network mmilibtestnet \
    mongo:4.2 mongo \
        --host test-mmilibsrv-mongo-src \
        -u admin -p testpwd --authenticationDatabase admin \
        mmilibsrv

echo "Bye."