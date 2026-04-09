<?php
include 'db_connect.php';
if($_SERVER['REQUEST_METHOD']=='POST'){
    $name = $_POST['full_name'];
    $email = $_POST['email'];
    $pass = password_hash($_POST['password'], PASSWORD_DEFAULT);
    $height = $_POST['height'];
    $weight = $_POST['weight'];

    $sql = "INSERT INTO users (full_name, email, password_hash, username, height_cm, weight_kg) 
            VALUES ('$name', '$email', '$pass', '$email', '$height', '$weight')";
    
    if($conn->query($sql) === TRUE) echo "success";
}
?>