package io.tolgee.component.bucket

import io.tolgee.fixtures.RedisRunner
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(
  properties = [
    "tolgee.cache.use-redis=true",
    "tolgee.cache.enabled=true",
    "tolgee.websocket.use-redis=true",
    "spring.redis.port=56379",
  ],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ContextConfiguration(initializers = [TokenBucketManagerTestWithRedis.Companion.Initializer::class])
class TokenBucketManagerTestWithRedis : AbstractTokenBucketManagerTest() {
  companion object {
    val redisRunner = RedisRunner()

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
      override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        redisRunner.run()
      }
    }
  }
}
