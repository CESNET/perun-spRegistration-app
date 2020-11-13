package cz.metacentrum.perun.spRegistration.persistence;

import cz.metacentrum.perun.spRegistration.persistence.managers.LinkCodeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledTasks {

    private final LinkCodeManager linkCodeManager;

    @Autowired
    public ScheduledTasks(LinkCodeManager linkCodeManager) {
        this.linkCodeManager = linkCodeManager;
    }

    @Scheduled(cron = "21 4 0 * * *", zone = "Europe/Prague")
    public void removeExpiredCodes() {
        linkCodeManager.deleteExpired();
    }

}
