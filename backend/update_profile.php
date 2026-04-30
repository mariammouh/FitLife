<?php
include 'db_connect.php';

if($_SERVER['REQUEST_METHOD']=='POST'){
    $email = $_POST['email'];
    $name = $_POST['full_name'];
    $age = $_POST['age'];
    $goal = $_POST['goal'];

    $sql = "UPDATE users SET full_name='$name', age='$age', weekly_goal_minutes='$goal' WHERE email='$email'";
    
    if($conn->query($sql) === TRUE){
        echo "success";
    } else {
        echo "error";
    }
}
?>