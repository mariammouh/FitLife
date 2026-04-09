<?php
include 'db_connect.php';

// 1. VOTRE CLÉ API (Vérifiez qu'elle est bien entre les guillemets sans espaces)
$apiKey = "AIzaSyDAs7YO2wnUbC51-PrTqZfKQ0wJmRhUa-w"; 

// 2. L'URL (Vérifiez bien le modèle gemini-1.5-flash)
$url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" . $apiKey;

// Pour le test direct, on accepte POST ou GET
$email = isset($_POST['email']) ? $_POST['email'] : (isset($_GET['email']) ? $_GET['email'] : null);

if($email){
    $userQuery = $conn->query("SELECT * FROM users WHERE email = '$email'");
    $userData = $userQuery->fetch_assoc();

    if(!$userData) {
        die(json_encode(["status" => "error", "message" => "Utilisateur non trouvé dans la base."]));
    }

    $prompt = "Tu es un coach expert. Analyse : Poids {$userData['weight_kg']}kg, Taille {$userData['height_cm']}cm. Donne une stratégie courte en français.";

    $data = ["contents" => [["parts" => [["text" => $prompt]]]]];

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
    
    // --- LIGNES ESSENTIELLES POUR XAMPP ---
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // Désactive la vérification SSL (Indispensable sur Windows)
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);           // Arrête après 30 secondes si pas de réponse
    // --------------------------------------

    $response = curl_exec($ch);

    // Vérification s'il y a une erreur de connexion (CURL Error)
    if(curl_errno($ch)){
        echo json_encode(["status" => "error", "message" => "CURL Error: " . curl_error($ch)]);
    } else {
        $result = json_decode($response, true);
        if(isset($result['candidates'][0]['content']['parts'][0]['text'])) {
            echo json_encode(["status" => "success", "strategy" => $result['candidates'][0]['content']['parts'][0]['text']]);
        } else {
            // Affiche la réponse brute de Google en cas d'erreur de clé ou de quota
            echo json_encode(["status" => "error", "message" => "Réponse Google vide", "raw" => $result]);
        }
    }
    curl_close($ch);
} else {
    echo json_encode(["status" => "error", "message" => "Email manquant."]);
}
?>