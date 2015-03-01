package com.comtop.mynote.utils;

import java.io.File;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Environment;


public class AudioRecorder {
	private static int SAMPLE_RATE_IN_HZ = 8000; 
	// ������Ƶ��¼�Ƶ�����CHANNEL_IN_STEREOΪ˫������CHANNEL_CONFIGURATION_MONOΪ������  
	private static int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	// ��Ƶ���ݸ�ʽ:PCM 16λÿ����������֤�豸֧�֡�PCM 8λÿ����������һ���ܵõ��豸֧�֡�
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	
	final MediaRecorder recorder = new MediaRecorder();
	final String path;

	public AudioRecorder(String path)
	{
		this.path = path; // sanitizePath(path);
	}

	/*private String sanitizePath(String path)
	{
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}
		if (!path.contains("."))
		{
			path += ".amr";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/my" + path;
	}*/

	public void start() throws IOException
	{
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) { throw new IOException(
				"SD Card is not mounted,It is  " + state + "."); }
		File directory = new File(path).getParentFile();
		if (!directory.exists() && !directory.mkdirs()) { throw new IOException(
				"Path to file could not be created"); }
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setAudioSamplingRate(SAMPLE_RATE_IN_HZ);
	
		//recorder.setAudioSamplingRate();
	 //AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ, channelConfig, audioFormat);

		recorder.setOutputFile(path);
		recorder.prepare();
		recorder.start();
	}

	public void stop() throws IOException
	{
		recorder.stop();
		recorder.release();
	}
	
	public double getAmplitude() {		
		if (recorder != null){			
			return  (recorder.getMaxAmplitude());		
			}		
		else			
			return 0;	
		}
}