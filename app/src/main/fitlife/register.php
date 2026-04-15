<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");

$host     = "localhost";
$dbname   = "FitLife";
$username = "root";
$password = "";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $full_name      = $_POST['full_name']      ?? '';
    $user_name      = $_POST['username']       ?? '';
    $email          = $_POST['email']          ?? '';
    $pass           = $_POST['password']       ?? '';
    $phone          = $_POST['phone']          ?? '';
    $gender         = $_POST['gender']         ?? '';
    $dob            = $_POST['dob']            ?? '';
    $height         = $_POST['height']         ?? '';
    $start_weight   = $_POST['start_weight']   ?? '';
    $current_weight = $_POST['current_weight'] ?? '';
    $goal_weight    = $_POST['goal_weight']    ?? '';
    $goal           = $_POST['goal']           ?? '';
    $fitness_level  = $_POST['fitness_level']  ?? '';

    if (empty($email) || empty($pass) || empty($full_name)) {
        echo json_encode(["success" => false, "message" => "Required fields missing"]);
        exit;
    }

    // Check if email exists
    $stmt = $pdo->prepare("SELECT user_id FROM users WHERE email = ?");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        echo json_encode(["success" => false, "message" => "Email already registered"]);
        exit;
    }

    $pass_hash = password_hash($pass, PASSWORD_DEFAULT);

    $sql = "INSERT INTO users (
                full_name, username, email, password_hash, phone, gender,
                date_of_birth, height_cm, start_weight_kg, current_weight_kg,
                goal_weight_kg, goal, fitness_level
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        $full_name, $user_name, $email, $pass_hash, $phone, $gender,
        $dob, $height, $start_weight, $current_weight,
        $goal_weight, $goal, $fitness_level
    ]);

    echo json_encode(["success" => true, "message" => "User registered successfully"]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>