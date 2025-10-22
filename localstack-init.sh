#!/bin/bash
set -e
ENDPOINT=http://localhost:4566
AWS="aws --endpoint-url=$ENDPOINT --region=ap-south-1"

echo "⏳ Creating lambda function..."

$AWS lambda create-function \
  --function-name ProcessTransactionStream \
  --runtime java21 \
  --zip-file fileb://lambda-processor/build/distributions/lambda-processor.zip \
  --handler com.pismo.lambda.StreamHandler::handleRequest \
  --role arn:aws:iam::000000000000:role/lambda-role || true
STREAM_ARN=$($AWS dynamodb describe-table --table-name account_ledger --query "Table.LatestStreamArn" --output text)

echo "✅ Stream ARN: $STREAM_ARN"

echo "🔗 Linking DynamoDB Stream to Lambda..."
$AWS lambda create-event-source-mapping \
  --function-name ProcessTransactionStream \
  --event-source-arn $STREAM_ARN \
  --starting-position LATEST

echo "✅ Setup complete!"