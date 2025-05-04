package sanity.nil.block.presentation.scheduled;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.quartz.*;

@JBossLog
@ApplicationScoped
public class DelayedTaskOrchestrator {

    @Inject
    org.quartz.Scheduler scheduler;
    @ConfigProperty(name = "application.scheduler.interval", defaultValue = "3")
    int intervalInSeconds;

    void onStart(@Observes StartupEvent event) throws SchedulerException {
        JobKey jobKey = new JobKey("deleteBlocks", "block-service");
        if (!scheduler.checkExists(jobKey)) {
            JobDetail job = JobBuilder.newJob(DeleteBlocksJob.class)
                    .withIdentity("deleteBlocks", "block-service")
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("deleteBlocks-tg", "block-service")
                    .startNow()
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(intervalInSeconds)
                                    .repeatForever())
                    .build();
            scheduler.scheduleJob(job, trigger);
        } else {
            log.info("Quartz job already exists: " + jobKey);
        }
    }
}
