package uk.gov.digital.ho.hocs.queue.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class ConditionalBeanTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(QueueListener.class, MigrationQueueListener.class);

    @Test
    public void queueListenerSetForMigration() {
        contextRunner
                .withPropertyValues("case-creator.mode=migration")
                .run(context -> assertAll(
                        () -> assertThat(context).hasSingleBean(MigrationQueueListener.class),
                        () -> assertThat(context).doesNotHaveBean(QueueListener.class)));
    }

    @Test
    public void queueListenerSetForCaseCreator() {
        contextRunner
                .withPropertyValues("case-creator.mode=creation")
                .run(context  -> assertAll(
                        () -> assertThat(context).hasSingleBean(QueueListener.class),
                        () -> assertThat(context).doesNotHaveBean(MigrationQueueListener.class)));
    }

    @Test
    public void queueListenerSetToDefaultCaseCreator() {
        contextRunner
                .run(context  -> assertAll(
                        () -> assertThat(context).hasSingleBean(QueueListener.class),
                        () -> assertThat(context).doesNotHaveBean(MigrationQueueListener.class)));
    }



}
