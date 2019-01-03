package org.deep.rogs.service;


import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deep.rogs.conf.ClientConstants;
import org.deep.rogs.model.CountingOutputStream;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.Map;

public class CommonService {

    private static final Log LOG = LogFactory.getLog(CommonService.class);


    public int getMsgAttributesSize(Map<String, MessageAttributeValue> msgAttributes) {
        int totalMsgAttributesSize = 0;
        for (Map.Entry<String, MessageAttributeValue> entry : msgAttributes.entrySet()) {
            totalMsgAttributesSize += getStringSizeInBytes(entry.getKey());

            MessageAttributeValue entryVal = entry.getValue();
            if (entryVal.getDataType() != null) {
                totalMsgAttributesSize += getStringSizeInBytes(entryVal.getDataType());
            }

            String stringVal = entryVal.getStringValue();
            if (stringVal != null) {
                totalMsgAttributesSize += getStringSizeInBytes(entryVal.getStringValue());
            }

            ByteBuffer binaryVal = entryVal.getBinaryValue();
            if (binaryVal != null) {
                totalMsgAttributesSize += binaryVal.array().length;
            }
        }
        return totalMsgAttributesSize;
    }

    public void checkMessageAttributes(Map<String, MessageAttributeValue> messageAttributes) {
        int msgAttributesSize = getMsgAttributesSize(messageAttributes);
        if (msgAttributesSize > ClientConstants.DEFAULT_MESSAGE_SIZE_THRESHOLD) {
            String errorMessage = "Total size of Message attributes is " + msgAttributesSize
                    + " bytes which is larger than the threshold of " + ClientConstants.DEFAULT_MESSAGE_SIZE_THRESHOLD
                    + " Bytes. Consider including the payload in the message body instead of message attributes.";
            LOG.error(errorMessage);
            throw new AmazonClientException(errorMessage);
        }

        int messageAttributesNum = messageAttributes.size();
        if (messageAttributesNum > ClientConstants.MAX_ALLOWED_ATTRIBUTES) {
            String errorMessage = "Number of message attributes [" + messageAttributesNum
                    + "] exceeds the maximum allowed for large-payload messages ["
                    + ClientConstants.MAX_ALLOWED_ATTRIBUTES + "].";
            LOG.error(errorMessage);
            throw new AmazonClientException(errorMessage);
        }

        MessageAttributeValue largePayloadAttributeValue = messageAttributes
                .get(ClientConstants.RESERVED_ATTRIBUTE_NAME);
        if (largePayloadAttributeValue != null) {
            String errorMessage = "Message attribute name " + ClientConstants.RESERVED_ATTRIBUTE_NAME
                    + " is reserved for use by SQS extended client.";
            LOG.error(errorMessage);
            throw new AmazonClientException(errorMessage);
        }

    }

    public static long getStringSizeInBytes(String str) {
        CountingOutputStream counterOutputStream = new CountingOutputStream();
        try {
            Writer writer = new OutputStreamWriter(counterOutputStream, "UTF-8");
            writer.write(str);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            String errorMessage = "Failed to calculate the size of message payload.";
            LOG.error(errorMessage, e);
            throw new AmazonClientException(errorMessage, e);
        }
        return counterOutputStream.getTotalSize();
    }
}
