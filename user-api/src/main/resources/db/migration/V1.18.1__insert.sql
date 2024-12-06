-- 학습 내역을 기반으로 총 학습 횟수와 완료된 학습 횟수 초기화
INSERT INTO `category_learning_progress` (user_id, category_id, total_learning_count, completed_learning_count)
SELECT lh.user_id,
       c.category_id,
       COUNT(*)                                                AS total_learning_count,
       SUM(CASE WHEN lh.learning_rate >= 80 THEN 1 ELSE 0 END) AS completed_learning_count
FROM `learning_history` lh
         JOIN `content` c ON lh.content_id = c.id
GROUP BY lh.user_id, c.category_id
ON DUPLICATE KEY UPDATE total_learning_count     = VALUES(total_learning_count),
                        completed_learning_count = VALUES(completed_learning_count);
