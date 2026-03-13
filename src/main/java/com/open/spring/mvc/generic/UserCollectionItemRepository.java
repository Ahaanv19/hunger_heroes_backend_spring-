package com.open.spring.mvc.generic;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCollectionItemRepository extends JpaRepository<UserCollectionItem, Long> {
    List<UserCollectionItem> findByType(CollectionItemType type);

    List<UserCollectionItem> findByUserIdAndType(String userId, CollectionItemType type);
}
