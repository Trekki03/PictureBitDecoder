package de.tobias.datareciever.decoder

import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JTextArea

class Decoder(textOutput : JTextArea, binaryOutput: JTextArea)
{
    //Datatype
    class Color(greenValue: Int, redValue: Int)
    {
        var green: Int = greenValue
        var red: Int = redValue
    }

    //Vars
    private var lastSwitchState: Boolean = true
    private var started: Boolean = false
    private var takeNext: Boolean = false
    private var counter: Int = 0
    private var messageBuffer: Queue<Boolean> = LinkedList()
    private var binaryString : String = ""
    private var textString : String = ""
    private var textOutputField: JTextArea = textOutput
    private var binaryOutputField: JTextArea = binaryOutput

    fun decodePicturesFromFile()
    {
        val path = System.getProperty("user.home") + "/dataReceiver/pictures/"

        val files : Array<out File>? = File(path).listFiles()

        if (files != null)
        {
            for(i in 1..files.size)
            {
                val number = String.format("%07d", i )
                analysePhoto("$path$number.png")
            }
            processBuffer()
            textOutputField.text = textString
            binaryOutputField.text = binaryString
        }
    }

    private fun analysePhoto(path: String) {
        val image: BufferedImage = ImageIO.read(File(path))

        val b0: Color = getColors(image.getRGB(image.width / 3 - image.width / 6, image.height / 2 - image.height / 4))
        val bP0: Color = getColors(image.getRGB(image.width / 3 * 2 - image.width / 6, image.height / 2 - image.height / 4))
        val bP1: Color = getColors(image.getRGB(image.width / 3 * 3 - image.width / 6, image.height / 2 - image.height / 4))

        val s0: Color = getColors(image.getRGB(image.width / 3 - image.width / 6, image.height / 2 * 2 - image.height / 4))
        val sP0: Color = getColors(image.getRGB(image.width / 3 * 2 - image.width / 6, image.height / 2 * 2 - image.height / 4))
        val sP1: Color = getColors(image.getRGB(image.width / 3 * 3 - image.width / 6, image.height / 2 * 2 - image.height / 4))

        if (!started) {
            if (((s0.green < 150 && sP0.green < 150) && sP1.green < 150) || ((s0.red < 150 && sP0.red < 150) && sP1.red < 150)) {
                started = true
                binaryOutputField.text = "$path: First Frame"
            }
        }

        if (started)
        {
            if (!takeNext)
            {
                val switchState: Boolean = if ((s0.green > s0.red) && (sP0.green > sP0.red) && (sP1.green > sP1.red)) {
                    true
                }
                else if ((s0.green < s0.red) && (sP0.green < sP0.red) && (sP1.green < sP1.red))
                {
                    false
                }
                else
                {
                    lastSwitchState
                }

                if (lastSwitchState != switchState) {
                    takeNext = true
                    lastSwitchState = switchState
                    return
                }
            }
            else
            {
                takeNext = false

                val bit: Boolean = if ((b0.green > b0.red) && (bP0.green > bP0.red) && (bP1.green > bP1.red))
                {
                    true
                }
                else if ((b0.green < b0.red) && (bP0.green < bP0.red) && (bP1.green < bP1.red))
                {
                    false
                }
                else
                {
                    bitCorrection(b0, bP0, bP1, path)
                }

                ++counter
                binaryString += if (bit) 1 else 0
                messageBuffer.add(bit)
                if (counter == 16) {
                    binaryString += " "
                    counter = 0
                }
            }
        }
    }

    private fun getColors(color: Int): Color
    {
        val green: Int = color and 0xff00 shr 8
        val red: Int = color and 0xff0000 shr 16

        return Color(green, red)
    }

    private fun bitCorrection(b0: Color, bP0: Color, bP1: Color, path: String): Boolean
    {
        return if ( (bP0.green > bP0.red) && (bP1.green > bP1.red) )
        {
            true
        }
        else if ( (bP0.green < bP0.red) && (bP1.green < bP1.red) )
        {
            false
        }
        else if ( (b0.green > bP0.red) && (bP1.green > bP0.red) )
        {
            true
        }
        else if ( (b0.green < bP0.red) && (bP1.green < bP0.red) )
        {
            false
        }
        else if ( (b0.green > bP1.red) && (b0.green > bP1.red) )
        {
            true
        }
        else if ( (b0.green < bP1.red) && (b0.green < bP1.red) )
        {
            false
        }
        else
        {
            binaryOutputField.text = "Unable to Read bit $path"
            false
        }
    }
    private fun processBuffer() {
        print("\n")
        while (!messageBuffer.isEmpty()) {
            //create a char for writing the buffer
            var letter = 0

            //  16 bit operations per char. The printout.
            for (i in 0..15) {
                // Insert bits into buffer
                letter = (letter shl 1) or if(messageBuffer.remove()) 0b1 else 0b0
            }
            textString += letter.toChar()
        }
    }
}