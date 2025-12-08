-- Feature Flags table
CREATE TABLE feature_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT false,
    rollout_percentage INTEGER NOT NULL DEFAULT 0 CHECK (rollout_percentage >= 0 AND rollout_percentage <= 100),
    created_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Flag Rules table for targeting
CREATE TABLE flag_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flag_id UUID NOT NULL REFERENCES feature_flags(id) ON DELETE CASCADE,
    rule_type VARCHAR(50) NOT NULL,
    rule_value VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_rule_per_flag UNIQUE (flag_id, rule_type, rule_value)
);

-- Flag Evaluations table for analytics
CREATE TABLE flag_evaluations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flag_id UUID NOT NULL REFERENCES feature_flags(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    result BOOLEAN NOT NULL,
    matched_rule_id UUID REFERENCES flag_rules(id) ON DELETE SET NULL,
    evaluation_reason VARCHAR(50) NOT NULL,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_flags_name ON feature_flags(name);
CREATE INDEX idx_flags_enabled ON feature_flags(enabled);
CREATE INDEX idx_rules_flag_id ON flag_rules(flag_id);
CREATE INDEX idx_rules_type_value ON flag_rules(rule_type, rule_value);
CREATE INDEX idx_evaluations_flag_id ON flag_evaluations(flag_id);
CREATE INDEX idx_evaluations_user_id ON flag_evaluations(user_id);
CREATE INDEX idx_evaluations_timestamp ON flag_evaluations(evaluated_at);
CREATE INDEX idx_evaluations_flag_timestamp ON flag_evaluations(flag_id, evaluated_at);

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_feature_flags_updated_at
    BEFORE UPDATE ON feature_flags
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_flag_rules_updated_at
    BEFORE UPDATE ON flag_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
