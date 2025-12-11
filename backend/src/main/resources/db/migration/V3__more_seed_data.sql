-- Additional demo feature flags
INSERT INTO feature_flags (id, name, description, enabled, rollout_percentage, created_at, updated_at) VALUES
  -- UI/UX Features
  (gen_random_uuid(), 'new_navigation', 'Redesigned sidebar navigation with icons', true, 75, NOW(), NOW()),
  (gen_random_uuid(), 'animated_transitions', 'Smooth page transition animations', true, 60, NOW(), NOW()),

  -- E-commerce Features
  (gen_random_uuid(), 'one_click_buy', 'Enable one-click purchase for returning customers', true, 30, NOW(), NOW()),
  (gen_random_uuid(), 'crypto_payments', 'Accept cryptocurrency as payment method', false, 0, NOW(), NOW()),

  -- AI/ML Features
  (gen_random_uuid(), 'smart_search', 'ML-enhanced search with typo tolerance', true, 80, NOW(), NOW()),
  (gen_random_uuid(), 'chatbot_support', 'AI chatbot for customer support', false, 0, NOW(), NOW()),

  -- Beta/Experimental
  (gen_random_uuid(), 'experimental_api_v2', 'New API version with GraphQL support', true, 10, NOW(), NOW()),
  (gen_random_uuid(), 'voice_commands', 'Voice-activated navigation and search', false, 0, NOW(), NOW()),

  -- User Tiers
  (gen_random_uuid(), 'early_access', 'Early access to new features for VIP users', true, 15, NOW(), NOW()),
  (gen_random_uuid(), 'unlimited_exports', 'Remove export limits for enterprise users', true, 100, NOW(), NOW());
