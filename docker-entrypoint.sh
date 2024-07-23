#!/bin/sh
# docker-entrypoint.sh

# run the main container command
java "-DfspiopBaseUrl=${FSP_IOP_BASE_URL}" "-DredisUrl=${REDIS_URL}" "-DisEnableJws=${ENABLE_JWS}" "-DcacheLifeTime=${CACHE_LIFE_TIME}" "-DpublicKey=${PUBLIC_KEY}" "-DlocaleLanguage=${LOCALE_LANGUAGE}" "-DisisUrl=${ISIS_URL}" "-DisisUserName=${ISIS_USERNAME}" "-DisisPassword=${ISIS_PASSWORD}" -cp fsp/fsp_connector_api.jar:fsp/lib/* com.thitsaworks.mojaloop.thitsaconnect.fsp.connector.api.FspConnectorApiApplication  &
java "-DfspiopBaseUrl=${FSP_IOP_BASE_URL}" "-DredisUrl=${REDIS_URL}" "-DisEnableJws=${ENABLE_JWS}" "-DcacheLifeTime=${CACHE_LIFE_TIME}" "-DpublicKey=${PUBLIC_KEY}" "-DlocaleLanguage=${LOCALE_LANGUAGE}" "-DisisUrl=${ISIS_URL}" "-DisisUserName=${ISIS_USERNAME}" "-DisisPassword=${ISIS_PASSWORD}" -cp hub/hub_connector_api.jar:hub/lib/* com.thitsaworks.mojaloop.thitsaconnect.hub.connector.api.HubConnectorApiApplication  &
# Wait for any process to exit
wait -n
# Exit with status of process that exited first
exit $?