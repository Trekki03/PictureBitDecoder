package de.tobias.datareciever.gui.actionListener

import de.tobias.datareciever.decoder.Decoder
import de.tobias.datareciever.util.FfmpegConnector
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JTextArea
import javax.swing.JTextPane

class ReadInputButtonActionListener(inputTextPane : JTextPane, textOutput: JTextArea, binaryOutput: JTextArea) : ActionListener
{
    private val inputPane = inputTextPane
    private val textOutputTextArea = textOutput
    private val binaryOutputTextArea = binaryOutput

    override fun actionPerformed(e: ActionEvent?)
    {
        val path : String = inputPane.text
        if(path.isNotBlank())
        {
            val ffmpeg = FfmpegConnector()
            val decoder = Decoder(textOutputTextArea, binaryOutputTextArea)


            ffmpeg.splitVideoInFrames(path)
            decoder.decodePicturesFromFile()
            ffmpeg.deleteAuxiliaryFolder()
        }
        else
        {
            println("No Content")
        }
    }
}