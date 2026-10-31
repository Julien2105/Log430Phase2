#!/usr/bin/env bash
# Démonstration bout en bout (UC-02, UC-04, UC-06) via l'API Gateway. Prérequis : docker compose up --build, curl, jq.
set -euo pipefail

GATEWAY="${GATEWAY:-http://localhost:8000}"
KEYCLOAK="${KEYCLOAK:-http://localhost:8080}"
CLIENT_A_SECRET="${CLIENT_A_SECRET:-client-a-secret}"
CLIENT_B_SECRET="${CLIENT_B_SECRET:-client-b-secret}"
DRONE="DRN-0231-$(date +%s)"          # unique à chaque exécution
IMSI="302720$(printf '%09d' $((RANDOM * RANDOM % 1000000000)))"
KEY="demo-$(date +%s)-$RANDOM"

step() { printf '\n\033[1;34m== %s\033[0m\n' "$*"; }
token() {  # OAuth2 Client Credentials auprès de Keycloak
  curl -sf -X POST "$KEYCLOAK/realms/candronex/protocol/openid-connect/token" \
    -d grant_type=client_credentials -d client_id="$1" -d client_secret="$2" | jq -r .access_token
}
status() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

step "1. Jetons Keycloak (client-a, client-b)"
TOKEN_A=$(token client-a "$CLIENT_A_SECRET"); TOKEN_B=$(token client-b "$CLIENT_B_SECRET")
echo "jetons obtenus (${#TOKEN_A} et ${#TOKEN_B} caractères)"

step "2. Sans jeton : 401 attendu"
echo "HTTP $(status "$GATEWAY/api/v1/drones")"

step "3. UC-02 : enregistrement du drone $DRONE (201, sans IMSI dans la réponse)"
curl -s -X POST "$GATEWAY/api/v1/drones" -H "Authorization: Bearer $TOKEN_A" -H 'Content-Type: application/json' \
  -d "{\"droneId\":\"$DRONE\",\"imsi\":\"$IMSI\",\"label\":\"Inspection Nord\"}" | jq .

step "4. Isolation : client-b consulte le drone de client-a (404 attendu)"
curl -s "$GATEWAY/api/v1/drones/$DRONE" -H "Authorization: Bearer $TOKEN_B" | jq .

step "5. Catalogue des offres (ServiceOffering + caractéristiques réseau)"
curl -s "$GATEWAY/api/v1/service-offerings" -H "Authorization: Bearer $TOKEN_A" | jq -c '.[] | {serviceOfferingId, serviceCharacteristics}'

BODY="{\"items\":[{\"action\":\"ADD\",\"droneId\":\"$DRONE\",\"serviceOfferingId\":\"C2_URLLC\"},{\"action\":\"ADD\",\"droneId\":\"$DRONE\",\"serviceOfferingId\":\"IMAGERY_EMBB\"}]}"
step "6. UC-04 : création de la commande (201, état ACCEPTED, caractéristiques copiées dans les lignes)"
RESP=$(curl -s -X POST "$GATEWAY/api/v1/orders" -H "Authorization: Bearer $TOKEN_A" -H "Idempotency-Key: $KEY" \
  -H 'Content-Type: application/json' -d "$BODY")
echo "$RESP" | jq .
ORDER_ID=$(echo "$RESP" | jq -r .orderId)

step "7. Rejeu avec la même clé et le même contenu : 200 et même orderId"
curl -s -X POST "$GATEWAY/api/v1/orders" -H "Authorization: Bearer $TOKEN_A" -H "Idempotency-Key: $KEY" \
  -H 'Content-Type: application/json' -d "$BODY" | jq -r .orderId

step "8. Même clé, contenu différent : 409 IDEMPOTENCY_KEY_CONFLICT"
curl -s -X POST "$GATEWAY/api/v1/orders" -H "Authorization: Bearer $TOKEN_A" -H "Idempotency-Key: $KEY" \
  -H 'Content-Type: application/json' \
  -d "{\"items\":[{\"action\":\"ADD\",\"droneId\":\"$DRONE\",\"serviceOfferingId\":\"C2_URLLC\"}]}" | jq .

step "9. UC-06 : attente de l'activation (ACCEPTED/IN_PROGRESS -> ACTIVE)"
for i in $(seq 1 30); do
  STATE=$(curl -s "$GATEWAY/api/v1/orders/$ORDER_ID" -H "Authorization: Bearer $TOKEN_A" | jq -r .state)
  echo "état de la commande : $STATE"
  case "$STATE" in ACTIVE|FAILED|PARTIALLY_ACTIVE) break;; esac
  sleep 1
done
curl -s "$GATEWAY/api/v1/orders/$ORDER_ID" -H "Authorization: Bearer $TOKEN_A" | jq '{orderId, state, items: [.items[] | {lineId, state}]}'

step "10. Inventaire des services activés pour le drone"
curl -s "$GATEWAY/api/v1/services?droneId=$DRONE" -H "Authorization: Bearer $TOKEN_A" | jq .

step "Terminé. Pour un échec réseau : MOCK_FAILURE_RATE=100 docker compose up -d slice-service, puis relancer ce script (état FAILED)."
