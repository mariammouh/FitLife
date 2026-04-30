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
        echo json_encode(["success" => false, "message" => "Missing user_id"]);
        exit;
    }

    $stmt = $pdo->prepare("
        SELECT 
            activity_id as id,
            activity_name as name,
            activity_type as type,
            duration_minutes as duration,
            distance_km as distance,
            calories_burned as calories,
            avg_heart_rate as avgHeart,
            max_heart_rate as maxHeart,
            sets as sets,
            reps_per_set as reps,
            weight_used_kg as weight,
            intensity as intensity,
            mood_before as moodBefore,
            mood_after as moodAfter,
            notes as notes,
            location as location,
            activity_date as date,
            start_time as start,
            end_time as end
        FROM sport_activity
        WHERE user_id = ?
        ORDER BY activity_date DESC
    ");

    $stmt->execute([$user_id]);
    $activities = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success"    => true,
        "user_id"    => $user_id,
        "count"      => count($activities),
        "activities" => $activities
    ]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => "DB Error: " . $e->getMessage()]);
}
?>