#!/bin/bash
# VAULT_RETRIES=5
# echo "Vault is starting..."
# until vault status > /dev/null 2>&1 || [ "$VAULT_RETRIES" -eq 0 ]; do
#         echo "Waiting for vault to start...: $((VAULT_RETRIES--))"
#         sleep 1
# done
export VAULT_ADDR=$VAULT_ADDRESS
export VAULT_TOKEN=$VAULT_ROOT_TOKEN
echo "Authenticating to vault..."
vault secrets enable -version=2 -path=ThitsaConnect kv
echo " Data Importing... "
vault kv put ThitsaConnect/redis/settings redisUrl=redis://redis:6379 
vault kv put ThitsaConnect/vnext/settings fspiopBaseUrl=http://fspiop-api-svc.dev.sanbox.wynepayhubsanbox-pre.com
vault kv put ThitsaConnect/thitsawallet/settings thitsawalletUrl=http://13.212.50.9/ 
vault kv put ThitsaConnect/interledger/settings thitsaconnectKey=ThitsaConnectKey 
vault kv put ThitsaConnect/musoni/settings apikey=yE2wPAq90Laa23jbNLo3K43Kjmu7y1VZ3Pe36sQR 
vault kv put ThitsaConnect/musoni/settings musoniUrl=https://api.demo.sing.musoniservices.com/v1/ 
vault kv put ThitsaConnect/musoni/settings password=zarchitun 
vault kv put ThitsaConnect/musoni/settings roundingvalue=100 
vault kv put ThitsaConnect/musoni/settings tenantId=thitsaworks 
vault kv put ThitsaConnect/musoni/settings username=testuser 
echo "Complete...."