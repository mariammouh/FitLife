<?php
include 'db_connect.php';
if($_SERVER['REQUEST_METHOD']=='POST'){
    $email = $_POST['email'];
    $password = $_POST['password'];

    $sql = "SELECT * FROM users WHERE email = '$email'";
    $result = $conn->query($sql);

    if($result->num_rows > 0){
        $user = $result->fetch_assoc();
        if(password_verify($password, $user['password_hash'])) echo "success";
        else echo "wrong_password";
    } else {
        echo "not_found"; // هادا هو الميساج لي غايقراه الأندرويد
    }
}
?>