<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$host   = "localhost";
$dbname = "FitLife";
$user   = "root";
$pass   = "";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;
    if ($user_id === 0) {
        echo json_encode(["success" => false, "message" => "user_id required"]);
        exit;
    }

    // ── 1. TODAY'S STATS ──
    $today = $pdo->prepare("
        SELECT
            COALESCE(SUM(duration_minutes), 0)  AS total_minutes,
            COALESCE(SUM(calories_burned), 0)   AS total_calories,
            COUNT(*)                             AS total_activities
        FROM sport_activity
        WHERE user_id = ? AND DATE(activity_date) = CURDATE()
    ");
    $today->execute([$user_id]);
    $todayStats = $today->fetch(PDO::FETCH_ASSOC);

    // ── 2. MONTHLY CALORIES (last 30 days, grouped by day) ──
    $monthly = $pdo->prepare("
        SELECT
            DATE(activity_date)            AS day,
            SUM(calories_burned)           AS calories
        FROM sport_activity
        WHERE user_id = ?
          AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        GROUP BY DATE(activity_date)
        ORDER BY day ASC
    ");
    $monthly->execute([$user_id]);
    $monthlyCalories = $monthly->fetchAll(PDO::FETCH_ASSOC);

    // ── 3. ACTIVITY TYPE DISTRIBUTION ──
    $types = $pdo->prepare("
        SELECT
            activity_type,
            COUNT(*) AS count
        FROM sport_activity
        WHERE user_id = ?
          AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        GROUP BY activity_type
        ORDER BY count DESC
    ");
    $types->execute([$user_id]);
    $activityTypes = $types->fetchAll(PDO::FETCH_ASSOC);

    // ── 4. MOOD AFTER WORKOUT BY TYPE ──
    $mood = $pdo->prepare("
        SELECT
            activity_type,
            mood_after,
            COUNT(*) AS count
        FROM sport_activity
        WHERE user_id = ?
          AND mood_after IS NOT NULL
          AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        GROUP BY activity_type, mood_after
        ORDER BY activity_type, count DESC
    ");
    $mood->execute([$user_id]);
    $moodData = $mood->fetchAll(PDO::FETCH_ASSOC);

    // ── 5. WEEKLY WORKOUT STREAK ──
    $streak = $pdo->prepare("
        SELECT COUNT(DISTINCT DATE(activity_date)) AS active_days
        FROM sport_activity
        WHERE user_id = ?
          AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
    ");
    $streak->execute([$user_id]);
    $streakData = $streak->fetch(PDO::FETCH_ASSOC);

    // ── 6. BEST WORKOUT THIS MONTH ──
    $best = $pdo->prepare("
        SELECT activity_name, calories_burned, duration_minutes, activity_date
        FROM sport_activity
        WHERE user_id = ?
          AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        ORDER BY calories_burned DESC
        LIMIT 1
    ");
    $best->execute([$user_id]);
    $bestWorkout = $best->fetch(PDO::FETCH_ASSOC);

    // ── 7. CALORIES BURNED THIS WEEK VS LAST WEEK ──
    $weekComp = $pdo->prepare("
        SELECT
            SUM(CASE WHEN activity_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
                     THEN calories_burned ELSE 0 END) AS this_week,
            SUM(CASE WHEN activity_date >= DATE_SUB(CURDATE(), INTERVAL 14 DAY)
                      AND activity_date < DATE_SUB(CURDATE(), INTERVAL 7 DAY)
                     THEN calories_burned ELSE 0 END) AS last_week
        FROM sport_activity
        WHERE user_id = ?
    ");
    $weekComp->execute([$user_id]);
    $weekComparison = $weekComp->fetch(PDO::FETCH_ASSOC);

    // ── 8. AVERAGE WORKOUT DURATION BY TYPE ──
    $avgDuration = $pdo->prepare("
        SELECT
            activity_type,
            ROUND(AVG(duration_minutes), 1) AS avg_duration,
            ROUND(AVG(calories_burned), 0)  AS avg_calories
        FROM sport_activity
        WHERE user_id = ?
          AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        GROUP BY activity_type
    ");
    $avgDuration->execute([$user_id]);
    $avgByType = $avgDuration->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success"          => true,
        "today"            => $todayStats,
        "monthly_calories" => $monthlyCalories,
        "activity_types"   => $activityTypes,
        "mood_data"        => $moodData,
        "streak"           => $streakData,
        "best_workout"     => $bestWorkout ?: null,
        "week_comparison"  => $weekComparison,
        "avg_by_type"      => $avgByType
    ]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>
