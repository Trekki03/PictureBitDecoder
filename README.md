# PictureBitDecoder

This is a Decoder for a school project about sending data. It decodes a video with bit information inside.
It used Hemming-7,4 for the bit correction. Data is sent in the order: <br>
b0 b1 b2 b3 p0 p1 p2 <br>
Ffmpeg is used to split the video into single frames.
<br>
<br>
You can find a Video to decode in the example folder. <br>
It contains the String <b>" Test1" </b> (With Space at the beginning)
