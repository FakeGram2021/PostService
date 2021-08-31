package tim6.postservice.common;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public abstract class CommonTestBase {

    static final ElasticsearchContainer elasticSearchContainer;
    static final KafkaContainer kafkaContainer;
    static final Long oneGigabyte = 1024L * 1024L * 1024L;

    static {
        elasticSearchContainer =
                new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.13.2");
        elasticSearchContainer.withSharedMemorySize(oneGigabyte);
        elasticSearchContainer.start();

        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));
        kafkaContainer.start();
    }

    @BeforeClass
    public static void initializeSystemProperties() {
        System.setProperty(
                "spring.elasticsearch.rest.uris",
                elasticSearchContainer.getContainerIpAddress()
                        + ":"
                        + elasticSearchContainer.getMappedPort(9200));
    }
}
