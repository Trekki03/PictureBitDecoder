package de.tobias.datareciever.util

import java.awt.FileDialog
import java.io.File
import javax.swing.JFrame

class FileContext
{
    fun openFileDialog() : String
    {
        val fileDialog = FileDialog(JFrame())
        fileDialog.isVisible = true
        val files : Array<out File>? = fileDialog.files
        return if(files != null)
        {
            if(files.isNotEmpty())
            {
                files[0].absolutePath
            }
            else
            {
                openFileDialog()
            }
        }
        else
        {
            openFileDialog()
        }
    }
}