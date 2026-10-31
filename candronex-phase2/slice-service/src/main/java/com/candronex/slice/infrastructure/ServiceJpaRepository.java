package com.candronex.slice.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceJpaRepository extends JpaRepository<ServiceJpaEntity, UUID> {

    List<ServiceJpaEntity> findByClientIdAndOrderIdOrderByAcceptedAt(String clientId, UUID orderId);

    @Modifying
    @Query("update ServiceJpaEntity s set s.state = 'ACTIVATING' where s.id = :id and s.state = 'PENDING'")
    int claim(@Param("id") UUID id);

    @Modifying
    @Query("update ServiceJpaEntity s set s.state = 'PENDING' where s.state = 'ACTIVATING'")
    int resetActivating();

    @Query("select s.id from ServiceJpaEntity s where s.state = 'PENDING' order by s.acceptedAt")
    List<UUID> findPendingIds();
}
