package net.sorted.fileextractor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.google.gson.Gson;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;
import java.util.stream.Collectors;


public class Handler implements RequestHandler<S3Event, String>{

    private final String envQueueName = System.getenv("queuename");
    private final String queueName = (envQueueName != null) ? envQueueName : "file-content-queue";

    private final S3Client s3 = S3Client.builder().region(Region.AP_SOUTHEAST_2).build();
    private final SqsClient sqsClient = SqsClient.builder().region(Region.AP_SOUTHEAST_2).build();

    private final GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
    private final String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();

    private final Gson gson = new Gson();

    @Override
    public String handleRequest(S3Event event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        S3EventNotificationRecord record = event.getRecords().get(0);
        String srcBucket = record.getS3().getBucket().getName();
        // Object key may have spaces or unicode non-ASCII characters.
        String srcKey = record.getS3().getObject().getUrlDecodedKey();
        logger.log("File Extractor 0.0.14\n");
        logger.log("RECORD: " + record + "\n");
        logger.log("SOURCE BUCKET: " + srcBucket + "\n");
        logger.log("SOURCE KEY: " + srcKey + "\n");

        logger.log("EVENT TYPE: " + event.getClass() + "\n");

        try {
            String content = processFile(srcBucket, srcKey);
            logger.log("Bytes read: " + content.length() + "\n");
            logger.log("READ FILE: " + content);
        } catch (Throwable e) {
            logger.log("Error " + e.getMessage() + " \n");
        }


        return srcBucket + "/" + srcKey;
    }

    private String processFile(String bucketName, String keyName) throws Exception {

        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(keyName)
                .bucket(bucketName)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
        String data = objectBytes.asUtf8String();

        var messages = data.lines().map(line -> lineToJson(keyName, line)).collect(Collectors.toList());
        messages.stream().forEach(message -> sendMessage(message));

        return data;

    }

    private void sendMessage(String message) {

        var msg = gson.fromJson(message, Map.class);

        var contentValue = MessageAttributeValue.builder()
                .stringValue(msg.get("type").toString())
                .dataType("String")
                .build();
        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .messageAttributes(Map.of("contentType", contentValue))
                .build();

        sqsClient.sendMessage(sendMsgRequest);
    }

    private String lineToJson(String file, String line) {
        String contentType = "X";
        if (line.contains(",")) {
            contentType = line.split("[,]")[0];
        }
        return gson.toJson(Map.of("content", line, "filename", file, "type", contentType));
    }

}
