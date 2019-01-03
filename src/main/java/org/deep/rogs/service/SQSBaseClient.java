package org.deep.rogs.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;

import java.util.List;
import java.util.Map;

public abstract class SQSBaseClient implements AmazonSQS {

    AmazonSQS sqsBaseClient;

    public SQSBaseClient(AmazonSQS sqsClient) {
        sqsBaseClient = sqsClient;
    }

    public SendMessageResult sendMessage(SendMessageRequest sendMessageRequest) {
        return sqsBaseClient.sendMessage(sendMessageRequest);
    }


    public ReceiveMessageResult receiveMessage(ReceiveMessageRequest receiveMessageRequest) {
        return sqsBaseClient.receiveMessage(receiveMessageRequest);
    }


    public DeleteMessageResult deleteMessage(DeleteMessageRequest deleteMessageRequest) {
        return sqsBaseClient.deleteMessage(deleteMessageRequest);
    }


    public SendMessageResult sendMessage(String queueUrl, String messageBody) throws AmazonClientException {

        return sqsBaseClient.sendMessage(queueUrl, messageBody);
    }


    public ReceiveMessageResult receiveMessage(String queueUrl) throws AmazonClientException {

        return sqsBaseClient.receiveMessage(queueUrl);
    }

    public DeleteMessageResult deleteMessage(String queueUrl, String receiptHandle)
            throws AmazonClientException {

        return sqsBaseClient.deleteMessage(queueUrl, receiptHandle);
    }


    public SetQueueAttributesResult setQueueAttributes(SetQueueAttributesRequest setQueueAttributesRequest)
            throws AmazonClientException {

        return sqsBaseClient.setQueueAttributes(setQueueAttributesRequest);

    }


    public ChangeMessageVisibilityBatchResult changeMessageVisibilityBatch(
            ChangeMessageVisibilityBatchRequest changeMessageVisibilityBatchRequest) throws AmazonClientException {

        return sqsBaseClient.changeMessageVisibilityBatch(changeMessageVisibilityBatchRequest);
    }


    public ChangeMessageVisibilityResult changeMessageVisibility(ChangeMessageVisibilityRequest changeMessageVisibilityRequest)
            throws AmazonClientException {

        return sqsBaseClient.changeMessageVisibility(changeMessageVisibilityRequest);
    }


    public GetQueueUrlResult getQueueUrl(GetQueueUrlRequest getQueueUrlRequest) throws AmazonClientException {

        return sqsBaseClient.getQueueUrl(getQueueUrlRequest);
    }


    public RemovePermissionResult removePermission(RemovePermissionRequest removePermissionRequest)
            throws AmazonClientException {

        return sqsBaseClient.removePermission(removePermissionRequest);
    }


    public GetQueueAttributesResult getQueueAttributes(GetQueueAttributesRequest getQueueAttributesRequest)
            throws AmazonClientException {

        return sqsBaseClient.getQueueAttributes(getQueueAttributesRequest);
    }


    public SendMessageBatchResult sendMessageBatch(SendMessageBatchRequest sendMessageBatchRequest)
            throws AmazonClientException {

        return sqsBaseClient.sendMessageBatch(sendMessageBatchRequest);
    }


    public PurgeQueueResult purgeQueue(PurgeQueueRequest purgeQueueRequest)
            throws AmazonClientException {

        return sqsBaseClient.purgeQueue(purgeQueueRequest);

    }


    public ListDeadLetterSourceQueuesResult listDeadLetterSourceQueues(
            ListDeadLetterSourceQueuesRequest listDeadLetterSourceQueuesRequest) throws AmazonClientException {

        return sqsBaseClient.listDeadLetterSourceQueues(listDeadLetterSourceQueuesRequest);
    }


    public DeleteQueueResult deleteQueue(DeleteQueueRequest deleteQueueRequest)
            throws AmazonClientException {

        return sqsBaseClient.deleteQueue(deleteQueueRequest);
    }


    public ListQueuesResult listQueues(ListQueuesRequest listQueuesRequest)
            throws AmazonClientException {

        return sqsBaseClient.listQueues(listQueuesRequest);
    }


    public DeleteMessageBatchResult deleteMessageBatch(DeleteMessageBatchRequest deleteMessageBatchRequest)
            throws AmazonClientException {

        return sqsBaseClient.deleteMessageBatch(deleteMessageBatchRequest);
    }


    public CreateQueueResult createQueue(CreateQueueRequest createQueueRequest)
            throws AmazonClientException {

        return sqsBaseClient.createQueue(createQueueRequest);
    }


    public AddPermissionResult addPermission(AddPermissionRequest addPermissionRequest)
            throws AmazonClientException {

        return sqsBaseClient.addPermission(addPermissionRequest);
    }


    public ListQueuesResult listQueues() throws AmazonClientException {

        return sqsBaseClient.listQueues();
    }


    public SetQueueAttributesResult setQueueAttributes(String queueUrl, Map<String, String> attributes)
            throws AmazonClientException {

        return sqsBaseClient.setQueueAttributes(queueUrl, attributes);
    }


    public ChangeMessageVisibilityBatchResult changeMessageVisibilityBatch(String queueUrl,
                                                                           List<ChangeMessageVisibilityBatchRequestEntry> entries)
            throws AmazonClientException {

        return sqsBaseClient.changeMessageVisibilityBatch(queueUrl, entries);
    }


    public ChangeMessageVisibilityResult changeMessageVisibility(String queueUrl, String receiptHandle, Integer visibilityTimeout)
            throws AmazonClientException {

        return sqsBaseClient.changeMessageVisibility(queueUrl, receiptHandle, visibilityTimeout);
    }


    public GetQueueUrlResult getQueueUrl(String queueName) throws AmazonClientException {

        return sqsBaseClient.getQueueUrl(queueName);
    }


    public RemovePermissionResult removePermission(String queueUrl, String label)
            throws AmazonClientException {

        return sqsBaseClient.removePermission(queueUrl, label);
    }


    public GetQueueAttributesResult getQueueAttributes(String queueUrl, List<String> attributeNames)
            throws AmazonClientException {

        return sqsBaseClient.getQueueAttributes(queueUrl, attributeNames);
    }


    public SendMessageBatchResult sendMessageBatch(String queueUrl, List<SendMessageBatchRequestEntry> entries)
            throws AmazonClientException {

        return sqsBaseClient.sendMessageBatch(queueUrl, entries);
    }


    public DeleteQueueResult deleteQueue(String queueUrl) throws AmazonClientException {

        return sqsBaseClient.deleteQueue(queueUrl);
    }


    public ListQueuesResult listQueues(String queueNamePrefix) throws AmazonClientException {

        return sqsBaseClient.listQueues(queueNamePrefix);
    }


    public DeleteMessageBatchResult deleteMessageBatch(String queueUrl, List<DeleteMessageBatchRequestEntry> entries)
            throws AmazonClientException {

        return sqsBaseClient.deleteMessageBatch(queueUrl, entries);
    }


    public CreateQueueResult createQueue(String queueName) throws AmazonClientException {

        return sqsBaseClient.createQueue(queueName);
    }


    public AddPermissionResult addPermission(String queueUrl, String label, List<String> aWSAccountIds, List<String> actions)
            throws AmazonClientException {

        return sqsBaseClient.addPermission(queueUrl, label, aWSAccountIds, actions);
    }


    public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {

        return sqsBaseClient.getCachedResponseMetadata(request);
    }

    public void shutdown() {

        sqsBaseClient.shutdown();
    }

}


