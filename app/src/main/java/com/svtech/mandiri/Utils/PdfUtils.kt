package com.svtech.mandiri.Utils

import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream


class PdfUtils{
    var namaFile:String
    var doc : Document
    constructor(namaFile: String){
        this.namaFile = namaFile
        this.doc = Document()
        PdfWriter.getInstance(doc, FileOutputStream(namaFile))
        doc.open()
        doc.pageSize = PageSize.A4
        doc.addCreationDate()
    }

    companion object{
        val fontNormal = Font(Font.FontFamily.TIMES_ROMAN,12F)
        val fontTitle = Font(Font.FontFamily.TIMES_ROMAN,18F, Font.BOLD)
        val fontHeader = Font(Font.FontFamily.TIMES_ROMAN,12F, Font.BOLD)
        val align_center = Element.ALIGN_CENTER
        val align_left = Element.ALIGN_LEFT
        val align_right = Element.ALIGN_RIGHT
        val align_justified = Element.ALIGN_JUSTIFIED
        val no_border = Rectangle.NO_BORDER

    }

    fun addParagraf(text:String, font:Font?, align:Int?){
        val paragraph = Paragraph(text, font?: fontNormal)
        paragraph.alignment = align?: align_left
        doc.add(paragraph)
    }

    fun addNewEnter(){
        doc.add(Paragraph("\n"))
    }

    fun addTable(table: PdfPTable, totalWidth:FloatArray, align: Int?){
        table.isLockedWidth = true
        table.setTotalWidth(totalWidth)
        table.horizontalAlignment = align?: align_left
        doc.add(table)
    }

    fun createCell(text: String, font: Font?, border:Int?): PdfPCell {
        val cell = PdfPCell(Phrase(text, font?: fontNormal))
        cell.border = border?:Rectangle.BOX
        return cell
    }

    fun close(){
        doc.close()
    }

}