package com.opentable.privatedining.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.opentable.privatedining.common.Constant;
import java.io.IOException;
import java.time.LocalTime;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register JSR310 module for Java 8 time support
        mapper.findAndRegisterModules();

        setLocalTimeConverter(mapper);

        setObjectIdConverter(mapper);
        return mapper;
    }

    private static void setLocalTimeConverter(ObjectMapper mapper) {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(Constant.FORMATTER));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(Constant.FORMATTER));
        mapper.registerModule(javaTimeModule);
    }

    private static void setObjectIdConverter(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        // Custom serializer to convert ObjectId to String
        module.addSerializer(ObjectId.class, new JsonSerializer<ObjectId>() {
            @Override
            public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
                if (objectId != null) {
                    jsonGenerator.writeString(objectId.toString());
                } else {
                    jsonGenerator.writeNull();
                }
            }
        });

        // Custom deserializer to convert String to ObjectId
        module.addDeserializer(ObjectId.class, new JsonDeserializer<ObjectId>() {
            @Override
            public ObjectId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
                String value = jsonParser.getValueAsString();
                return value != null && !value.isEmpty() ? new ObjectId(value) : null;
            }
        });

        mapper.registerModule(module);
    }
}