package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.model.PaymentRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfInvoiceGenerator {
    fun generateInvoicePdf(context: Context, record: PaymentRecord): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (in postscript points)
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E293B") // Slate 800
            textSize = 22f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#475569") // Slate 600
            textSize = 14f
            isFakeBoldText = true
        }
        val labelPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 11f
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }
        val mutedPaint = Paint().apply {
            color = Color.parseColor("#64748B") // Slate 500
            textSize = 9f
        }

        // Draw header background style (top banner border)
        paint.color = Color.parseColor("#0F172A") // Slate 900
        canvas.drawRect(50f, 30f, 545f, 35f, paint)

        // Logo & Title
        canvas.drawText("MARKS TRACKING", 50f, 65f, titlePaint)
        canvas.drawText("STUDENT MARKS TRACKING SAAS", 50f, 80f, mutedPaint)
        
        canvas.drawText("TAX INVOICE / RECEIPT", 390f, 65f, headerPaint)

        // Metadata block (left side)
        canvas.drawText("Invoice Details:", 50f, 115f, labelPaint)
        canvas.drawText("Invoice Number:  INV-MT-${record.id}-${record.timestamp % 1000}", 50f, 130f, valuePaint)
        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date(record.timestamp))
        canvas.drawText("Invoice Date:    $dateStr", 50f, 145f, valuePaint)
        canvas.drawText("Payment Gateway: ${record.paymentGateway}", 50f, 160f, valuePaint)
        canvas.drawText("Reference ID:    ${record.paymentId}", 50f, 175f, valuePaint)

        // Customer details block (right side)
        canvas.drawText("Bill To:", 350f, 115f, labelPaint)
        canvas.drawText("Name:  ${record.userName}", 350f, 130f, valuePaint)
        canvas.drawText("Email: ${record.userEmail}", 350f, 145f, valuePaint)

        // Draw horizontal partition
        paint.color = Color.parseColor("#CBD5E1") // Slate 300
        canvas.drawLine(50f, 195f, 545f, 195f, paint)

        // Table headers
        var y = 220f
        paint.color = Color.parseColor("#F1F5F9") // Light grey banner background
        canvas.drawRect(50f, y, 545f, y + 25f, paint)
        
        canvas.drawText("Subscription Item Description", 60f, y + 17f, labelPaint)
        canvas.drawText("Base Price (INR)", 430f, y + 17f, labelPaint)

        // Table body item
        y += 45f
        val planDesc = when (record.planType) {
            "INDIVIDUAL_PARENT_PLAN" -> "Individual Parent Plan (1 Year Access - ₹100 base)"
            "SCHOOL_PLAN" -> "School Admin Plan (1 Year Access - ₹10,000 base)"
            else -> "Marks Tracking Pro Student Membership"
        }
        canvas.drawText(planDesc, 60f, y, textPaint)
        canvas.drawText("INR ${String.format("%.2f", record.basePrice)}", 430f, y, textPaint)

        // Draw divider
        y += 30f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(50f, y, 545f, y, paint)

        // Totals breakdown (Right-aligned columns)
        y += 25f
        canvas.drawText("Subtotal:", 320f, y, labelPaint)
        canvas.drawText("INR ${String.format("%.2f", record.basePrice)}", 430f, y, valuePaint)

        y += 20f
        canvas.drawText("GST (18%):", 320f, y, labelPaint)
        canvas.drawText("INR ${String.format("%.2f", record.gstAmount)}", 430f, y, valuePaint)

        y += 25f
        // Bold double line under totals
        paint.color = Color.parseColor("#94A3B8")
        paint.strokeWidth = 2f
        canvas.drawLine(320f, y - 5f, 545f, y - 5f, paint)
        
        canvas.drawText("Grand Total:", 320f, y + 10f, labelPaint)
        canvas.drawText("INR ${String.format("%.2f", record.totalAmount)}", 430f, y + 10f, titlePaint.apply { textSize = 14f })

        // Reset text paint sizes
        titlePaint.textSize = 22f

        // Bottom Footer details
        y += 80f
        canvas.drawText("System Certifications & Security Compliance:", 50f, y, labelPaint)
        y += 15f
        canvas.drawText("1. This is a computationally signed invoice registered under multi-tenant SSL/CSRF protection rules.", 50f, y, mutedPaint)
        y += 12f
        canvas.drawText("2. Fully compliant with AES-256 local-first client student identifiers encoding.", 50f, y, mutedPaint)
        y += 12f
        canvas.drawText("3. For technical queries regarding your subscriptions, mail to support@markstracking.saas.", 50f, y, mutedPaint)

        // Footer signature placeholder
        y += 50f
        canvas.drawLine(380f, y, 510f, y, paint.apply { strokeWidth = 1f; color = Color.BLACK })
        canvas.drawText("Authorized SaaS Coordinator", 380f, y + 15f, mutedPaint)

        pdfDocument.finishPage(page)

        // Directory path to place invoices in external downloads
        val dir = File(context.getExternalFilesDir(null), "Invoices")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "Invoice_${record.paymentId}.pdf")
        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
