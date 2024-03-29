package no.nav.helse.flex

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureObservability
@SpringBootTest(classes = [Application::class])
abstract class FellesTestOppsett {

    companion object {
        init {
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.1")).also {
                it.start()
                System.setProperty("KAFKA_BROKERS", it.bootstrapServers)
            }
        }
    }
}
