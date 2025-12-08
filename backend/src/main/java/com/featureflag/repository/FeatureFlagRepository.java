package com.featureflag.repository;

import com.featureflag.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {

    Optional<FeatureFlag> findByName(String name);

    boolean existsByName(String name);

    List<FeatureFlag> findByEnabledTrue();

    @Query("SELECT f FROM FeatureFlag f LEFT JOIN FETCH f.rules WHERE f.enabled = true")
    List<FeatureFlag> findAllEnabledWithRules();

    @Query("SELECT f FROM FeatureFlag f LEFT JOIN FETCH f.rules WHERE f.id = :id")
    Optional<FeatureFlag> findByIdWithRules(@Param("id") UUID id);

    @Query("SELECT f FROM FeatureFlag f LEFT JOIN FETCH f.rules WHERE f.name = :name")
    Optional<FeatureFlag> findByNameWithRules(@Param("name") String name);

    @Modifying
    @Query("UPDATE FeatureFlag f SET f.enabled = :enabled WHERE f.id = :id")
    int updateEnabledStatus(@Param("id") UUID id, @Param("enabled") boolean enabled);

    @Modifying
    @Query("UPDATE FeatureFlag f SET f.rolloutPercentage = :percentage WHERE f.id = :id")
    int updateRolloutPercentage(@Param("id") UUID id, @Param("percentage") int percentage);

    @Query("SELECT COUNT(f) FROM FeatureFlag f WHERE f.enabled = true")
    long countEnabled();

    List<FeatureFlag> findAllByOrderByCreatedAtDesc();
}
