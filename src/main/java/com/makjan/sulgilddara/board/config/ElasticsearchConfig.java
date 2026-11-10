package com.makjan.sulgilddara.board.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
public class ElasticsearchConfig {
	@Bean
    public RestClient restClient(
            @Value("${elasticsearch.url:http://localhost:9200}")
            String url) {
        return RestClient.builder(HttpHost.create(url)).build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(
            RestClient restClient) {
        return new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(
            ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
