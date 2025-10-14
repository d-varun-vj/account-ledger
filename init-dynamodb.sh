#!/bin/bash

set -e

echo "Waiting for DynamoDB Local to start..."
sleep 5

#ENDPOINT="http://localhost:8000"
#TABLE_NAME="account_ledger"

aws dynamodb create-table \
    --table-name account_ledger \
    --attribute-definitions \
        AttributeName=account_id,AttributeType=S \
        AttributeName=item_id,AttributeType=S \
    --key-schema \
        AttributeName=account_id,KeyType=HASH \
        AttributeName=item_id,KeyType=RANGE \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000 || echo "Table account_ledger already exists"

echo "Table account_ledger created or already exists!"

aws dynamodb create-table \
    --table-name account_unique_constraint \
    --attribute-definitions \
        AttributeName=constraint_type,AttributeType=S \
        AttributeName=constraint_value,AttributeType=S \
    --key-schema \
        AttributeName=constraint_type,KeyType=HASH \
        AttributeName=constraint_value,KeyType=RANGE \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000 || echo "Table account_unique_constraint already exists"

echo "Table account_unique_constraint created or already exists!"
