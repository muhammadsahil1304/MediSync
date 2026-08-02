package com.example.newmedisync.firebase

import com.example.newmedisync.utils.Constants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constants.GEMINI_API_KEY
    )

    suspend fun getClinicalAdvice(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "No response from AI"
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    suspend fun analyzeHealthRisk(patientData: String): String = withContext(Dispatchers.IO) {
        val prompt = "Based on the following patient data, provide a percentage risk for Cardiovascular Disease, Diabetes, and Kidney Disease. Format the output as JSON with keys 'heart', 'diabetes', and 'kidney' as integers between 0 and 100. Patient Data: $patientData"
        try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    suspend fun getHealthScore(patientData: String): String = withContext(Dispatchers.IO) {
        val prompt = "Analyze the following patient data and provide a health score from 0 to 100 and a status (Poor, Fair, Good, Excellent). Format: Score, Status. Patient Data: $patientData"
        try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun getPrescriptionSuggestions(patientData: String): String = withContext(Dispatchers.IO) {
        val prompt = "Based on the following patient data (allergies, diseases, current medications), suggest 3 medical advice points or lifestyle changes. Keep it professional and concise. Patient Data: $patientData"
        try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
