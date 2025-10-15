# Account Ledger Application

## Overview
A Spring Boot microservice that manages customer account and transactions.

- Sequential Account IDs
- Sequential Transaction IDs

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Setup & Run](#setup--run)
- [API Documentation](#api-documentation)
- [Testing](#testing)

## Overview

The Account Ledger Service provides APIs to manage financial accounts and transactions.  
It supports:
- Creating accounts
- Recording debit/credit transactions

## Features
- RESTful APIs with JSON (snake_case)
- Swagger/OpenAPI documentation
- DynamoDB integration
- UTC-based timestamp handling
- Tests with JUnit 5

## Tech Stack
- Java 25
- Spring Boot 3.5.6
- DynamoDB (AWS SDK v2)
- JUnit 5, Mockito
- Springdoc OpenAPI
- Gradle

## Setup & Run

### Prerequisites
- Java 25
- Docker (for local DynamoDB)
- Docker-compose

### Running locally
Clone : https://github.com/d-varun-vj/account-ledger
```bash
# Clone the repository
git clone git@github.com:d-varun-vj/account-ledger.git
cd account-ledger-service

# Build
./gradlew clean build

# Run
docker-compose up --build  
```

## API Documentation

Once the app is running, access Swagger UI:
http://localhost:9040/swagger-ui/index.html

## Testing

The project uses JUnit 5 parameterized tests and Spring Boot test containers.

Run all tests:
```bash
./gradlew test