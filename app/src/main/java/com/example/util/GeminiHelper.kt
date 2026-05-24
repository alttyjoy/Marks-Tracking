package com.example.util

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Mark
import com.example.data.model.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiHelper {
    private const val TAG = "GeminiHelper"
    private const val MODEL_NAME = "gemini-3.5-flash"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Generates a study recommendation plan either via Gemini API (if key matches)
     * or a highly realistic local intelligence analyzer fallback.
     */
    suspend fun getStudyPlan(
        studentName: String,
        subjects: List<Subject>,
        marks: List<Mark>
    ): String = withContext(Dispatchers.IO) {
        // Construct detailed data summary
        val markSummary = StringBuilder()
        markSummary.append("Student Name: ").append(studentName).append("\n")
        
        subjects.forEach { sub ->
            val subMarks = marks.filter { it.subjectId == sub.id }
            if (subMarks.isNotEmpty()) {
                markSummary.append("- ").append(sub.name).append(": ")
                val marksStr = subMarks.joinToString(", ") { "${it.examType}: ${it.marksObtained}/${it.maxMarks}" }
                markSummary.append(marksStr).append("\n")
            }
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "No Gemini API key found, generating local recommendation plan.")
            return@withContext getLocalPlanFallback(studentName, subjects, marks)
        }

        val prompt = """
            You are a senior student mentor. Below is the historical academic grades list of student '$studentName' for different exams:
            $markSummary
            
            Based on these marks, provide:
            1. An analytical executive summary (recognizing strengths and pointing out areas needing urgent work).
            2. High-impact improvement strategies specifically for subjects with marks under 40% or downward trends.
            3. A structured, realistic schedule table with weekly focus goals.
            
            Keep the tone supportive, direct, professional and practical. Format with clean Markdown.
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
            
            // Construct nested request body using JSONObject
            val requestBodyJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBodyJson.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini API error: Code ${response.code}, Body: ${response.body?.string()}")
                    return@withContext getLocalPlanFallback(studentName, subjects, marks)
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                        }
                    }
                }
                return@withContext getLocalPlanFallback(studentName, subjects, marks)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini network call: ${e.message}", e)
            return@withContext getLocalPlanFallback(studentName, subjects, marks)
        }
    }

    private fun getLocalPlanFallback(studentName: String, subjects: List<Subject>, marks: List<Mark>): String {
        val lowPerformingSubjects = mutableListOf<String>()
        val downwardTrendSubjects = mutableListOf<String>()
        val strengths = mutableListOf<String>()

        subjects.forEach { sub ->
            val subMarks = marks.filter { it.subjectId == sub.id }.sortedBy { getExamOrder(it.examType) }
            if (subMarks.isNotEmpty()) {
                val averagePercent = subMarks.map { (it.marksObtained / it.maxMarks) * 100.0 }.average()
                
                if (averagePercent >= 75) {
                    strengths.add(sub.name)
                } else if (averagePercent < 40) {
                    lowPerformingSubjects.add("${sub.name} (Avg: ${String.format("%.1f", averagePercent)}%)")
                }

                // Check downward trend > 10%
                if (subMarks.size >= 2) {
                    val firstPercent = (subMarks.first().marksObtained / subMarks.first().maxMarks) * 100.0
                    val lastPercent = (subMarks.last().marksObtained / subMarks.last().maxMarks) * 100.0
                    if (firstPercent - lastPercent > 10.0) {
                        downwardTrendSubjects.add("${sub.name} (Dropped from ${String.format("%.0f", firstPercent)}% to ${String.format("%.0f", lastPercent)}%)")
                    }
                }
            }
        }

        val result = StringBuilder()
        result.append("## AI-Lite Academic Advisory Plan for **$studentName**\n\n")
        result.append("> _Note: Operating on local analytical heuristics engine configured in secondary offline-first isolation._\n\n")
        
        result.append("### 📊 Marks Overview & Strength Mapping\n")
        if (strengths.isNotEmpty()) {
            result.append("🏆 **Excellent Subject Proficiency:** ${strengths.joinToString(", ")}. Keep up the spectacular efforts! Consistent practices in these fields establish sound cognitive structures.\n\n")
        } else {
            result.append("📈 Continue tracking and recording additional exam cycles to formulate precise strength vectors.\n\n")
        }

        if (lowPerformingSubjects.isNotEmpty() || downwardTrendSubjects.isNotEmpty()) {
            result.append("### 🚨 Urgent Attention Points (Risk Flags)\n")
            if (lowPerformingSubjects.isNotEmpty()) {
                result.append("⚠️ **Critical Low Scores (<40%):** We flagged the following subjects needing immediate conceptual review:\n")
                lowPerformingSubjects.forEach { result.append("- **$it**\n") }
                result.append("\n")
            }
            if (downwardTrendSubjects.isNotEmpty()) {
                result.append("📉 **Downward Trend Highlight (>10% dip):** We observed a critical drop in consecutive exams for:\n")
                downwardTrendSubjects.forEach { result.append("- **$it**\n") }
                result.append("\n")
            }
            
            result.append("### 🧠 Actionable Improvement Guide\n")
            result.append("1. **Conceptual Sandboxing:** Set aside 45 minutes daily to review fundamental definitions in hard subjects rather than attempting test sheets directly.\n")
            result.append("2. **Inverted Learning:** Try explaining the topic aloud to a parent or peer. This reinforces intellectual recall mechanisms.\n")
            result.append("3. **Incremental Progress Checks:** Schedule short 15-minute weekly drills. Aim to improve by just 5% in each subsequent check.\n\n")
            
            result.append("### 📅 Focus Study Schedule Layout\n")
            result.append("| Subject Priority | Daily Duration | Primary Target Method |\n")
            result.append("| :--- | :--- | :--- |\n")
            lowPerformingSubjects.forEach { sub ->
                val nameOnly = sub.substringBefore(" (")
                result.append("| **$nameOnly** | 1.0 Hour | Active Recall, Formula Deconstruction |\n")
            }
            downwardTrendSubjects.forEach { sub ->
                val nameOnly = sub.substringBefore(" (")
                if (!lowPerformingSubjects.any { it.startsWith(nameOnly) }) {
                    result.append("| **$nameOnly** | 45 Mins | Practice Exam Spacing, Error Journal Analysis |\n")
                }
            }
        } else {
            result.append("### 🟢 Clean Status Report\n")
            result.append("All subjects are performing inside comfortable threshold barriers. Maintain standard periodic drill timings and continue utilizing the Marks Entry tracker to sustain performance vectors.")
        }
        
        return result.toString()
    }

    private fun getExamOrder(examType: String): Int {
        return when (examType) {
            "Weekly" -> 1
            "Monthly" -> 2
            "Quarterly" -> 3
            "Half-Yearly" -> 4
            "Annual" -> 5
            else -> 6
        }
    }
}
