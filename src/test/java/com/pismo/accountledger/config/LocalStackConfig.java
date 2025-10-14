package com.pismo.accountledger.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@TestConfiguration(proxyBeanMethods = false)
@Profile("test")
public class LocalStackConfig {

    private static final DockerImageName LOCALSTACK_IMAGE_NAME = DockerImageName.parse("localstack/localstack:latest");

    @Bean(initMethod = "start", destroyMethod = "stop")
    public LocalStackContainer localStackContainer() {
        return new LocalStackContainer(LOCALSTACK_IMAGE_NAME)
                .withServices(LocalStackContainer.Service.DYNAMODB);
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(LocalStackContainer localStack) {
        AwsBasicCredentials creds = AwsBasicCredentials.create(
                localStack.getAccessKey(), localStack.getSecretKey());
        return StaticCredentialsProvider.create(creds);
    }

    @Bean
    public DynamoDbClient dynamoDbClient(LocalStackContainer localStack,
                                         AwsCredentialsProvider credsProvider) {
        return DynamoDbClient.builder()
                .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
                .credentialsProvider(credsProvider)
                .region(Region.of(localStack.getRegion()))
                .build();
    }
}
