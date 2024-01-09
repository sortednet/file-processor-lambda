package net.sorted.fileprocessor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;


public class Handler implements RequestHandler<SQSEvent, String>{

    @Override
    public String handleRequest(SQSEvent event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        logger.log("File Processor 0.0.4\n");
        logger.log("EVENT TYPE: " + event.getClass() + "\n");
        logger.log("EVENT: " + event.toString() + "\n");
        logger.log("NumRecords: " + event.getRecords().size() + "\n");

        var all = event.getRecords();
        all.stream().forEach(msg -> logger.log(toMessageLogFormat(msg)));

        return ""+all.size();
    }

    private String toMessageLogFormat(SQSEvent.SQSMessage message) {
        SQSEvent.MessageAttribute typeAttr = message.getMessageAttributes().get("contentType");
        String type = (typeAttr != null) ? typeAttr.getStringValue() : "none" ;
        return String.format("Type: %s, Content %s",
                type,
                message.getBody());
    }

}
