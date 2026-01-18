package com.opentable.privatedining.config;

import com.opentable.privatedining.common.Constant;
import com.opentable.privatedining.converter.LocalTimeToStringConverter;
import com.opentable.privatedining.converter.StringToLocalTimeConverter;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import java.util.List;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
@Profile("!test")
@ImportAutoConfiguration(EmbeddedMongoAutoConfiguration.class)
public class EmbeddedMongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(
            List.of(new LocalTimeToStringConverter(Constant.ZONE_ID), new StringToLocalTimeConverter(
                Constant.ZONE_ID)));
    }
}