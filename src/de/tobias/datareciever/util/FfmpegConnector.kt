package de.tobias.datareciever.util

import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.io.File

class FfmpegConnector
{
    fun splitVideoInFrames(path: String)
    {
        val folderPath : String = System.getProperty("user.home") + "/dataReceiver/pictures/"
        Files.createDirectories(Paths.get(folderPath))
        print("Started")
        val process = Runtime.getRuntime().exec("ffmpeg -i $path $folderPath/%07d.png")
        while(process.isAlive)
        {
            print(".")
            TimeUnit.MILLISECONDS.sleep(200)
        }
       println("Finished")
    }

    fun deleteAuxiliaryFolder()
    {
        val path = System.getProperty("user.home") + "/dataReceiver"
        val files : Array<out File>? = File("$path/pictures/").listFiles()

        if (files != null)
        {
            for(i in files)
            {
                Files.deleteIfExists(Paths.get(i.absolutePath))
            }
        }
        Files.deleteIfExists(Paths.get("$path/pictures"))
        Files.deleteIfExists(Paths.get(path))
    }
}