package com.travelmate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 설정
 *
 * 필수 의존성 (build.gradle):
 * implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
 *
 * application.yml 설정:
 * spring:
 *   elasticsearch:
 *     uris: http://localhost:9200
 *     username: elastic
 *     password: password
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.travelmate.repository.search")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder =
            ClientConfiguration.builder()
                .connectedTo(extractHost(elasticsearchUrl));

        // 인증 정보가 있으면 추가
        if (username != null && !username.isEmpty() &&
            password != null && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }

        return builder
            .withConnectTimeout(5000)
            .withSocketTimeout(30000)
            .build();
    }

    private String extractHost(String url) {
        // http://localhost:9200 -> localhost:9200
        return url.replace("http://", "").replace("https://", "");
    }
}
