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

    $allowed = ['username', 'goal_weight_kg', 'goal', 'fitness_level'];
    $sets    = [];
    $values  = [];

    foreach ($allowed as $field) {
        if (isset($data[$field]) && $data[$field] !== '') {
            $sets[]   = "$field = ?";
            $values[] = $data[$field];
        }
    }

    if (empty($sets)) {
        echo json_encode(["success" => false, "message" => "Nothing to update"]);
        exit;
    }

    $values[] = $user_id;
    $sql = "UPDATE users SET " . implode(", ", $sets) . " WHERE user_id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute($values);

    echo json_encode(["success" => true, "message" => "Updated successfully"]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>
