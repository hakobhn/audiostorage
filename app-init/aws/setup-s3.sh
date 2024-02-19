#!/usr/bin/env bash

echo "S3 Configuration started"
echo "Creating default bucket"

aws --endpoint-url=http://localhost:4566 s3 mb s3://audiofiles

echo "List s3 buckets"

aws --endpoint-url="http://localhost:4566" s3 ls

echo "S3 Configured"