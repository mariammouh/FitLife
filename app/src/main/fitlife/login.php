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

    $email    = isset($_POST['email'])    ? $_POST['email']    : '';
    $password = isset($_POST['password']) ? $_POST['password'] : '';

    if (empty($email) || empty($password)) {
        echo json_encode(["success" => false, "message" => "Email and password required"]);
        exit;
    }

    $stmt = $pdo->prepare("
        SELECT user_id, full_name, username, email, weight_kg, goal_weight_kg, goal, nbr_tries, is_paying
        FROM users
        WHERE email = ?
    ");
    $stmt->execute([$email]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        echo json_encode(["success" => false, "message" => "User not found"]);
        exit;
    }

    // NOTE: In real app use password_verify() — this is mock only
    if ($password !== $user['password_hash']) {
        echo json_encode(["success" => false, "message" => "Wrong password"]);
        exit;
    }

    echo json_encode([
        "success"        => true,
        "user_id"        => $user['user_id'],
        "full_name"      => $user['full_name'],
        "username"       => $user['username'],
        "email"          => $user['email'],
        "weight_kg"      => $user['weight_kg'],
        "goal_weight_kg" => $user['goal_weight_kg'],
        "goal"           => $user['goal'],
        "nbr_tries"      => $user['nbr_tries'],
        "is_paying"      => $user['is_paying']
    ]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>