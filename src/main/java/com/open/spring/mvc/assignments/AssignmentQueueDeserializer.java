package com.open.spring.mvc.assignments;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.List;

public class AssignmentQueueDeserializer extends JsonDeserializer<AssignmentQueue> {
    @Override
    public AssignmentQueue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode node = mapper.readTree(jp);
        
        AssignmentQueue queue = new AssignmentQueue();
        
        // Handle the ID field
        if (node.has("id")) {
            JsonNode idNode = node.get("id");
            if (!idNode.isNull()) {
                queue.setId(idNode.asLong());
            }
        }
        
        // Convert the array nodes to List<String> using TypeReference
        if (node.has("working")) {
            queue.setWorking(mapper.convertValue(node.get("working"), 
                new TypeReference<List<String>>() {}));
        }
        
        if (node.has("waiting")) {
            queue.setWaiting(mapper.convertValue(node.get("waiting"), 
                new TypeReference<List<String>>() {}));
        }
        
        if (node.has("completed")) {
            queue.setCompleted(mapper.convertValue(node.get("completed"), 
                new TypeReference<List<String>>() {}));
        }
        
        return queue;
    }
}