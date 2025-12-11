-- Demo feature flags
INSERT INTO feature_flags (id, name, description, enabled, rollout_percentage, created_at, updated_at) VALUES
  (gen_random_uuid(), 'dark_mode', 'Enable dark mode theme for the application', true, 100, NOW(), NOW()),
  (gen_random_uuid(), 'new_checkout_flow', 'Redesigned checkout experience with fewer steps', true, 50, NOW(), NOW()),
  (gen_random_uuid(), 'ai_recommendations', 'AI-powered product recommendations on homepage', false, 0, NOW(), NOW()),
  (gen_random_uuid(), 'beta_dashboard', 'New analytics dashboard for beta testers', true, 25, NOW(), NOW()),
  (gen_random_uuid(), 'premium_features', 'Unlock premium features for subscribed users', true, 100, NOW(), NOW());
