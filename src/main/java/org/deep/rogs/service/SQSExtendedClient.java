package org.deep.rogs.service;


import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deep.rogs.conf.ClientConstants;
import org.deep.rogs.conf.SQSClientConfiguration;
import org.deep.rogs.model.S3PointerMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SQSExtendedClient extends SQSBaseClient implements AmazonSQS {

    private static final Log LOG = LogFactory.getLog(SQSExtendedClient.class);

    @Autowired
    private SQSClientConfiguration clientConfiguration;

    @Autowired
    private S3BaseClient s3Client;

    @Autowired
    private CommonService commonService;



    public SQSExtendedClient(AmazonSQS sqsClient) {
        super(sqsClient);
    }

    public SendMessageResult sendMessage(SendMessageRequest sendMessageRequest) {

        if (sendMessageRequest == null) {
            String errorMessage = "sendMessageRequest cannot be null.";
            LOG.error(errorMessage);
            throw new AmazonClientException(errorMessage);
        }

        sendMessageRequest.getRequestClientOptions().appendUserAgent(ClientConstants.USER_AGENT_HEADER);

        if (!clientConfiguration.isLargePayloadSupport()) {
            return super.sendMessage(sendMessageRequest);
        }

        if (sendMessageRequest.getMessageBody() == null || "".equals(sendMessageRequest.getMessageBody())) {
            String errorMessage = "messageBody cannot be null or empty.";
            LOG.error(errorMessage);
            throw new AmazonClientException(errorMessage);
        }

        // Storing Message in S3
        if (clientConfiguration.isAlwaysThroughS3() || isLarge(sendMessageRequest)) {
            sendMessageRequest = s3Client.storeMessageInS3(sendMessageRequest);
        }
        return super.sendMessage(sendMessageRequest);
    }

    public SendMessageResult sendMessage(String queueUrl, String messageBody) {
        SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, messageBody);
        return sendMessage(sendMessageRequest);
    }


    public ReceiveMessageResult receiveMessage(ReceiveMessageRequest receiveMessageRequest) {

        if (receiveMessageRequest == null) {
            String errorMessage = "receiveMessageRequest cannot be null.";
            LOG.error(errorMessage);
            throw new AmazonClientException(errorMessage);
        }

        receiveMessageRequest.getRequestClientOptions().appendUserAgent(ClientConstants.USER_AGENT_HEADER);

        if (!clientConfiguration.isLargePayloadSupport()) {
            return super.receiveMessage(receiveMessageRequest);
        }

        if (!receiveMessageRequest.getMessageAttributeNames().contains(ClientConstants.RESERVED_ATTRIBUTE_NAME)) {
            receiveMessageRequest.getMessageAttributeNames().add(ClientConstants.RESERVED_ATTRIBUTE_NAME);
        }

        ReceiveMessageResult receiveMessageResult = super.receiveMessage(receiveMessageRequest);

        List<Message> messages = receiveMessageResult.getMessages();
        for (Message message : messages) {

            // for each received message check if they are stored in S3.
            MessageAttributeValue largePayloadAttributeValue = message.getMessageAttributes().get(
                    ClientConstants.RESERVED_ATTRIBUTE_NAME);
            if (largePayloadAttributeValue != null) {
                String messageBody = message.getBody();

                // read the S3 pointer from the message body JSON string.
                S3PointerMessage s3Pointer = s3Client.readMessageS3PointerFromJSON(messageBody);

                String s3MsgBucketName = s3Pointer.getS3BucketName();
                String s3MsgKey = s3Pointer.getS3Key();

                String origMsgBody = s3Client.getTextFromS3(s3MsgBucketName, s3MsgKey);
                LOG.info("S3 object read, Bucket name: " + s3MsgBucketName + ", Object key: " + s3MsgKey + ".");

                message.setBody(origMsgBody);

                // remove the additional attribute before returning the message
                // to user.
                message.getMessageAttributes().remove(ClientConstants.RESERVED_ATTRIBUTE_NAME);

                // Embed s3 object pointer in the receipt handle.
                String modifiedReceiptHandle = s3Client.embedS3PointerInReceiptHandle(message.getReceiptHandle(),
                        s3MsgBucketName, s3MsgKey);

                message.setReceiptHandle(modifiedReceiptHandle);
            }
        }
        return receiveMessageResult;
    }


    public ReceiveMessageResult receiveMessage(String queueUrl) {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        return receiveMessage(receiveMessageRequest);
    }


    public DeleteMessageResult deleteMessage(DeleteMessageRequest deleteMessageRequest) {

        if (deleteMessageRequest == null) {
            String errorMessage = "deleteMessageRequest cannot be null.";
            LOG.error(errorMessage);
            throw new AmazonClientException(errorMessage);
        }

        //deleteMessageRequest.getRequestClientOptions().appendUserAgent(SQSExtendedClientConstants.USER_AGENT_HEADER);

        if (!clientConfiguration.isLargePayloadSupport()) {
            return super.deleteMessage(deleteMessageRequest);
        }

        String receiptHandle = deleteMessageRequest.getReceiptHandle();
        String origReceiptHandle = receiptHandle;
        if (s3Client.isS3ReceiptHandle(receiptHandle)) {
            s3Client.deleteMessagePayloadFromS3(receiptHandle);
            origReceiptHandle = s3Client.getOrigReceiptHandle(receiptHandle);
        }
        deleteMessageRequest.setReceiptHandle(origReceiptHandle);
        return super.deleteMessage(deleteMessageRequest);
    }


    public DeleteMessageResult deleteMessage(String queueUrl, String receiptHandle) {
        DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, receiptHandle);
        return deleteMessage(deleteMessageRequest);
    }


    public ChangeMessageVisibilityResult changeMessageVisibility(String queueUrl,
                                                                 String receiptHandle,
                                                                 Integer visibilityTimeout) {
        ChangeMessageVisibilityRequest changeMessageVisibilityRequest =
                new ChangeMessageVisibilityRequest(queueUrl, receiptHandle, visibilityTimeout);
        return changeMessageVisibility(changeMessageVisibilityRequest);
    }


    public ChangeMessageVisibilityResult changeMessageVisibility(ChangeMessageVisibilityRequest changeMessageVisibilityRequest)
            throws AmazonClientException {

        if (s3Client.isS3ReceiptHandle(changeMessageVisibilityRequest.getReceiptHandle())) {
            changeMessageVisibilityRequest.setReceiptHandle(
                    s3Client.getOrigReceiptHandle(changeMessageVisibilityRequest.getReceiptHandle()));
        }
        return sqsBaseClient.changeMessageVisibility(changeMessageVisibilityRequest);
    }


    public PurgeQueueResult purgeQueue(PurgeQueueRequest purgeQueueRequest)
            throws AmazonClientException {
        LOG.warn("Calling purgeQueue deletes SQS messages without deleting their payload from S3.");

        if (purgeQueueRequest == null) {
            String errorMessage = "purgeQueueRequest cannot be null.";
            LOG.error(errorMessage);
            throw new AmazonClientException(errorMessage);
        }

        purgeQueueRequest.getRequestClientOptions().appendUserAgent(ClientConstants.USER_AGENT_HEADER);

        return super.purgeQueue(purgeQueueRequest);
    }

    private boolean isLarge(SendMessageRequest sendMessageRequest) {
        int msgAttributesSize = commonService.getMsgAttributesSize(sendMessageRequest.getMessageAttributes());
        long msgBodySize = CommonService.getStringSizeInBytes(sendMessageRequest.getMessageBody());
        long totalMsgSize = msgAttributesSize + msgBodySize;
        return (totalMsgSize > ClientConstants.DEFAULT_MESSAGE_SIZE_THRESHOLD);
    }

}
