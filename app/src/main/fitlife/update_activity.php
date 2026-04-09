<?php
$conn = new mysqli("127.0.0.1","root","","fitlife",3307);

if ($conn->connect_error) {
    die("error: " . $conn->connect_error);
}

$activity_id = $_POST['activity_id'] ?? '';
$name = $_POST['name'] ?? '';
$type = $_POST['type'] ?? '';
$duration = $_POST['duration'] ?? '';
$distance = $_POST['distance'] ?? '';
$calories = $_POST['calories'] ?? '';
$avg_heart = $_POST['avg_heart'] ?? '';
$max_heart = $_POST['max_heart'] ?? '';
$sets = $_POST['sets'] ?? '';
$reps = $_POST['reps'] ?? '';
$weight = $_POST['weight'] ?? '';
$intensity = $_POST['intensity'] ?? '';
$mood_before = $_POST['mood_before'] ?? '';
$mood_after = $_POST['mood_after'] ?? '';
$notes = $_POST['notes'] ?? '';
$location = $_POST['location'] ?? '';
$date = $_POST['date'] ?? '';
$start = $_POST['start'] ?? '';
$end = $_POST['end'] ?? '';

if ($activity_id == '') {
    echo "error: missing activity_id";
    exit();
}

$sql = "UPDATE sport_activity SET
    activity_name='$name',
    activity_type='$type',
    duration_minutes='$duration',
    distance_km='$distance',
    calories_burned='$calories',
    avg_heart_rate='$avg_heart',
    max_heart_rate='$max_heart',
    sets='$sets',
    reps_per_set='$reps',
    weight_used_kg='$weight',
    intensity='$intensity',
    mood_before='$mood_before',
    mood_after='$mood_after',
    notes='$notes',
    location='$location',
    activity_date='$date',
    start_time='$start',
    end_time='$end'
    WHERE activity_id='$activity_id'";

if ($conn->query($sql) === TRUE) {
    echo "success";
} else {
    echo "error: " . $conn->error;
}

$conn->close();
?>