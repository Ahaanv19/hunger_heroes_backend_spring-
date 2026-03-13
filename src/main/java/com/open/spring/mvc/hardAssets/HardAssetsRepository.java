package com.open.spring.mvc.hardAssets;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// JPA is an object-relational mapping (ORM) to persistent data, originally relational databases (SQL). Today JPA implementations has been extended for NoSQL.
public interface HardAssetsRepository extends JpaRepository<HardAsset, Long> {
        List<HardAsset> findByOwnerUID(String ownerUID);
}
