package com.spring.ai.rag.configs;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

@Configuration
public class CommonConfig {

    @Value("${rag.elasticsearch.host}")
    private String host;

    @Value("${rag.elasticsearch.port}")
    private int port;

    @Value("${rag.elasticsearch.username}")
    private String userName;

    @Value("${rag.elasticsearch.password}")
    private String password;

    @Value("${rag.ai.vectorstore.elasticsearch.initialize-schema}")
    private boolean schema;

    @Value("${rag.ai.vectorstore.elasticsearch.index-name}")
    private String indexName;


    @Bean
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(host, port, "https"))
                .setHttpClientConfigCallback(createHttpClientConfigCallBack())
                .build();
    }

    @Bean
    public VectorStore vectorStore(RestClient restClient, EmbeddingModel embeddingModel) {
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName(indexName);    // Optional: defaults to "spring-ai-document-index"

        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)                     // Optional: use custom options
                .initializeSchema(schema)               // Optional: defaults to false
                .build();
    }

    private RestClientBuilder.HttpClientConfigCallback createHttpClientConfigCallBack(){
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName,password);
        credentialsProvider.setCredentials(AuthScope.ANY,credentials);
        return httpClientBuilder-> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                .setSSLContext(getSslContext())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setMaxConnTotal(10)
                .setMaxConnPerRoute(10)
                .addInterceptorLast((HttpResponseInterceptor) (response,context)-> response.addHeader("X-Elastic-Product", "Elasticsearch"));
    }



    private SSLContext getSslContext(){
        try{
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            Certificate ca;
            try(InputStream certificateInputStream = new FileInputStream("src/main/resources/certs/http_ca.crt")){
                ca = cf.generateCertificate(certificateInputStream);
            }

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore =KeyStore.getInstance(keyStoreType);
            keyStore.load(null);
            keyStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null,tmf.getTrustManagers(),null);
            return context;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
