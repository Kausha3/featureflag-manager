package com.featureflag.repository;

import com.featureflag.entity.FlagRule;
import com.featureflag.enums.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlagRuleRepository extends JpaRepository<FlagRule, UUID> {

    List<FlagRule> findByFlagIdOrderByPriorityDesc(UUID flagId);

    List<FlagRule> findByFlagIdAndEnabledTrueOrderByPriorityDesc(UUID flagId);

    Optional<FlagRule> findByFlagIdAndRuleTypeAndRuleValue(UUID flagId, RuleType ruleType, String ruleValue);

    boolean existsByFlagIdAndRuleTypeAndRuleValue(UUID flagId, RuleType ruleType, String ruleValue);

    @Modifying
    @Query("DELETE FROM FlagRule r WHERE r.flag.id = :flagId")
    int deleteByFlagId(@Param("flagId") UUID flagId);

    @Modifying
    @Query("UPDATE FlagRule r SET r.enabled = :enabled WHERE r.id = :id")
    int updateEnabledStatus(@Param("id") UUID id, @Param("enabled") boolean enabled);

    long countByFlagId(UUID flagId);
}
