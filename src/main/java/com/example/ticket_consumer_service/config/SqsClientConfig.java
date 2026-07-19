package com.example.ticket_consumer_service.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsClientConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public SqsClient sqsClient(
			@Value("${app.aws.region}") String region,
			@Value("${app.aws.access-key-id:}") String accessKeyId,
			@Value("${app.aws.secret-access-key:}") String secretAccessKey,
			@Value("${app.aws.session-token:}") String sessionToken,
			@Value("${app.aws.endpoint:}") String endpoint) {
		var builder = SqsClient.builder().region(Region.of(region));

		if (StringUtils.hasText(endpoint)) {
			builder.endpointOverride(URI.create(endpoint));
		}

		if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)) {
			if (StringUtils.hasText(sessionToken)) {
				builder.credentialsProvider(StaticCredentialsProvider.create(
						AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)));
			} else {
				builder.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
			}
		} else {
			builder.credentialsProvider(DefaultCredentialsProvider.create());
		}

		return builder.build();
	}
}