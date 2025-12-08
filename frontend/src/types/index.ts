export type RuleType = 'USER_ID' | 'EMAIL_DOMAIN' | 'EMAIL_EXACT' | 'COUNTRY' | 'PERCENTAGE_GROUP';
export type EvaluationReason = 'FLAG_DISABLED' | 'RULE_MATCH' | 'ROLLOUT_INCLUDED' | 'ROLLOUT_EXCLUDED' | 'NO_RULES_DEFAULT';

export interface Flag {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
  rolloutPercentage: number;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
  rules?: Rule[];
  rulesCount?: number;
}

export interface Rule {
  id: string;
  flagId: string;
  ruleType: RuleType;
  ruleValue: string;
  enabled: boolean;
  priority: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateFlagRequest {
  name: string;
  description: string;
  enabled: boolean;
  rolloutPercentage: number;
  createdBy?: string;
}

export interface UpdateFlagRequest {
  description?: string;
  enabled?: boolean;
  rolloutPercentage?: number;
}

export interface CreateRuleRequest {
  ruleType: RuleType;
  ruleValue: string;
  enabled: boolean;
  priority: number;
}

export interface EvaluateRequest {
  userId: string;
  userEmail?: string;
  country?: string;
}

export interface EvaluationDetail {
  result: boolean;
  reason: EvaluationReason;
  matchedRuleId?: string;
  explanation: string;
}

export interface EvaluationResponse {
  flags: Record<string, boolean>;
  details: Record<string, EvaluationDetail>;
}

export interface TimeSeriesPoint {
  timestamp: string;
  enabledCount: number;
  disabledCount: number;
  totalCount: number;
}

export interface Analytics {
  flagId: string;
  flagName: string;
  totalEvaluations: number;
  enabledCount: number;
  disabledCount: number;
  enabledPercentage: number;
  configuredRolloutPercentage: number;
  evaluationsOverTime: TimeSeriesPoint[];
  evaluationsByReason: Record<string, number>;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}
