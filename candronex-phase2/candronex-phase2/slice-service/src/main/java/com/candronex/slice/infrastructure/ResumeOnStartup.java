package com.candronex.slice.infrastructure;

import com.candronex.slice.application.ResumePendingActivationsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ResumeOnStartup {

    private static final Logger LOG = LoggerFactory.getLogger(ResumeOnStartup.class);

    private final ResumePendingActivationsUseCase resume;

    public ResumeOnStartup(ResumePendingActivationsUseCase resume) { this.resume = resume; }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        int n = resume.resume();
        if (n > 0) LOG.info("event=ACTIVATIONS_RESUMED count={}", n);
    }
}
