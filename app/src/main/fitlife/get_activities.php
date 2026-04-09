<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$host     = "localhost";
$dbname   = "FitLife";
$username = "root";
$password = "";  // XAMPP default is empty

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
            activity_id,
            activity_name,
            activity_type,
            duration_minutes,
            calories_burned,
            avg_heart_rate,
            distance_km,
            intensity,
            mood_after,
            activity_date,
            start_time,
            end_time
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
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>
```

Test it in your browser:
```
http://localhost/fitlife/get_activities.php?user_id=1