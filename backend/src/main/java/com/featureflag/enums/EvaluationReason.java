package com.featureflag.enums;

public enum EvaluationReason {
    FLAG_DISABLED,
    RULE_MATCH,
    ROLLOUT_INCLUDED,
    ROLLOUT_EXCLUDED,
    NO_RULES_DEFAULT
}
