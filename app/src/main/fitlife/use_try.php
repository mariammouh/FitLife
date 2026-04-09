<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");

$host   = "localhost";
$dbname = "FitLife";
$user   = "root";
$pass   = "";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $data    = json_decode(file_get_contents("php://input"), true);
    $user_id = isset($data['user_id']) ? intval($data['user_id']) : 0;

    if ($user_id === 0) {
        echo json_encode(["success" => false, "message" => "user_id required"]);
        exit;
    }

    // Only decrement if user is NOT paying AND has tries left
    $check = $pdo->prepare("SELECT nbr_tries, is_paying FROM users WHERE user_id = ?");
    $check->execute([$user_id]);
    $row = $check->fetch(PDO::FETCH_ASSOC);

    if (!$row) {
        echo json_encode(["success" => false, "message" => "User not found"]);
        exit;
    }

    // Paying users — no decrement
    if ($row['is_paying']) {
        echo json_encode([
            "success"   => true,
            "nbr_tries" => $row['nbr_tries'],
            "is_paying" => true,
            "locked"    => false
        ]);
        exit;
    }

    // Already at 0 — locked
    if ($row['nbr_tries'] <= 0) {
        echo json_encode([
            "success"   => true,
            "nbr_tries" => 0,
            "is_paying" => false,
            "locked"    => true
        ]);
        exit;
    }

    // Decrement by 1
    $update = $pdo->prepare("UPDATE users SET nbr_tries = nbr_tries - 1 WHERE user_id = ?");
    $update->execute([$user_id]);

    $newTries = $row['nbr_tries'] - 1;

    echo json_encode([
        "success"   => true,
        "nbr_tries" => $newTries,
        "is_paying" => false,
        "locked"    => $newTries <= 0
    ]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>
