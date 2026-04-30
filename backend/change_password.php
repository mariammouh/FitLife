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

    $user_id  = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
    $new_pass = isset($_POST['new_password']) ? $_POST['new_password'] : '';

    if ($user_id === 0 || empty($new_pass)) {
        echo json_encode(["success" => false, "message" => "user_id and new_password required"]);
        exit;
    }

    $pass_hash = password_hash($new_pass, PASSWORD_DEFAULT);

    $sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$pass_hash, $user_id]);

    echo json_encode(["success" => true, "message" => "Password changed successfully"]);

} catch (PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>