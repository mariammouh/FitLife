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

    $stmt = $pdo->prepare("
        SELECT user_id, full_name, username, email,
               height_cm, weight_kg, goal_weight_kg,
               goal, fitness_level, gender,
               fitness_state,
               nbr_tries, is_paying, created_at
        FROM users WHERE user_id = ?
    ");
    $stmt->execute([$user_id]);
    $userData = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$userData) {
        echo json_encode(["success" => false, "message" => "User not found"]);
        exit;
    }

    echo json_encode(["success" => true, "user" => $userData]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>
