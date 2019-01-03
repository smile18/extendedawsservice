package org.deep.rogs.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.util.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deep.rogs.conf.BaseClientConfiguration;
import org.deep.rogs.conf.ClientConstants;
import org.deep.rogs.conf.S3ClientConfiguration;
import org.deep.rogs.model.JsonDataConverter;
import org.deep.rogs.model.S3PointerMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class S3BaseClient {

    private static final Log LOG = LogFactory.getLog(S3BaseClient.class);


    private AmazonS3 s3Client;
    @Autowired
    private S3ClientConfiguration configuration;

    @Autowired
    private CommonService commonService;

    @Autowired
    private BaseClientConfiguration config;

    @PostConstruct
    public void init() {
        s3Client = config.s3Client();
        createBucket();
    }

    private void createBucket() {
        if (!s3Client.doesBucketExist(configuration.getS3BucketName())) {
            s3Client.createBucket(new CreateBucketRequest(configuration.getS3BucketName()));
            String bucketLocation = s3Client.getBucketLocation(new GetBucketLocationRequest(configuration.getS3BucketName()));

            LOG.info("Bucket Location from init  :   " + bucketLocation);
        }
    }


    public SendMessageRequest storeMessageInS3(SendMessageRequest sendMessageRequest) {

        commonService.checkMessageAttributes(sendMessageRequest.getMessageAttributes());

        String s3Key = UUID.randomUUID().toString();

        // Read the content of the message from message body
        String messageContentStr = sendMessageRequest.getMessageBody();

        Long messageContentSize = CommonService.getStringSizeInBytes(messageContentStr);

        // Add a new message attribute as a flag
        MessageAttributeValue messageAttributeValue = new MessageAttributeValue();
        messageAttributeValue.setDataType("Number");
        messageAttributeValue.setStringValue(messageContentSize.toString());
        sendMessageRequest.addMessageAttributesEntry(ClientConstants.RESERVED_ATTRIBUTE_NAME,
                messageAttributeValue);

        // Store the message content in S3.
        storeTextInS3(s3Key, messageContentStr, messageContentSize);
        LOG.info("S3 object created, Bucket name: " + configuration.getS3BucketName() + ", Object key: " + s3Key
                + ".");

        // Convert S3 pointer (bucket name, key, etc) to JSON string
        S3PointerMessage s3Pointer = new S3PointerMessage(configuration.getS3BucketName(), s3Key);

        String s3PointerStr = getJSONFromS3Pointer(s3Pointer);

        // Storing S3 pointer in the message body.
        sendMessageRequest.setMessageBody(s3PointerStr);

        return sendMessageRequest;
    }

    public S3PointerMessage readMessageS3PointerFromJSON(String messageBody) {

        S3PointerMessage s3Pointer;
        try {
            JsonDataConverter jsonDataConverter = new JsonDataConverter();
            s3Pointer = jsonDataConverter.deserializeFromJson(messageBody, S3PointerMessage.class);
        } catch (Exception e) {
            String errorMessage = "Failed to read the S3 object pointer from an SQS message. Message was not received.";
            LOG.error(errorMessage, e);
            throw new AmazonClientException(errorMessage, e);
        }
        return s3Pointer;
    }


    public String getTextFromS3(String s3BucketName, String s3Key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(s3BucketName, s3Key);
        String embeddedText = null;
        S3Object obj;
        try {
            obj = s3Client.getObject(getObjectRequest);
        } catch (AmazonServiceException e) {
            String errorMessage = "Failed to get the S3 object which contains the message payload. Message was not received.";
            LOG.error(errorMessage, e);
            throw new AmazonServiceException(errorMessage, e);
        } catch (AmazonClientException e) {
            String errorMessage = "Failed to get the S3 object which contains the message payload. Message was not received.";
            LOG.error(errorMessage, e);
            throw new AmazonClientException(errorMessage, e);
        }
        S3ObjectInputStream is = obj.getObjectContent();
        try {
            embeddedText = IOUtils.toString(is);
        } catch (IOException e) {
            String errorMessage = "Failure when handling the message which was read from S3 object. Message was not received.";
            LOG.error(errorMessage, e);
            throw new AmazonClientException(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(is, LOG);
        }
        return embeddedText;
    }


    private void storeTextInS3(String s3Key, String messageContentStr, Long messageContentSize) {
        InputStream messageContentStream = new ByteArrayInputStream(messageContentStr.getBytes(StandardCharsets.UTF_8));
        ObjectMetadata messageContentStreamMetadata = new ObjectMetadata();
        messageContentStreamMetadata.setContentLength(messageContentSize);
        PutObjectRequest putObjectRequest = new PutObjectRequest(configuration.getS3BucketName(), s3Key,
                messageContentStream, messageContentStreamMetadata);
        try {
            s3Client.putObject(putObjectRequest);
        } catch (AmazonServiceException e) {
            String errorMessage = "Failed to store the message content in an S3 object. SQS message was not sent.";
            LOG.error(errorMessage, e);
            throw new AmazonServiceException(errorMessage, e);
        } catch (AmazonClientException e) {
            String errorMessage = "Failed to store the message content in an S3 object. SQS message was not sent.";
            LOG.error(errorMessage, e);
            throw new AmazonClientException(errorMessage, e);
        }
    }

    private String getJSONFromS3Pointer(S3PointerMessage s3Pointer) {
        String s3PointerStr;
        try {
            JsonDataConverter jsonDataConverter = new JsonDataConverter();
            s3PointerStr = jsonDataConverter.serializeToJson(s3Pointer);
        } catch (Exception e) {
            String errorMessage = "Failed to convert S3 object pointer to text. Message was not sent.";
            LOG.error(errorMessage, e);
            throw new AmazonClientException(errorMessage, e);
        }
        return s3PointerStr;
    }


    public void deleteMessagePayloadFromS3(String receiptHandle) {
        String s3MsgBucketName = getFromReceiptHandleByMarker(receiptHandle,
                ClientConstants.S3_BUCKET_NAME_MARKER);
        String s3MsgKey = getFromReceiptHandleByMarker(receiptHandle, ClientConstants.S3_KEY_MARKER);
        try {
            s3Client.deleteObject(s3MsgBucketName, s3MsgKey);
        } catch (AmazonServiceException e) {
            String errorMessage = "Failed to delete the S3 object which contains the SQS message payload. SQS message was not deleted.";
            LOG.error(errorMessage, e);
            throw new AmazonServiceException(errorMessage, e);
        } catch (AmazonClientException e) {
            String errorMessage = "Failed to delete the S3 object which contains the SQS message payload. SQS message was not deleted.";
            LOG.error(errorMessage, e);
            throw new AmazonClientException(errorMessage, e);
        }
        LOG.info("S3 object deleted, Bucket name: " + s3MsgBucketName + ", Object key: " + s3MsgKey + ".");
    }

    private String getFromReceiptHandleByMarker(String receiptHandle, String marker) {
        int firstOccurence = receiptHandle.indexOf(marker);
        int secondOccurence = receiptHandle.indexOf(marker, firstOccurence + 1);
        return receiptHandle.substring(firstOccurence + marker.length(), secondOccurence);
    }


    public String embedS3PointerInReceiptHandle(String receiptHandle, String s3MsgBucketName, String s3MsgKey) {
        return ClientConstants.S3_BUCKET_NAME_MARKER + s3MsgBucketName
                + ClientConstants.S3_BUCKET_NAME_MARKER + ClientConstants.S3_KEY_MARKER
                + s3MsgKey + ClientConstants.S3_KEY_MARKER + receiptHandle;
    }


    public String getOrigReceiptHandle(String receiptHandle) {
        int secondOccurence = receiptHandle.indexOf(ClientConstants.S3_KEY_MARKER,
                receiptHandle.indexOf(ClientConstants.S3_KEY_MARKER) + 1);
        return receiptHandle.substring(secondOccurence + ClientConstants.S3_KEY_MARKER.length());
    }


    public boolean isS3ReceiptHandle(String receiptHandle) {
        return receiptHandle.contains(ClientConstants.S3_BUCKET_NAME_MARKER)
                && receiptHandle.contains(ClientConstants.S3_KEY_MARKER);
    }

}
