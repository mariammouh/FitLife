<?php
error_reporting(E_ALL);
ini_set('display_errors', 0); // This hides the <br> warnings from the app
header('Content-Type: text/plain'); // Since we are using ResponseBody

include 'db_connect.php';

// Debug: If you want to see if $conn exists
if (!isset($conn)) {
    die("Error: Connection variable \$conn is not defined in db_connect.php");
}

if($_SERVER['REQUEST_METHOD']=='POST'){
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';


    $sql = "SELECT * FROM users WHERE email = '$email'";
    $result = $conn->query($sql);

    if($result->num_rows > 0){
        $user = $result->fetch_assoc();
       if(password_verify($password, $user['password_hash'])) {
    echo json_encode([
        "success" => true,
        "user_id" => $user['user_id'] // Make sure 'id' matches your database column
    ]);

            
        } else {
            $response["message"] = "Wrong password";
        }
    } else {
        $response["message"] = "User not found"; 
    }
}


echo json_encode($response);
exit();
?>