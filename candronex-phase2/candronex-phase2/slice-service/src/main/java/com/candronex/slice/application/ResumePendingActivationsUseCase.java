package com.candronex.slice.application;

/** Reprise au démarrage : rejoue les activations PENDING et interrompues (ACTIVATING). */
public interface ResumePendingActivationsUseCase {

    int resume();
}
