<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$host     = "localhost";
$dbname   = "fitlife";
$username = "root";
$password = "";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;
    if ($user_id === 0) {
        echo json_encode(["success" => false, "message" => "user_id required"]);
        exit;
    }

    // 1. TODAY'S SUMMARY
    $today = $pdo->prepare("SELECT COALESCE(SUM(duration_minutes), 0) AS total_minutes, COALESCE(SUM(calories_burned), 0) AS total_calories FROM sport_activity WHERE user_id = ? AND DATE(activity_date) = CURDATE()");
    $today->execute([$user_id]);
    $todayRes = $today->fetch(PDO::FETCH_ASSOC);

    // 2. LIFETIME SESSIONS
    $lifetime = $pdo->prepare("SELECT COUNT(*) AS total_activities FROM sport_activity WHERE user_id = ?");
    $lifetime->execute([$user_id]);
    $countRes = $lifetime->fetch(PDO::FETCH_ASSOC);

    $todayStats = [
        "total_minutes"    => (int)$todayRes['total_minutes'],
        "total_calories"   => (int)$todayRes['total_calories'],
        "total_activities" => (int)$countRes['total_activities']
    ];

    // 3. MONTHLY CALORIES
    $monthly = $pdo->prepare("SELECT DATE(activity_date) AS day, SUM(calories_burned) AS calories FROM sport_activity WHERE user_id = ? AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY DATE(activity_date) ORDER BY day ASC");
    $monthly->execute([$user_id]);
    $monthlyCalories = $monthly->fetchAll(PDO::FETCH_ASSOC);

    // 4. ACTIVITY TYPES
    $types = $pdo->prepare("SELECT LOWER(activity_type) as activity_type, COUNT(*) AS count FROM sport_activity WHERE user_id = ? GROUP BY LOWER(activity_type)");
    $types->execute([$user_id]);
    $activityTypes = $types->fetchAll(PDO::FETCH_ASSOC);

    // 5. MOOD DATA
    $mood = $pdo->prepare("SELECT LOWER(activity_type) as activity_type, LOWER(mood_after) as mood_after, COUNT(*) as count FROM sport_activity WHERE user_id = ? AND mood_after IS NOT NULL AND mood_after != '' GROUP BY LOWER(activity_type), LOWER(mood_after)");
    $mood->execute([$user_id]);
    $moodData = $mood->fetchAll(PDO::FETCH_ASSOC);

    // 6. AVERAGE KCAL BY TYPE
    $avg = $pdo->prepare("SELECT LOWER(activity_type) as activity_type, ROUND(AVG(calories_burned), 1) as avg_calories FROM sport_activity WHERE user_id = ? GROUP BY LOWER(activity_type)");
    $avg->execute([$user_id]);
    $avgByType = $avg->fetchAll(PDO::FETCH_ASSOC);

    // 7. STREAK
    $streak = $pdo->prepare("SELECT COUNT(DISTINCT DATE(activity_date)) AS active_days FROM sport_activity WHERE user_id = ? AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)");
    $streak->execute([$user_id]);
    $streakRes = $streak->fetch(PDO::FETCH_ASSOC);

    // 8. BEST WORKOUT
    $best = $pdo->prepare("SELECT activity_name, calories_burned, duration_minutes, activity_date FROM sport_activity WHERE user_id = ? ORDER BY calories_burned DESC LIMIT 1");
    $best->execute([$user_id]);
    $bestWorkout = $best->fetch(PDO::FETCH_ASSOC);

    echo json_encode([
        "success"          => true,
        "today"            => $todayStats,
        "monthly_calories" => $monthlyCalories,
        "activity_types"   => $activityTypes,
        "mood_data"        => $moodData,
        "streak"           => ["active_days" => (int)$streakRes['active_days']],
        "best_workout"     => $bestWorkout ?: null,
        "week_comparison"  => ["this_week" => (int)$todayRes['total_calories'], "last_week" => 0],
        "avg_by_type"      => $avgByType
    ]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>