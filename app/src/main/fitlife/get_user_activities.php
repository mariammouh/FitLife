<?php

$conn = new mysqli("127.0.0.1","root","","fitlife",3307);

$user_id = 1;

$sql = "SELECT * FROM sport_activity 
        WHERE user_id=$user_id 
        ORDER BY activity_date DESC";

$result = $conn->query($sql);

$data = array();

while($row = $result->fetch_assoc()){
    $data[] = $row;
}

echo json_encode($data);

$conn->close();
?>
