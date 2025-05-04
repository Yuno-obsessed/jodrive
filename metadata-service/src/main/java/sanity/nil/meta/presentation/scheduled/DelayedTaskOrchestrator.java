package sanity.nil.meta.presentation.scheduled;

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
    Scheduler scheduler;
    @ConfigProperty(name = "application.scheduler.interval", defaultValue = "2")
    int intervalInSeconds;

    void onStart(@Observes StartupEvent event) throws SchedulerException {
        JobKey jobKey = new JobKey("deleteFile", "metadata-service");
        if (!scheduler.checkExists(jobKey)) {
            JobDetail job = JobBuilder.newJob(DeleteFileJob.class)
                    .withIdentity("deleteFile", "metadata-service")
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("deleteFile-tg", "metadata-service")
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
