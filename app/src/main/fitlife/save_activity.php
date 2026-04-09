<?php

print_r($_POST);

$conn = new mysqli("127.0.0.1","root","","fitlife",3307);

if ($conn->connect_error) {
    die("Connection failed");
}

$user_id = 1;

$name = $_POST['name'] ?? null;
$type = $_POST['type'] ?? null;
$duration = $_POST['duration'] ?? null;
$distance = $_POST['distance'] ?? null;
$calories = $_POST['calories'] ?? null;
$date = $_POST['date'] ?? null;
$start = $_POST['start'] ?? null;
$end = $_POST['end'] ?? null;
$location = $_POST['location'] ?? null;
$notes = $_POST['notes'] ?? null;
$intensity = $_POST['intensity'] ?? null;
$avg_heart = $_POST['avg_heart'] ?? null;
$max_heart = $_POST['max_heart'] ?? null;
$sets = $_POST['sets'] ?? null;
$reps = $_POST['reps'] ?? null;
$weight = $_POST['weight'] ?? null;
$mood_before = $_POST['mood_before'] ?? null;
$mood_after = $_POST['mood_after'] ?? null;

$sql = "INSERT INTO sport_activity
(user_id, activity_name, activity_type, duration_minutes, distance_km, calories_burned,
avg_heart_rate, max_heart_rate, sets, reps_per_set, weight_used_kg,
intensity, mood_before, mood_after, notes, location, activity_date, start_time, end_time)

VALUES
('$user_id','$name','$type','$duration','$distance','$calories',
'$avg_heart','$max_heart','$sets','$reps','$weight',
'$intensity','$mood_before','$mood_after','$notes','$location','$date','$start','$end')";

if($conn->query($sql)){
    echo "success";
}else{
    echo "error";
}

$conn->close();

?>