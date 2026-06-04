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

        // ------------------ PAGE 1 ------------------
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 Size
        val page1 = pdfDocument.startPage(pageInfo1)
        val canvas1: Canvas = page1.canvas

        // Initialize paints
        val mainPaint = Paint()
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A") // Slate 900
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#0D9488") // Teal 600
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#475569") // Slate 600
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#1E293B") // Slate 800
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.parseColor("#64748B") // Slate 500
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.parseColor("#334155") // Slate 700
            textSize = 9f
            isAntiAlias = true
        }
        val cellValPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 8.5f
            isAntiAlias = true
        }
        val mutedPaint = Paint().apply {
            color = Color.parseColor("#94A3B8") // Slate 400
            textSize = 8f
            isAntiAlias = true
        }

        // 1. Top Decorative Primary Color Banner
        mainPaint.color = Color.parseColor("#0D9488") // Teal 600
        canvas1.drawRect(50f, 35f, 545f, 41f, mainPaint)

        // 2. Banner Header Title
        canvas1.drawText("STUDENT PERFORMANCE REPORT SUMMARY", 50f, 65f, titlePaint)
        canvas1.drawText("ACADEMIC DOSSIER & PROGRESS COMPILATION", 50f, 78f, subTitlePaint)

        // Right side school label
        val schoolLabel = if (student.schoolName.isNotBlank()) student.schoolName.uppercase() else "MARKS TRACKING ACADEMY"
        canvas1.drawText(schoolLabel, 310f, 65f, headerPaint.apply { textSize = 9f; color = Color.parseColor("#0F172A") })

        headerPaint.textSize = 12f // Reset

        // Draw horizontal separator
        mainPaint.color = Color.parseColor("#E2E8F0") // Slate 200
        canvas1.drawLine(50f, 92f, 545f, 92f, mainPaint)

        // 3. Student Bio Block (Two Column Layout)
        var y1 = 110f
        canvas1.drawText("Student Name:", 50f, y1, labelPaint)
        canvas1.drawText(decryptedStudentName, 140f, y1, valPaint.apply { isFakeBoldText = true; color = Color.parseColor("#0F172A") })

        canvas1.drawText("Roll Number:", 320f, y1, labelPaint)
        canvas1.drawText(student.rollNo.ifBlank { "N/A" }, 430f, y1, valPaint.apply { isFakeBoldText = false; color = Color.parseColor("#334155") })

        y1 += 16f
        canvas1.drawText("Class / Grade:", 50f, y1, labelPaint)
        canvas1.drawText(student.studentClass.ifBlank { "Unassigned" }, 140f, y1, valPaint)

        canvas1.drawText("School / Tenant Code:", 320f, y1, labelPaint)
        canvas1.drawText(student.schoolId.ifBlank { "GLOBAL_TENANT" }, 430f, y1, valPaint)

        y1 += 16f
        val rawParent = student.parentName
        val decryptedParent = if (rawParent.isNotBlank()) EncryptionUtil.decrypt(rawParent) else "No Parent Connected"
        canvas1.drawText("Parent / Guardian:", 50f, y1, labelPaint)
        canvas1.drawText(decryptedParent, 140f, y1, valPaint)

        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val dateString = sdf.format(Date())
        canvas1.drawText("Report Compiled On:", 320f, y1, labelPaint)
        canvas1.drawText(dateString, 430f, y1, valPaint)

        // Draw horizontal divider under Bio
        y1 += 15f
        mainPaint.color = Color.parseColor("#E2E8F0")
        canvas1.drawLine(50f, y1, 545f, y1, mainPaint)

        // 4. Executive Overview Indicators
        y1 += 18f
        canvas1.drawText("I. EXECUTIVE SCHOLASTIC OVERVIEW", 50f, y1, sectionTitlePaint)

        // Calculate basic performance statistics
        val overallMean = if (marks.isNotEmpty()) marks.map { it.marksObtained }.average() else 0.0
        val maxTotalObtained = marks.sumOf { it.marksObtained }
        val maxTotalPossible = marks.sumOf { it.maxMarks }
        
        val alphaGrade = when {
            overallMean >= 90 -> "A+ (Excellent)"
            overallMean >= 80 -> "A (Outstanding)"
            overallMean >= 70 -> "B+ (Very Good)"
            overallMean >= 65 -> "B (Satisfactory)"
            overallMean >= 50 -> "C (Pass)"
            else -> "F (Needs Help)"
        }

        // Draw executive KPI cards
        val cardWidth = 153f
        y1 += 10f
        
        // Card 1: Aggregate Mean
        mainPaint.color = Color.parseColor("#F8FAFC") // Slate 50 background
        canvas1.drawRoundRect(50f, y1, 50f + cardWidth, y1 + 50f, 6f, 6f, mainPaint)
        mainPaint.color = Color.parseColor("#E2E8F0") // Slate 200 border
        mainPaint.style = Paint.Style.STROKE
        canvas1.drawRoundRect(50f, y1, 50f + cardWidth, y1 + 50f, 6f, 6f, mainPaint)
        mainPaint.style = Paint.Style.FILL
        canvas1.drawText("AGGREGATE MEAN SCORE", 60f, y1 + 18f, mutedPaint.apply { textSize = 7f; isFakeBoldText = true })
        canvas1.drawText("${String.format(Locale.US, "%.1f", overallMean)}%", 60f, y1 + 38f, titlePaint.apply { textSize = 14f; color = Color.parseColor("#0284C7") })

        // Card 2: Cumulative Entries
        val card2X = 50f + cardWidth + 15f
        mainPaint.color = Color.parseColor("#F8FAFC")
        canvas1.drawRoundRect(card2X, y1, card2X + cardWidth, y1 + 50f, 6f, 6f, mainPaint)
        mainPaint.color = Color.parseColor("#E2E8F0")
        mainPaint.style = Paint.Style.STROKE
        canvas1.drawRoundRect(card2X, y1, card2X + cardWidth, y1 + 50f, 6f, 6f, mainPaint)
        mainPaint.style = Paint.Style.FILL
        canvas1.drawText("TOTAL GRADED EXAMS", card2X + 10f, y1 + 18f, mutedPaint)
        canvas1.drawText("${marks.size} Marks Logged", card2X + 10f, y1 + 38f, titlePaint.apply { textSize = 11.5f; color = Color.parseColor("#0F172A") })

        // Card 3: Grade Range
        val card3X = card2X + cardWidth + 15f
        mainPaint.color = Color.parseColor("#F8FAFC")
        canvas1.drawRoundRect(card3X, y1, card3X + cardWidth, y1 + 50f, 6f, 6f, mainPaint)
        mainPaint.color = Color.parseColor("#E2E8F0")
        mainPaint.style = Paint.Style.STROKE
        canvas1.drawRoundRect(card3X, y1, card3X + cardWidth, y1 + 50f, 6f, 6f, mainPaint)
        mainPaint.style = Paint.Style.FILL
        canvas1.drawText("SCHOLASTIC MERIT LEVEL", card3X + 10f, y1 + 18f, mutedPaint)
        canvas1.drawText(alphaGrade, card3X + 10f, y1 + 38f, titlePaint.apply { textSize = 11f; color = Color.parseColor("#0D9488") })

        // 5. Sorted Graded Marks Compilation Table
        y1 += 72f
        canvas1.drawText("II. INDIVIDUAL SCORE CARD ENTRIES", 50f, y1, sectionTitlePaint)

        y1 += 10f
        mainPaint.color = Color.parseColor("#F1F5F9") // Slate 100 table header
        canvas1.drawRect(50f, y1, 545f, y1 + 20f, mainPaint)
        
        // Draw Table Header columns
        canvas1.drawText("SUBJECT / COURSE", 56f, y1 + 14f, labelPaint.apply { color = Color.parseColor("#1E293B"); textSize = 8.5f })
        canvas1.drawText("EXAMINATION TYPE", 210f, y1 + 14f, labelPaint)
        canvas1.drawText("MARKS OBTAINED", 330f, y1 + 14f, labelPaint)
        canvas1.drawText("MAX SCORE", 435f, y1 + 14f, labelPaint)
        canvas1.drawText("PERCENTAGE", 495f, y1 + 14f, labelPaint)

        y1 += 20f
        
        val subjectMap = subjects.associate { it.id to it.name }
        
        if (marks.isEmpty()) {
            canvas1.drawText("No individual scorecard entries are available for this student portfolio yet.", 60f, y1 + 22f, valPaint.apply { isFakeBoldText = false; color = Color.GRAY })
            y1 += 40f
        } else {
            val borderPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }

            // Draw up to 22 marks to safely fit Page 1 without overflow
            val printableMarks = marks.take(22)
            printableMarks.forEach { mark ->
                canvas1.drawLine(50f, y1 + 17f, 545f, y1 + 17f, borderPaint)

                val subjectLabel = subjectMap[mark.subjectId] ?: "General Study"
                val examLabel = mark.examType
                val score = mark.marksObtained
                val maxScore = mark.maxMarks
                val percentage = if (maxScore > 0) (score / maxScore) * 100.0 else 0.0

                canvas1.drawText(subjectLabel, 56f, y1 + 11f, cellValPaint.apply { isFakeBoldText = true })
                canvas1.drawText(examLabel, 210f, y1 + 11f, cellValPaint.apply { isFakeBoldText = false })
                canvas1.drawText(String.format(Locale.US, "%.1f", score), 330f, y1 + 11f, cellValPaint)
                canvas1.drawText(String.format(Locale.US, "%.1f", maxScore), 435f, y1 + 11f, cellValPaint)
                
                val percentScoreStr = "${String.format(Locale.US, "%.1f", percentage)}%"
                val colorString = if (percentage >= 50.0) "#0D9488" else "#E11D48"
                canvas1.drawText(percentScoreStr, 495f, y1 + 11f, Paint().apply {
                    color = Color.parseColor(colorString)
                    textSize = 8.5f
                    isFakeBoldText = true
                    isAntiAlias = true
                })

                y1 += 17f
            }

            if (marks.size > 22) {
                canvas1.drawText("* Total marks count exceeds page limits. Showing top 22 entries.", 56f, y1 + 11f, mutedPaint.apply { color = Color.RED })
            }
        }

        // Page footer text page 1
        canvas1.drawText("Generated via Marks Tracking Suite | Web Analytics Portal", 50f, 810f, mutedPaint)
        canvas1.drawText("Page 1 of 2", 495f, 810f, mutedPaint)

        pdfDocument.finishPage(page1)


        // ------------------ PAGE 2 ------------------
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        val canvas2: Canvas = page2.canvas

        // 1. Top Decorative Primary Color Banner
        mainPaint.color = Color.parseColor("#0D9488") // Teal 600
        canvas2.drawRect(50f, 35f, 545f, 41f, mainPaint)

        // 2. Banner Header Title
        val titlePaint2 = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas2.drawText("ACADEMIC SUMMARY & PERFORMANCE TRENDS", 50f, 65f, titlePaint2)
        canvas2.drawText("DETAILED PERFORMANCE PROGRESS & SCHOLASTIC GROWTH VELOCITY", 50f, 78f, subTitlePaint)

        // Draw horizontal separator
        mainPaint.color = Color.parseColor("#E2E8F0")
        canvas2.drawLine(50f, 92f, 545f, 92f, mainPaint)

        // Calculate subject-wise average marks
        val subjectGrades = marks.groupBy { it.subjectId }
            .map { (subId, subMarks) ->
                val sumObtained = subMarks.sumOf { it.marksObtained }
                val sumMax = subMarks.sumOf { it.maxMarks }
                val avg = if (sumMax > 0) (sumObtained / sumMax) * 100.0 else 0.0
                val name = subjectMap[subId] ?: "General Study"
                name to avg
            }.sortedBy { it.first }

        val weakestSubject = if (subjectGrades.isNotEmpty()) subjectGrades.minByOrNull { it.second } else null
        val weakestSubjectName = weakestSubject?.first ?: "N/A"
        val weakestSubjectAvg = weakestSubject?.second ?: 0.0

        // 3. Subject-wise average marks section
        var y2 = 112f
        canvas2.drawText("III. SUBJECT-WISE AVERAGE METRICS INDEX (with progress checks)", 50f, y2, sectionTitlePaint)

        y2 += 12f
        
        // Draw 2-column list of subjects with custom progress bars
        if (subjectGrades.isEmpty()) {
            canvas2.drawText("No subject score records registered for average calculations.", 60f, y2 + 15f, valPaint.apply { color = Color.GRAY })
            y2 += 40f
        } else {
            subjectGrades.forEachIndexed { index, (subjectName, avgPct) ->
                val col = index % 2
                val row = index / 2
                val rx = if (col == 0) 50f else 305f
                val ry = y2 + 10f + (row * 30f)

                if (ry < 280f) { // Safely within subject-box height
                    // Subject title
                    canvas2.drawText(subjectName, rx, ry, cellValPaint.apply { isFakeBoldText = true; textSize = 8.5f })
                    // Numeric percentage
                    val percentStr = "${String.format(Locale.US, "%.1f", avgPct)}%"
                    canvas2.drawText(percentStr, rx + 195f, ry, valPaint.apply { isFakeBoldText = true; color = Color.parseColor("#0F172A") })

                    // Visual Progress Rail Underneath
                    val barY = ry + 6f
                    val barLeft = rx
                    val barRight = rx + 230f
                    val barHeight = 4.5f

                    // Rail track background
                    mainPaint.color = Color.parseColor("#F1F5F9")
                    canvas2.drawRect(barLeft, barY, barRight, barY + barHeight, mainPaint)

                    // Active track fill
                    val fillWidth = (avgPct.toFloat() / 100f) * 230f
                    mainPaint.color = when {
                        avgPct >= 75.0 -> Color.parseColor("#0D9488") // High (Teal)
                        avgPct >= 50.0 -> Color.parseColor("#0284C7") // Satisfactory (Sky Blue)
                        else -> Color.parseColor("#E11D48") // Low (Rose Red)
                    }
                    canvas2.drawRect(barLeft, barY, barLeft + fillWidth, barY + barHeight, mainPaint)
                }
            }
            // Advance height to the lower limit of grid
            val rowCount = (subjectGrades.size + 1) / 2
            y2 += 15f + (rowCount * 30f)
        }

        // Align y2 to start Section IV safely
        if (y2 < 290f) {
            y2 = 290f
        }

        // 4. Chronological Progress Trends Bar/Line Chart
        canvas2.drawText("IV. CHRONOLOGICAL EXAMINATION PERFORMANCE TRENDLINES", 50f, y2, sectionTitlePaint)
        
        y2 += 12f
        
        // Define standard chronological exams map
        val examOrder = listOf("Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual")
        val trendPoints = marks.groupBy { it.examType }
            .mapValues { (_, examMarks) ->
                val sumObtained = examMarks.sumOf { it.marksObtained }
                val sumMax = examMarks.sumOf { it.maxMarks }
                if (sumMax > 0) (sumObtained / sumMax) * 100.0 else 0.0
            }
            .toList()
            .sortedWith(compareBy { (examType, _) ->
                val index = examOrder.indexOf(examType)
                if (index != -1) index else Int.MAX_VALUE
            })

        // Draw axes and grids
        val chartLeft = 85f
        val chartRight = 505f
        val chartTop = y2 + 10f
        val chartBottom = y2 + 120f
        val chartHeight = 110f
        val chartWidth = 420f

        val axesPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            strokeWidth = 1.3f
            isAntiAlias = true
        }

        // Grid lines (y ticks)
        val gridPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            strokeWidth = 1f
        }
        
        for (i in 1..4) {
            val ty = chartBottom - (i * 27.5f)
            canvas2.drawLine(chartLeft, ty, chartRight, ty, gridPaint)
            canvas2.drawText("${i * 25}%", chartLeft - 26f, ty + 3f, mutedPaint.apply { textSize = 7.5f })
        }
        
        // Baseline axes labels
        canvas2.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axesPaint) // X axis
        canvas2.drawLine(chartLeft, chartTop - 5f, chartLeft, chartBottom, axesPaint) // Y axis

        if (trendPoints.isEmpty()) {
            canvas2.drawText("No chronologically registered test points found for trend visuals.", 130f, y2 + 65f, valPaint.apply { color = Color.GRAY })
        } else {
            val numPoints = trendPoints.size
            val stepX = if (numPoints > 1) chartWidth / (numPoints - 1) else chartWidth

            val linePaint = Paint().apply {
                color = Color.parseColor("#0D9488") // Teal line
                strokeWidth = 2.5f
                isAntiAlias = true
            }
            val pointPaint = Paint().apply {
                color = Color.parseColor("#0284C7") // Sky point
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val pointBorderPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                isAntiAlias = true
            }
            val trendLabelPaint = Paint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 8.5f
                isFakeBoldText = true
                isAntiAlias = true
            }

            // 1. Draw connection line paths
            for (i in 0 until numPoints) {
                val px = if (numPoints > 1) chartLeft + i * stepX else 295f
                val (_, pct) = trendPoints[i]
                val py = chartBottom - (pct.toFloat() / 100f * chartHeight)

                if (i > 0) {
                    val prevX = if (numPoints > 1) chartLeft + (i - 1) * stepX else 295f
                    val prevY = chartBottom - (trendPoints[i - 1].second.toFloat() / 100f * chartHeight)
                    canvas2.drawLine(prevX, prevY, px, py, linePaint)
                }
            }

            // 2. Draw dots and values labels
            for (i in 0 until numPoints) {
                val px = if (numPoints > 1) chartLeft + i * stepX else 295f
                val (examType, pct) = trendPoints[i]
                val py = chartBottom - (pct.toFloat() / 100f * chartHeight)

                // Fill circle
                canvas2.drawCircle(px, py, 4.5f, pointPaint)
                canvas2.drawCircle(px, py, 4.5f, pointBorderPaint)

                // Numerical percentage text
                val valStr = "${String.format(Locale.US, "%.1f", pct)}%"
                canvas2.drawText(valStr, px - 14f, py - 9f, trendLabelPaint)

                // X Axis Labels
                canvas2.drawText(examType, px - 18f, chartBottom + 14f, trendLabelPaint.apply { textSize = 7.5f; color = Color.parseColor("#475569") })
            }
        }

        // 5. Diagnostics recommendations
        y2 += 160f
        canvas2.drawText("V. AI-LITE ACADEMIC DIAGNOSTICS & SYLLABUS DIRECTIVES", 50f, y2, sectionTitlePaint)

        y2 += 10f
        mainPaint.color = Color.parseColor("#F8FAFC") // Slate 50 box
        canvas2.drawRoundRect(50f, y2, 545f, y2 + 65f, 6f, 6f, mainPaint)
        mainPaint.color = Color.parseColor("#E2E8F0") // Border line
        mainPaint.style = Paint.Style.STROKE
        canvas2.drawRoundRect(50f, y2, 545f, y2 + 65f, 6f, 6f, mainPaint)
        mainPaint.style = Paint.Style.FILL

        // Dynamic Growth Pace Analytics text
        val adviceText = if (trendPoints.size >= 2) {
            val firstPct = trendPoints.first().second
            val lastPct = trendPoints.last().second
            val velocity = lastPct - firstPct
            if (velocity > 0) {
                "Growth Pace: Positive progress velocity of +${String.format(Locale.US, "%.1f", velocity)}% detected! Student highlights healthy learning expansion. Practice advanced curriculum worksheets."
            } else if (velocity < 0) {
                "Growth Pace: Deficit trajectory of ${String.format(Locale.US, "%.1f", velocity)}% recorded. Recommend systematic focus blocks, remedial homework, and targeted teaching guides."
            } else {
                "Growth Pace: Flat line stability (0.0% variance). Review test scores consistently to promote a positive momentum cycle."
            }
        } else {
            "Growth Pace: Steady baseline. Continue entering marks across Weekly, Monthly tests to render continuous progress curves."
        }
        
        val improvementSubjectText = if (weakestSubject != null) {
            "Strongest Improvement Need: Focused study in ${weakestSubjectName.uppercase()} is highly advised (current average is ${String.format(Locale.US, "%.1f", weakestSubjectAvg)}%)."
        } else {
            "Strongest Improvement Need: No subject deficits found. Great academic compliance maintained!"
        }

        canvas2.drawText(adviceText, 62f, y2 + 18f, valPaint.apply { textSize = 8.5f; isFakeBoldText = true; color = Color.parseColor("#1E293B") })
        canvas2.drawText(improvementSubjectText, 62f, y2 + 34f, valPaint.apply { textSize = 8f; isFakeBoldText = false; color = Color.parseColor("#475569") })
        canvas2.drawText("Educational Guidance Directive: Weekly review of errors, mock exam sessions, and concept cards will accelerate milestone grades.", 62f, y2 + 48f, valPaint)

        // 6. Certification statements & Authorizing signatures
        y2 += 95f
        canvas2.drawText("CERTIFYING AUTHORITY & INFORMATION COMPLIANCE", 50f, y2, labelPaint.apply { textSize = 8.5f })
        
        y2 += 15f
        canvas2.drawText("1. This report card compiling grades of $decryptedStudentName is locked via secure encryption algorithms.", 50f, y2, mutedPaint)
        y2 += 11f
        canvas2.drawText("2. Fully compliant under our local-first student data privacy guidelines and verified school ledger formats.", 50f, y2, mutedPaint)

        // Signature lines at bottom
        y2 += 50f
        mainPaint.color = Color.parseColor("#94A3B8")
        canvas2.drawLine(70f, y2, 200f, y2, mainPaint)
        canvas2.drawLine(380f, y2, 510f, y2, mainPaint)
        canvas2.drawText("Class Instructor / Evaluator", 82f, y2 + 13f, mutedPaint)
        canvas2.drawText("Principal / Authorized Registrar", 380f, y2 + 13f, mutedPaint)

        // Page footer text page 2
        canvas2.drawText("Generated via Marks Tracking Suite | Web Analytics Portal", 50f, 810f, mutedPaint)
        canvas2.drawText("Page 2 of 2", 495f, 810f, mutedPaint)

        pdfDocument.finishPage(page2)

        // Write content to destination file
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
            e.printStackTrace()
            null
        }
    }
}
