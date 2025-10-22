#!/bin/bash
set -e
ENDPOINT=http://localhost:4566
AWS="aws --endpoint-url=$ENDPOINT"

echo "⏳ Creating DynamoDB tables..."

$AWS dynamodb create-table \
  --table-name Transactions \
  --attribute-definitions AttributeName=transactionId,AttributeType=S \
  --key-schema AttributeName=transactionId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --stream-specification StreamEnabled=true,StreamViewType=NEW_IMAGE

$AWS dynamodb create-table \
  --table-name Accounts \
  --attribute-definitions AttributeName=accountId,AttributeType=S \
  --key-schema AttributeName=accountId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

STREAM_ARN=$($AWS dynamodb describe-table --table-name account_ledger --query "Table.LatestStreamArn" --output text)
echo "✅ Stream ARN: $STREAM_ARN"

echo "📦 Packaging Java Lambda..."
cd lambda-processor
./gradlew clean build
cd ..

echo "🧠 Creating Lambda function..."
$AWS lambda create-function \
  --function-name ProcessTransactionStream \
  --runtime java17 \
  --zip-file fileb://lambda-processor/build/distributions/lambda-processor.zip \
  --handler com.example.lambda.StreamHandler::handleRequest \
  --role arn:aws:iam::000000000000:role/lambda-role

echo "🔗 Linking DynamoDB Stream to Lambda..."
$AWS lambda create-event-source-mapping \
  --function-name ProcessTransactionStream \
  --event-source-arn $STREAM_ARN \
  --starting-position LATEST

echo "✅ Setup complete!"