-- À exécuter dans slice_db après le test de charge :
--   docker compose exec slice-db psql -U candronex -d slice_db -f /dev/stdin < performance/p95-activation.sql
SELECT
  count(*)                                                                          AS services_actifs,
  percentile_cont(0.95) WITHIN GROUP (ORDER BY EXTRACT(EPOCH FROM (activated_at - accepted_at))) AS p95_secondes,
  avg(EXTRACT(EPOCH FROM (activated_at - accepted_at)))                             AS moyenne_secondes,
  max(EXTRACT(EPOCH FROM (activated_at - accepted_at)))                             AS max_secondes
FROM service
WHERE state = 'ACTIVE';
