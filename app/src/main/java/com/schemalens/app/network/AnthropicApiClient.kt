package com.schemalens.app.network

import com.schemalens.app.data.AssessmentResult
import com.schemalens.app.data.CallSite
import com.schemalens.app.data.RiskSeverity
import com.schemalens.app.data.SiteVerdict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Anthropic API Client for Green Light Zone Deep Risk Assessment.
 *
 * Calls Claude Sonnet 4.6 with the exact minimal payload:
 * only the package name, change notes, and flagged call sites.
 */
class AnthropicApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Assesses migration risk for the given call sites and change notes.
     */
    suspend fun assessRisk(
        apiKey: String,
        packageName: String,
        changeNotes: String,
        callSites: List<CallSite>
    ): Result<AssessmentResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            return@withContext Result.failure(
                IllegalArgumentException("Anthropic API key is not configured. Set ANTHROPIC_API_KEY in local.properties or provide an API key in the UI settings.")
            )
        }

        if (callSites.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("No call sites provided for assessment.")
            )
        }

        val siteListFormatted = buildString {
            callSites.forEach { site ->
                appendLine("[#${site.index}] ${site.file ?: "unknown"}:${site.lineNumber} -> ${site.lineText}")
            }
        }

        val userPrompt = """
            You are assessing upgrade/migration risk for "$packageName".

            What is changing:
            $changeNotes

            Call sites:
            $siteListFormatted

            For EACH call site (by index), decide "safe", "risky", or "breaking". Respond ONLY with JSON, no prose, no markdown fences: {"overall_score": <0-100>, "summary": "<one sentence>", "sites": [{"index": <int>, "sev": "green"|"amber"|"red", "note": "<1 sentence>"}], "orm_patch": "<a short, corrected/optimized ORM model or schema snippet reflecting the change, plain text>"}
        """.trimIndent()

        val jsonPayload = JSONObject().apply {
            put("model", "claude-sonnet-4-6")
            put("max_tokens", 1400)
            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }
            put("messages", messagesArray)
        }

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("Content-Type", "application/json")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(jsonPayload.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorDetail = try {
                    val errorJson = JSONObject(responseBody)
                    errorJson.optJSONObject("error")?.optString("message") ?: responseBody
                } catch (_: Exception) {
                    responseBody.ifBlank { "HTTP Error ${response.code}" }
                }
                return@withContext Result.failure(
                    IOException("Claude API Error (${response.code}): $errorDetail")
                )
            }

            // Parse response body
            val responseJson = JSONObject(responseBody)
            val contentArray = responseJson.optJSONArray("content")
            val rawText = contentArray?.optJSONObject(0)?.optString("text") ?: ""

            val cleanedJsonText = stripMarkdownFences(rawText)
            val result = parseAssessmentJson(cleanedJsonText, callSites.size)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Strips ```json ... ``` or ``` ... ``` markdown fences from model responses.
     */
    fun stripMarkdownFences(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        return text.trim()
    }

    /**
     * Parses the LLM's assessment JSON into structured AssessmentResult.
     */
    fun parseAssessmentJson(jsonText: String, expectedSitesCount: Int): AssessmentResult {
        val root = JSONObject(jsonText)
        val overallScore = root.optInt("overall_score", 50)
        val summary = root.optString("summary", "Migration impact assessed.")
        val ormPatch = root.optString("orm_patch", "// No patch generated")

        val sitesList = mutableListOf<SiteVerdict>()
        val sitesArray = root.optJSONArray("sites")

        if (sitesArray != null) {
            for (i in 0 until sitesArray.length()) {
                val item = sitesArray.optJSONObject(i) ?: continue
                val index = item.optInt("index", i)
                val sevCode = item.optString("sev", "grey")
                val note = item.optString("note", "")
                sitesList.add(
                    SiteVerdict(
                        index = index,
                        sev = RiskSeverity.fromCode(sevCode),
                        note = note
                    )
                )
            }
        }

        // Fill in missing site verdicts if any were omitted
        val indexedMap = sitesList.associateBy { it.index }
        val completeSites = (0 until expectedSitesCount).map { idx ->
            indexedMap[idx] ?: SiteVerdict(
                index = idx,
                sev = RiskSeverity.RISKY,
                note = "Requires review for schema compatibility."
            )
        }

        return AssessmentResult(
            overallScore = overallScore.coerceIn(0, 100),
            summary = summary,
            sites = completeSites,
            ormPatch = ormPatch
        )
    }
}
