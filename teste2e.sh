#!/bin/bash

# ANSI colors for better output
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}--- Starting Docker Environment for E2E Test ---${NC}"
docker-compose down
docker-compose up -d

echo -e "${YELLOW}--- Waiting for services to be ready (30 seconds)... ---${NC}"
sleep 30

# JSON payload for the order
ORDER_DATA='{
    "customerId": "USER_TEST_1",
    "fromAddress": "123 Start St",
    "toAddress": "456 End Blvd",
    "packageWeight": 2.5,
    "requestedDeliveryTime": "2026-05-20T15:00:00",
    "maxDeliveryTimeMinutes": 45
}'

echo -e "${CYAN}--- Sending new order to Customer Service ---${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d "$ORDER_DATA")

if [ -z "$RESPONSE" ]; then
    echo -e "${RED}Error: No response from Customer service. Is it running on port 8080?${NC}"
    docker-compose down
    exit 1
fi

echo -e "${GREEN}Order submitted! SAGA ID: $RESPONSE${NC}"

echo -e "${YELLOW}--- Waiting for SAGA to complete across microservices (15 seconds)... ---${NC}"
sleep 15

echo -e "${CYAN}--- Checking final state in the logs ---${NC}"
# Check if the SAGA reached COMPLETED status in the logs
if docker-compose logs customer | grep -q "COMPLETED"; then
    echo -e "${GREEN}SUCCESS: Saga completed successfully!${NC}"
else
    echo -e "${RED}FAILURE: Saga status 'COMPLETED' not found in logs.${NC}"
fi

echo -e "${YELLOW}--- Test Finished. Shutting down... ---${NC}"
docker-compose down