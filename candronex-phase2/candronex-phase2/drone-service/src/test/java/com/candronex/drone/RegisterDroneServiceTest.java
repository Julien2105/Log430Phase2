package com.candronex.drone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.candronex.drone.application.DroneQueryService;
import com.candronex.drone.application.RegisterDroneService;
import com.candronex.drone.application.RegisterDroneUseCase.Command;
import com.candronex.drone.domain.ClientId;
import com.candronex.drone.domain.DomainException;
import com.candronex.drone.domain.DroneId;
import com.candronex.drone.domain.Imsi;
import com.candronex.drone.support.FixedClock;
import com.candronex.drone.support.InMemoryDroneRepository;
import com.candronex.drone.support.RecordingAuditTrail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** U-01 à U-08 : UC-02 (enregistrement et consultation des drones). */
class RegisterDroneServiceTest {

    private static final ClientId A = ClientId.of("client-a");
    private static final ClientId B = ClientId.of("client-b");
    private static final String IMSI_1 = "302720123456789";
    private static final String IMSI_2 = "302720123456790";

    private InMemoryDroneRepository repo;
    private RecordingAuditTrail audit;
    private RegisterDroneService service;
    private DroneQueryService query;

    @BeforeEach
    void setUp() {
        repo = new InMemoryDroneRepository();
        audit = new RecordingAuditTrail();
        service = new RegisterDroneService(repo, audit, new FixedClock());
        query = new DroneQueryService(repo);
    }

    private Command cmd(ClientId c, String id, String imsi, String label) {
        return new Command(c, DroneId.of(id), Imsi.of(imsi), label);
    }

    @Test
    void registersADroneForTheClient() {
        var r = service.register(cmd(A, "DRN-0231", IMSI_1, "Nord"));
        assertThat(r.created()).isTrue();
        assertThat(repo.size()).isEqualTo(1);
        assertThat(audit.events).containsExactly("DRONE_REGISTERED");
    }

    @Test
    void replayWithSameContentReturnsTheSameDroneWithoutDuplicate() {
        service.register(cmd(A, "DRN-0231", IMSI_1, "Nord"));
        var again = service.register(cmd(A, "DRN-0231", IMSI_1, "Nord"));
        assertThat(again.created()).isFalse();
        assertThat(repo.size()).isEqualTo(1);
    }

    @Test
    void sameDroneIdWithDifferentContentIsAConflict() {
        service.register(cmd(A, "DRN-0231", IMSI_1, "Nord"));
        assertThatThrownBy(() -> service.register(cmd(A, "DRN-0231", IMSI_2, "Nord")))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.code()).isEqualTo("DRONE_CONFLICT"));
    }

    @Test
    void imsiAlreadyUsedByAnotherDroneIsRejected() {
        service.register(cmd(A, "DRN-0231", IMSI_1, "Nord"));
        assertThatThrownBy(() -> service.register(cmd(B, "DRN-0999", IMSI_1, "Sud")))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.code()).isEqualTo("IMSI_ALREADY_REGISTERED"));
    }

    @Test
    void droneIdIsUniquePerClientNotGlobally() {
        service.register(cmd(A, "DRN-0231", IMSI_1, "Nord"));
        var other = service.register(cmd(B, "DRN-0231", IMSI_2, "Sud"));
        assertThat(other.created()).isTrue();
        assertThat(repo.size()).isEqualTo(2);
    }

    @Test
    void invalidImsiIsRejectedByTheValueObject() {
        assertThatThrownBy(() -> Imsi.of("123"))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.code()).isEqualTo("INVALID_IMSI"));
    }

    @Test
    void anotherClientsDroneLooksNonExistent() {
        service.register(cmd(A, "DRN-0231", IMSI_1, "Nord"));
        assertThatThrownBy(() -> query.get(B, DroneId.of("DRN-0231")))
                .isInstanceOfSatisfying(DomainException.class, e -> assertThat(e.kind()).isEqualTo(DomainException.Kind.NOT_FOUND));
    }

    @Test
    void listIsFilteredByClient() {
        service.register(cmd(A, "DRN-0231", IMSI_1, "Nord"));
        service.register(cmd(B, "DRN-0500", IMSI_2, "Sud"));
        assertThat(query.list(A)).hasSize(1);
    }

    @Test
    void imsiIsNeverPrintedByToString() {
        assertThat(Imsi.of(IMSI_1).toString()).doesNotContain(IMSI_1);
    }
}
