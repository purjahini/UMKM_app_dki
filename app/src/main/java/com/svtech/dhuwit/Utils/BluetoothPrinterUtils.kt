package com.svtech.dhuwit.Utils

import java.io.OutputStream

/*class untuk melakukan proses printing*/
class BluetoothPrinterUtils(val outputStream: OutputStream) {
    fun printText(text:String, alignment: ByteArray){
        outputStream.write(alignment)
        outputStream.write(text.toByteArray())
        printNewLine()
    }
    fun printText(text:String){
        outputStream.write(text.toByteArray())
        printNewLine()
    }
    fun printString(str:String, alignment: ByteArray){
        outputStream.write(alignment)
        outputStream.write(str.toByteArray())
    }
    fun printString(str:String){
        outputStream.write(str.toByteArray())
    }
    fun printNewLine(){
        val str = "\n"
        outputStream.write(str.toByteArray())
    }
    fun printNewLines(count:Int){
        repeat(count) {
            val str = "\n"
            outputStream.write(str.toByteArray())
        }
    }
    fun printLine(){
        val str = "================================\n"
//        val str = "==========================================\n" //small font
        outputStream.write(str.toByteArray())
    }
    fun setFontStyle(style:ByteArray){
        outputStream.write(style)
    }

    companion object{
        val ALIGN_LEFT = byteArrayOf(0x1b, 'a'.toByte(), 0x00)
        val ALIGN_RIGHT = byteArrayOf(0x1b, 'a'.toByte(), 0x02)
        val ALIGN_CENTER = byteArrayOf(0x1b, 'a'.toByte(), 0x01)
        val f1 = byteArrayOf(0x1B, 0x21, 0x01) // 0- small size text
        val f2 = byteArrayOf(0x1B, 0x21, 0x00) // 1- normal size text
        val f3 = byteArrayOf(0x1B, 0x21, 0x08) // 2- only bold text
        val f4 = byteArrayOf(0x1B, 0x21, 0x20) // 3- bold with medium text
        val f5 = byteArrayOf(0x1B, 0x21, 0x10) // 4- bold with large text

    }

}