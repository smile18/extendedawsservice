package org.deep.rogs.conf;


import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class BaseClientConfiguration {
    private static final Log LOG = LogFactory.getLog(BaseClientConfiguration.class);


    @Autowired
    private S3ClientConfiguration configuration;
    @Autowired
    private SQSClientConfiguration sqsConfig;

    @Bean
    public AmazonS3 s3Client() {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(configuration.getClientRegion())
                .build();
        return s3Client;
    }

    @Bean
    public AmazonSQS sqsClient() {
        try {
            AWSCredentials awsCredentials = new BasicAWSCredentials(sqsConfig.getACCESS_KEY_ID(), sqsConfig.getSECRET_ACCESS_KEY());
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setProxyPort(sqsConfig.getPort());
            clientConfiguration.setProxyHost(sqsConfig.getPROXY_HOST());
            clientConfiguration.setMaxErrorRetry(2);

            AmazonSQS sqsBaseClient = AmazonSQSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withClientConfiguration(clientConfiguration)
                    .withRegion(sqsConfig.getRegion())
                    .build();

            return sqsBaseClient;
        } catch (AmazonSQSException e ) {
            LOG.error(e.getMessage());
            LOG.error(e.getErrorCode());
            return null;
        }

    }

}
