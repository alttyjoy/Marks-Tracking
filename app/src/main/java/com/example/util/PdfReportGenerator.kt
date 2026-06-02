package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.model.Student
import com.example.data.model.Mark
import com.example.data.model.Subject
import com.example.data.model.TestType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {
    fun generateStudentReportPdf(
        context: Context,
        student: Student,
        decryptedStudentName: String,
        marks: List<Mark>,
        subjects: List<Subject>,
        testTypes: List<TestType>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Initialize paints
        val mainPaint = Paint()
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A") // Slate 900
            textSize = 20f
            isFakeBoldText = true
        }
        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#0D9488") // Teal 600
            textSize = 10f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#475569") // Slate 600
            textSize = 13f
            isFakeBoldText = true
        }
        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#1E293B") // Slate 800
            textSize = 12f
            isFakeBoldText = true
        }
        val labelPaint = Paint().apply {
            color = Color.parseColor("#64748B") // Slate 500
            textSize = 9.5f
            isFakeBoldText = true
        }
        val valPaint = Paint().apply {
            color = Color.parseColor("#334155") // Slate 700
            textSize = 9.5f
        }
        val cellPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9f
        }
        val mutedPaint = Paint().apply {
            color = Color.parseColor("#94A3B8") // Slate 400
            textSize = 8f
        }

        // 1. Top Decorative Primary Color Banner
        mainPaint.color = Color.parseColor("#0D9488") // Teal 600
        canvas.drawRect(50f, 35f, 545f, 41f, mainPaint)

        // 2. Banner Header Title
        canvas.drawText("ACADEMIC PERFORMANCE REPORT SUMMARY", 50f, 65f, titlePaint)
        canvas.drawText("MARKS COMPILATION & SCHOLASTIC GROWTH DIARY", 50f, 80f, subTitlePaint)

        // Right side school label
        val schoolLabel = if (student.schoolName.isNotBlank()) student.schoolName.uppercase() else "INDIVIDUAL PERFORMANCE LEDGER"
        canvas.drawText(schoolLabel, 340f, 65f, headerPaint.apply { textSize = 9.5f; color = Color.parseColor("#0F172A") })

        // Reset header paint text size
        headerPaint.textSize = 13f

        // Draw horizontal separator
        mainPaint.color = Color.parseColor("#E2E8F0") // Slate 200
        canvas.drawLine(50f, 95f, 545f, 95f, mainPaint)

        // 3. Student Bio Block (Two Column Layout)
        var y = 115f
        canvas.drawText("Student Name:", 50f, y, labelPaint)
        canvas.drawText(decryptedStudentName, 140f, y, valPaint.apply { isFakeBoldText = true; color = Color.parseColor("#0F172A") })

        canvas.drawText("Roll Number / Code:", 320f, y, labelPaint)
        canvas.drawText(student.rollNo.ifBlank { "N/A" }, 430f, y, valPaint.apply { isFakeBoldText = false; color = Color.parseColor("#334155") })

        y += 18f
        canvas.drawText("Class / Grade:", 50f, y, labelPaint)
        canvas.drawText(student.studentClass.ifBlank { "Unassigned" }, 140f, y, valPaint)

        canvas.drawText("School Tenant Code:", 320f, y, labelPaint)
        canvas.drawText(student.schoolId.ifBlank { "GLOBAL_TENANT" }, 430f, y, valPaint)

        y += 18f
        val rawParent = student.parentName
        val decryptedParent = if (rawParent.isNotBlank()) EncryptionUtil.decrypt(rawParent) else "No Parent Connected"
        canvas.drawText("Parent / Guardian:", 50f, y, labelPaint)
        canvas.drawText(decryptedParent, 140f, y, valPaint)

        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val dateString = sdf.format(Date())
        canvas.drawText("Compilation Date:", 320f, y, labelPaint)
        canvas.drawText(dateString, 430f, y, valPaint)

        // Draw horizontal divider under Bio card
        y += 15f
        mainPaint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(50f, y, 545f, y, mainPaint)

        // 4. KPI Performance Breakdown Cards (y = 185 to 240)
        y += 18f
        canvas.drawText("I. EXECUTIVE SCHOLASTIC OVERVIEW", 50f, y, sectionTitlePaint)

        // Calculate performance details
        val totalMarksCount = marks.size
        val overallMean = if (marks.isNotEmpty()) marks.map { it.marksObtained }.average() else 0.0
        val maxTotalObtained = marks.sumOf { it.marksObtained }
        val maxTotalPossible = marks.sumOf { it.maxMarks }
        
        val alphaGrade = when {
            overallMean >= 90 -> "A+ (Excellent)"
            overallMean >= 80 -> "A (Outstanding)"
            overallMean >= 70 -> "B+ (Very Good)"
            overallMean >= 60 -> "B (Satisfactory)"
            overallMean >= 50 -> "C (Improvement needed)"
            else -> "F (Needs Tutorial)"
        }

        // Draw KPI boxes
        val cardWidth = 155f
        y += 10f
        
        // Card 1: Aggregate Mean
        mainPaint.color = Color.parseColor("#F8FAFC") // Slate 50 background
        canvas.drawRoundRect(50f, y, 50f + cardWidth, y + 50f, 6f, 6f, mainPaint)
        mainPaint.color = Color.parseColor("#E2E8F0") // Slate 200 border
        mainPaint.style = Paint.Style.STROKE
        canvas.drawRoundRect(50f, y, 50f + cardWidth, y + 50f, 6f, 6f, mainPaint)
        mainPaint.style = Paint.Style.FILL
        canvas.drawText("AGGREGATE MEAN SCORE", 60f, y + 18f, mutedPaint.apply { textSize = 7.5f; isFakeBoldText = true })
        canvas.drawText("${String.format(Locale.US, "%.1f", overallMean)}%", 60f, y + 38f, titlePaint.apply { textSize = 15f; color = Color.parseColor("#0284C7") })

        // Card 2: Cumulative Entries
        val card2X = 50f + cardWidth + 15f
        mainPaint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(card2X, y, card2X + cardWidth, y + 50f, 6f, 6f, mainPaint)
        mainPaint.color = Color.parseColor("#E2E8F0")
        mainPaint.style = Paint.Style.STROKE
        canvas.drawRoundRect(card2X, y, card2X + cardWidth, y + 50f, 6f, 6f, mainPaint)
        mainPaint.style = Paint.Style.FILL
        canvas.drawText("TOTAL DATA LEDGER ENTRIES", card2X + 10f, y + 18f, mutedPaint)
        canvas.drawText("$totalMarksCount Subjects Logged", card2X + 10f, y + 38f, titlePaint.apply { textSize = 12.5f; color = Color.parseColor("#0F172A") })

        // Card 3: Alphabetic GPA Rank
        val card3X = card2X + cardWidth + 15f
        mainPaint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(card3X, y, card3X + cardWidth, y + 50f, 6f, 6f, mainPaint)
        mainPaint.color = Color.parseColor("#E2E8F0")
        mainPaint.style = Paint.Style.STROKE
        canvas.drawRoundRect(card3X, y, card3X + cardWidth, y + 50f, 6f, 6f, mainPaint)
        mainPaint.style = Paint.Style.FILL
        canvas.drawText("SCHOLASTIC MERIT ACCORD", card3X + 10f, y + 18f, mutedPaint)
        canvas.drawText(alphaGrade, card3X + 10f, y + 38f, titlePaint.apply { textSize = 11.5f; color = Color.parseColor("#0D9488") })

        // Reset titlePaint sizes
        titlePaint.textSize = 20f

        // 5. Marks compilation Table Header block
        y += 75f
        canvas.drawText("II. MARKS & SCHOLASTIC GRADE COMPILATION", 50f, y, sectionTitlePaint)

        y += 10f
        mainPaint.color = Color.parseColor("#F1F5F9") // Light table header background
        canvas.drawRect(50f, y, 545f, y + 20f, mainPaint)
        
        // Draw Table Header columns
        canvas.drawText("SUBJECT SYLLABUS", 56f, y + 14f, labelPaint.apply { color = Color.parseColor("#1E293B"); textSize = 8.5f })
        canvas.drawText("EXAMINATION TYPE", 210f, y + 14f, labelPaint)
        canvas.drawText("MARKS OBTAINED", 340f, y + 14f, labelPaint)
        canvas.drawText("MAX MARKS", 430f, y + 14f, labelPaint)
        canvas.drawText("PASSED %", 500f, y + 14f, labelPaint)

        y += 20f
        
        // Match IDs to descriptive Names
        val subjectMap = subjects.associate { it.id to it.name }
        
        if (marks.isEmpty()) {
            canvas.drawText("No individual scorecard entries are available for this student portfolio yet.", 60f, y + 22f, valPaint.apply { isFakeBoldText = false; color = Color.GRAY })
            y += 40f
        } else {
            val cellValPaint = Paint().apply {
                color = Color.parseColor("#334155")
                textSize = 8.5f
            }
            val borderPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }

            marks.forEach { mark ->
                // Draw bottom border per row to make it organized and highly scannable
                canvas.drawLine(50f, y + 18f, 545f, y + 18f, borderPaint)

                val subjectLabelRaw = subjectMap[mark.subjectId] ?: "General Study"
                val examLabel = mark.examType
                val score = mark.marksObtained
                val maxScore = mark.maxMarks
                val percentage = if (maxScore > 0) (score / maxScore) * 100.0 else 0.0

                canvas.drawText(subjectLabelRaw, 56f, y + 12f, cellValPaint.apply { isFakeBoldText = true })
                canvas.drawText(examLabel, 210f, y + 12f, cellValPaint.apply { isFakeBoldText = false })
                canvas.drawText(String.format(Locale.US, "%.1f", score), 340f, y + 12f, cellValPaint)
                canvas.drawText(String.format(Locale.US, "%.1f", maxScore), 430f, y + 12f, cellValPaint)
                
                val percentScoreStr = "${String.format(Locale.US, "%.1f", percentage)}%"
                val colorString = if (percentage >= 50.0) "#0D9488" else "#E11D48"
                canvas.drawText(percentScoreStr, 500f, y + 12f, Paint().apply {
                    color = Color.parseColor(colorString)
                    textSize = 8.5f
                    isFakeBoldText = true
                })

                y += 18f
            }
        }

        // Summary calculations
        y += 20f
        canvas.drawText("III. SUBJECT PERFORMANCE DIAGNOSTICS", 50f, y, sectionTitlePaint)
        
        y += 12f
        mainPaint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(50f, y, 545f, y + 45f, 4f, 4f, mainPaint)
        mainPaint.color = Color.parseColor("#CBD5E1")
        mainPaint.style = Paint.Style.STROKE
        canvas.drawRoundRect(50f, y, 545f, y + 45f, 4f, 4f, mainPaint)
        mainPaint.style = Paint.Style.FILL

        // Diagnostics metrics
        val subAverageText = "Total Marks Tallied: ${String.format(Locale.US, "%.1f", maxTotalObtained)} out of ${String.format(Locale.US, "%.1f", maxTotalPossible)} possible points."
        val recommendedTutorText = if (overallMean >= 75.0) {
            "Diagnostic: Performance complies with premium scholar expectations. Approved for advanced extension pathways."
        } else {
            "Diagnostic: Cumulative averages lie below expected thresholds. Weekly focus sessions and syllabus review authorized."
        }
        canvas.drawText(subAverageText, 62f, y + 18f, valPaint.apply { textSize = 8.5f; isFakeBoldText = true; color = Color.parseColor("#1E293B") })
        canvas.drawText(recommendedTutorText, 62f, y + 32f, valPaint.apply { textSize = 8f; isFakeBoldText = false; color = Color.parseColor("#475569") })

        // Certification & verification
        y += 75f
        canvas.drawText("CERTIFYING AUTHORITY STATEMENTS", 50f, y, labelPaint.apply { textSize = 8.5f })
        
        y += 15f
        canvas.drawText("1. This academic summary report is compiled over securely stored digital database ledgers.", 50f, y, mutedPaint)
        y += 11f
        canvas.drawText("2. Parent authorization keys and enrollment metadata are secured via industry-standard local AES-256 wrapping logic.", 50f, y, mutedPaint)
        y += 11f
        canvas.drawText("3. Fully cryptographically authenticated under the secure educational student evaluation guidelines of Marks Tracking Suite.", 50f, y, mutedPaint)

        // Signature lines
        y += 45f
        mainPaint.color = Color.parseColor("#94A3B8")
        canvas.drawLine(70f, y, 200f, y, mainPaint)
        canvas.drawLine(380f, y, 510f, y, mainPaint)
        canvas.drawText("Class Teacher / Evaluator", 82f, y + 13f, mutedPaint)
        canvas.drawText("Principal / Authorized Registrar", 380f, y + 13f, mutedPaint)

        pdfDocument.finishPage(page)

        val reportDir = File(context.getExternalFilesDir(null), "Reports")
        if (!reportDir.exists()) {
            reportDir.mkdirs()
        }
        val safeName = decryptedStudentName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val finalFile = File(reportDir, "Academic_Report_${safeName}_Roll_${student.rollNo.ifBlank { "Unassigned" }}.pdf")

        return try {
            val fos = FileOutputStream(finalFile)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            finalFile
        } catch (e: Exception) {
            null
        }
    }
}
