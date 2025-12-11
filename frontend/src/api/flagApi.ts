import axios from 'axios';
import type {
  Flag,
  Rule,
  CreateFlagRequest,
  UpdateFlagRequest,
  CreateRuleRequest,
  EvaluateRequest,
  EvaluationResponse,
  Analytics,
  ApiResponse,
} from '../types';

const getApiBase = () => {
  if (import.meta.env.VITE_API_URL) {
    return import.meta.env.VITE_API_URL;
  }
  // In production, use the backend URL from environment or default
  if (typeof window !== 'undefined' && window.location.hostname !== 'localhost') {
    return 'https://featureflag-manager-production.up.railway.app/api';
  }
  return 'http://localhost:8080/api';
};

const API_BASE = getApiBase();

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Flags
export const getFlags = async (): Promise<Flag[]> => {
  const response = await api.get<ApiResponse<Flag[]>>('/flags');
  return response.data.data;
};

export const getFlag = async (id: string): Promise<Flag> => {
  const response = await api.get<ApiResponse<Flag>>(`/flags/${id}`);
  return response.data.data;
};

export const createFlag = async (data: CreateFlagRequest): Promise<Flag> => {
  const response = await api.post<ApiResponse<Flag>>('/flags', data);
  return response.data.data;
};

export const updateFlag = async (id: string, data: UpdateFlagRequest): Promise<Flag> => {
  const response = await api.put<ApiResponse<Flag>>(`/flags/${id}`, data);
  return response.data.data;
};

export const toggleFlag = async (id: string, enabled: boolean): Promise<void> => {
  await api.patch(`/flags/${id}/toggle?enabled=${enabled}`);
};

export const deleteFlag = async (id: string): Promise<void> => {
  await api.delete(`/flags/${id}`);
};

// Rules
export const getRules = async (flagId: string): Promise<Rule[]> => {
  const response = await api.get<ApiResponse<Rule[]>>(`/flags/${flagId}/rules`);
  return response.data.data;
};

export const addRule = async (flagId: string, data: CreateRuleRequest): Promise<Rule> => {
  const response = await api.post<ApiResponse<Rule>>(`/flags/${flagId}/rules`, data);
  return response.data.data;
};

export const toggleRule = async (ruleId: string, enabled: boolean): Promise<void> => {
  await api.patch(`/flags/rules/${ruleId}/toggle?enabled=${enabled}`);
};

export const deleteRule = async (ruleId: string): Promise<void> => {
  await api.delete(`/flags/rules/${ruleId}`);
};

// Evaluation
export const evaluateFlags = async (data: EvaluateRequest): Promise<EvaluationResponse> => {
  const response = await api.post<ApiResponse<EvaluationResponse>>('/flags/evaluate', data);
  return response.data.data;
};

// Analytics
export const getAnalytics = async (flagId: string, hours: number = 24): Promise<Analytics> => {
  const response = await api.get<ApiResponse<Analytics>>(`/flags/${flagId}/analytics?hours=${hours}`);
  return response.data.data;
};

// Health
export const healthCheck = async (): Promise<Record<string, unknown>> => {
  const response = await api.get<ApiResponse<Record<string, unknown>>>('/health');
  return response.data.data;
};
