package com.consol.citrus.simulator.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SSLRestTemplateConfiguration {

    @Bean
    public RestTemplate sslRestTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContextBuilder sslcontext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy());

        HttpClient client = HttpClients.custom().setSSLContext(sslcontext.build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();

        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
    }
}
