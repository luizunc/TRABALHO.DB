package br.com.biblioteca.configuracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class ConfiguracaoRedis {

    @Bean
    public RedisConnectionFactory fabricaConexaoRedis() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public ObjectMapper mapeadorObjetoRedis() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public GenericJackson2JsonRedisSerializer serializadorJsonRedisGenerico(ObjectMapper mapeadorObjetoRedis) {
        return new GenericJackson2JsonRedisSerializer(mapeadorObjetoRedis);
    }

    @Bean
    public RedisTemplate<String, Object> templateRedis(
            RedisConnectionFactory fabricaConexao,
            GenericJackson2JsonRedisSerializer serializadorJson) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(fabricaConexao);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializadorJson);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializadorJson);
        return template;
    }

    @Bean
    public CacheManager gerenciadorCache(
            RedisConnectionFactory fabricaConexao,
            GenericJackson2JsonRedisSerializer serializadorJson) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializadorJson))
            .disableCachingNullValues();

        return RedisCacheManager.builder(fabricaConexao)
            .cacheDefaults(config)
            .build();
    }
}



