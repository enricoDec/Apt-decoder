# NOAA APT decoder
[Javadoc](https://enricodec.github.io/Apt-decoder/)  
The project is still being developed any feedback would be much apreciated!

# Audio Format
- Audio has to be .wav
- Stereo or Mono (Stereo will be converted to Mono automatically)
- Sample Rate has to be 20800Hz (Guide on how to resample with audacity [below](#audacity-resample))

# Console Application Usage
1. Download the latest jar release 
2. Execute with java -jar apt-decoder.jar [Input Audio File] [Output Image File] [Options]
```shell
java -jar apt-decoder.jar audio.wav output.png
```
## Options
- -R or --Raw to save a raw version of the output image (no sync, or correction)
- -D or --Debug to output some debug info

![](../../blob/docs/images/example_1.png)

# Audacity Resample

1. Import Audio
2. Select Track
3. Tracks -> Resample... -> 20800Hz -> Ok
4. Change Project Rate to 20800Hz (Usually bottom left)
5. File -> Export -> Export as WAV
6. Done
