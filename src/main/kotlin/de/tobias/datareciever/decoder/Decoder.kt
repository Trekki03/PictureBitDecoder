package de.tobias.datareciever.decoder

import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JTextArea
import kotlin.system.exitProcess

class Decoder(textOutput: JTextArea, binaryOutput: JTextArea)
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
    private var correctedMessageBuffer: Queue<Boolean> = LinkedList()
    private var binaryString: String = ""
    private var textString: String = ""
    private var textOutputField: JTextArea = textOutput
    private var binaryOutputField: JTextArea = binaryOutput

    /**
     * contains the functions, which convert the Video to the
     * included text/bits
     */
    fun decodePicturesFromFile()
    {
        //picture path
        val path = System.getProperty("user.home") + "/dataReceiver/pictures/"
        val files: Array<out File>? = File(path).listFiles()

        if (files != null)
        {
            //Iterate through
            for (i in 1..files.size)
            {
                //Add uncorrected bits from Video to buffer
                val number = String.format("%07d", i)
                analysePhoto("$path$number.png")
            }
            //Correct the bits (Hamming-7,4)
            correctBuffer()
            //Convert buffer intro readable text/binary output
            processBuffer()

            //printout text / binary output
            println(textString)
            println(binaryString)
            textOutputField.text = textString
            binaryOutputField.text = binaryString
        }
    }

    /**
     * Converts the content of the pictures into bits and add it to the message buffer. Has to be invoked for every picture
     * @param path Path of the picture
     */
    private fun analysePhoto(path: String)
    {
        val image: BufferedImage = ImageIO.read(File(path))

        //Get pixel information of the picture
        //data bit + control bits
        val b0: Color = getColors(image.getRGB(image.width / 3 - image.width / 6, image.height / 2 - image.height / 4))
        val bP0: Color = getColors(image.getRGB(image.width / 3 * 2 - image.width / 6, image.height / 2 - image.height / 4))
        val bP1: Color = getColors(image.getRGB(image.width / 3 * 3 - image.width / 6, image.height / 2 - image.height / 4))

        //clock bit + control bits
        val s0: Color = getColors(image.getRGB(image.width / 3 - image.width / 6, image.height / 2 * 2 - image.height / 4))
        val sP0: Color = getColors(image.getRGB(image.width / 3 * 2 - image.width / 6, image.height / 2 * 2 - image.height / 4))
        val sP1: Color = getColors(image.getRGB(image.width / 3 * 3 - image.width / 6, image.height / 2 * 2 - image.height / 4))

        /*
         * Video is white at the beginning.
         * When the green or red value is below 150 for the first time, the data part is started
         */
        if (!started)
        {
            if (((s0.green < 150 && sP0.green < 150) && sP1.green < 150) || ((s0.red < 150 && sP0.red < 150) && sP1.red < 150))
            {
                started = true
                binaryOutputField.text = "$path: First Frame"
            }
        }

        if (started)
        {
            /* When the clock changes, the next picture contains the new bits.
             * The second frame of a new clock cycle is used, because the
             * data part of the first picture is caused by camera issues mostly
             * corrupted
             */
            if (!takeNext)
            {
                val switchState: Boolean = if ((s0.green > s0.red) && (sP0.green > sP0.red) && (sP1.green > sP1.red))
                {
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

                if (lastSwitchState != switchState)
                {
                    takeNext = true
                    lastSwitchState = switchState
                    return
                }
            }
            /*
             * When frame is selected for data extraction
             */
            else
            {
                takeNext = false

                /*
                 * First, it tests, if all three pixels from the data area have the same color
                 */
                val bit: Boolean = if ((b0.green > b0.red) && (bP0.green > bP0.red) && (bP1.green > bP1.red))
                {
                    true
                }
                else if ((b0.green < b0.red) && (bP0.green < bP0.red) && (bP1.green < bP1.red))
                {
                    false
                }
                /*
                 * if this isn't the case, the bit gets corrected
                 */
                else
                {
                    bitCorrection(b0, bP0, bP1, path)
                }
                //Then the (corrected) bit is added to the buffer
                messageBuffer.add(bit)
            }
        }
    }

    /**
     * return the green and red value of the pixel
     * @param color Int value with the color of the pixel
     * @return Color class object with green and red values
     */
    private fun getColors(color: Int): Color
    {
        val green: Int = color and 0xff00 shr 8
        val red: Int = color and 0xff0000 shr 16

        return Color(green, red)
    }

    /**
     * Checks, if the bit has an error and correct it.
     * Only one bit is allowed to be incorrect
     * @param b0 data bit Color object
     * @param bP0 data pBit0 Color object
     * @param bP1 data PBit1 Color object
     * @param path String path to the object
     */
    private fun bitCorrection(b0: Color, bP0: Color, bP1: Color, path: String): Boolean
    {
        /*
         * Checks which part is more present.
         * e.g. When two pBits are the same, the data bit is incorrect.
         */
        return if ((bP0.green > bP0.red) && (bP1.green > bP1.red))
        {
            true
        }
        else if ((bP0.green < bP0.red) && (bP1.green < bP1.red))
        {
            false
        }
        else if ((b0.green > bP0.red) && (bP1.green > bP0.red))
        {
            true
        }
        else if ((b0.green < bP0.red) && (bP1.green < bP0.red))
        {
            false
        }
        else if ((b0.green > bP1.red) && (b0.green > bP1.red))
        {
            true
        }
        else if ((b0.green < bP1.red) && (b0.green < bP1.red))
        {
            false
        }
        else
        {
            binaryOutputField.text = "Unable to Read bit $path"
            false
        }
    }

    /**
     * This function checks the buffer with the Hemming-7,4 style pBits for incorrect bits and corrects them.
     * Only one bit per 7 bit errors can be corrected.
     */
    private fun correctBuffer()
    {
        while (messageBuffer.isNotEmpty())
        {
            val dataArray: Array<Boolean?> = arrayOfNulls(7)
            //Message has to have a multiplier of 7 bits
            if(messageBuffer.size < 7)
            {
                println("Lost Bits while Transmitting Data")
                assert(false)
            }
            //read bits
            for(c in 0..6)
            {
                dataArray[c] = messageBuffer.remove()
            }

            /* Order and calculation of bits
             * P0 -> B0 ^ B1 ^ B3
             * P1 -> B0 ^ B2 ^ B3
             * P2 -> B1 ^ B2 ^ B3
             * 0 - B0
             * 1 - B1
             * 2 - B2
             * 3 - B3
             * 4 - P0
             * 5 - P1
             * 6 - P2
             */

            /*
             * Calculate the pBits and check if the are similar to the received ones
             */
            val pBitArray: Array<Boolean> = arrayOf(true, true, true)
            if(((dataArray[0]!! xor dataArray[1]!!) xor dataArray[3]!!) != dataArray[4]!!)
            {
                println("Wrong bP0")
                pBitArray[0] = false
            }
            if(((dataArray[0]!! xor dataArray[2]!!) xor dataArray[3]!!) != dataArray[5]!!)
            {
                println("Wrong bP1")
                pBitArray[1] = false
            }
            if(((dataArray[1]!! xor dataArray[2]!!) xor dataArray[3]!!) != dataArray[6]!!)
            {
                println("Wrong bP2")
                pBitArray[1] = false
            }

            //Test how many pBits are incorrect
            var wrongPBits: Int = 0
            for(i in 0..2)
            {
                if(!pBitArray[i])
                {
                    ++wrongPBits
                }
            }

            /*
             * Correct Bits:
             * if one pBit is incorrect, the pBit isn't correct -> ignore
             * if two pBit are incorrect, error is in bit 0-2   -> correct
             * if three pBits are incorrect, bit 3 is incorrect -> correct
             */
            if(wrongPBits == 2)
            {
                // Test with the combinations, which bit is incorrect
                if(!pBitArray[0] && !pBitArray[1])
                {
                    println("Corrected b0")
                    dataArray[0] = !dataArray[0]!!
                }
                else if(!pBitArray[0] && !pBitArray[2])
                {
                    println("Corrected b1")
                    dataArray[1] = !dataArray[1]!!
                }
                else if(!pBitArray[1] && !pBitArray[2])
                {
                    println("Corrected b2")
                    dataArray[2] = !dataArray[2]!!
                }
            }
            else if(wrongPBits == 3)
            {
                println("Corrected b3")
                dataArray[3] = !dataArray[3]!!
            }

            //Add all checked/corrected bits, without pBits, to the corrected message buffer
            for(i in 0..3)
            {
                correctedMessageBuffer.add(dataArray[i])
            }
        }
    }

    /**
     * translate buffer into readable form
     */
    private fun processBuffer()
    {
        while(correctedMessageBuffer.isNotEmpty())
        {
            //create a char for writing the buffer
            var letter = 0

            //  16 bit operations per char. The printout.
            for (i in 0..15)
            {
                // Insert bits into buffer
                var bit: Boolean = correctedMessageBuffer.remove()
                letter = (letter shl 1) or if (bit) 0b1 else 0b0
                //add bits to binary buffer
                ++counter
                binaryString += if (bit) 1 else 0

                //add a space every 16 letters to the binary Buffer
                if (counter == 16)
                {
                    binaryString += " "
                    counter = 0
                }
            }
            textString += letter.toChar()
        }
    }
}