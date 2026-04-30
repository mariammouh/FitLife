<?php
include 'db_connect.php';

if($_SERVER['REQUEST_METHOD']=='POST'){
    $email = $_POST['email'];
    $sql = "SELECT full_name, age, weekly_goal_minutes FROM users WHERE email = '$email'";
    $result = $conn->query($sql);

    if($result->num_rows > 0){
        
        echo json_encode($result->fetch_assoc());
    } else {
        echo json_encode(array("error"=>"not_found"));
    }
}
?>