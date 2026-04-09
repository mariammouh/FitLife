<?php

$conn = new mysqli("127.0.0.1","root","","fitlife",3307);

$id = $_POST['activity_id'];

$sql = "DELETE FROM sport_activity WHERE activity_id='$id'";

if($conn->query($sql)){
    echo "deleted";
}else{
    echo "error";
}

$conn->close();

?>
