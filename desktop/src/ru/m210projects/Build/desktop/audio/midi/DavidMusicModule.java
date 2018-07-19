// This file is part of Mocha Doom.
// Copyright (C) 1993-1996  id Software, Inc.
// Copyright (C) 2010-2013  Victor Epitropou
// Copyright (C) 2016-2017  Alexandre-Xavier Labonté-Lamoureux
//
// This file has been modified by Alexander Makarov-[M210] (m210-2007@mail.ru)

package ru.m210projects.Build.desktop.audio.midi;

import static ru.m210projects.Build.OnSceenDisplay.Console.OSDTEXT_GOLD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;

import ru.m210projects.Build.Audio.BMusic.Music;
import ru.m210projects.Build.Audio.BMusic.MusicSource;
import ru.m210projects.Build.OnSceenDisplay.Console;

public class DavidMusicModule implements Music {

	protected Sequencer sequencer;
	protected VolumeScalingReceiver receiver;
	private Transmitter transmitter;
	
	private MidiDevice.Info nDevice;
	private String name;
	private boolean inited;
	
	public DavidMusicModule(int nDevice) {
		List<MidiDevice.Info> devices = getDevices();
		for(int i = 0; i < devices.size(); i++) {
			if(i == nDevice) {
				name = devices.get(i).getName();
				this.nDevice = devices.get(i);
			}
		} 
	}
	
	public static List<MidiDevice.Info> getDevices()
	{
		List<MidiDevice.Info> dInfos = new ArrayList<MidiDevice.Info>(Arrays.asList(MidiSystem.getMidiDeviceInfo()));
        try {
        for (Iterator<MidiDevice.Info> it = dInfos.iterator(); it.hasNext();) {
            MidiDevice.Info dInfo = it.next();
            MidiDevice dev = MidiSystem.getMidiDevice(dInfo);
            if (dev.getMaxReceivers() == 0) {
                // We cannot use input-only devices
                it.remove();
            }
        }
        } catch (Exception e) {
        	e.printStackTrace();
			return null;
        }
		
		return dInfos;
	}
	
	@Override
	public MusicSource newMusic(byte[] data) {
		try {
			return new DavidMusicSource(sequencer, receiver, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public MusicSource newMusic(String name) {
		
		return null;
	}

	@Override
	public boolean init() {
		try {
			inited = false;
			sequencer = (Sequencer) MidiSystem.getSequencer(false);
			sequencer.open();

		    receiver = VolumeScalingReceiver.getInstance(nDevice);
		    if(receiver == null)
		    	return false;
		  
		    sendSysexMessage(receiver, (byte)0xf0, (byte)0x7e, (byte)0x7f, (byte)9, (byte)1, (byte)0xf7);
		    transmitter = sequencer.getTransmitter();
		    transmitter.setReceiver(receiver);
		    
		    String name = receiver.getName();
		    byte[] namedata = new byte[name.length()];
			//Cyrillic convert
			for(int c = 0; c < name.length(); c++) 
				namedata[c] = (byte) name.codePointAt(c);
			name = new String(namedata, 0, name.length()).trim();
		    
		    Console.Println(name + " initialized", OSDTEXT_GOLD);
		    inited = true;
		    return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void setVolume(float volume) {
		if(volume != 0 && volume < 0.005)
			System.err.println();
		System.out.println("Midi volume set to " + volume);
		receiver.setGlobalVolume(volume);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isInited() {
		return inited;
	}

	@Override
	public void dispose() {
    	if(sequencer != null)
    	{
			sequencer.stop();
			sequencer.close();
    	}
    	if(transmitter != null)
    		transmitter.close();
    	if(receiver != null) 
    		receiver.close();
	} 
	
	@Override
	public void update() {/* nothing */}

	private static void sendSysexMessage(Receiver receiver, byte... message) {
	    SysexMessage msg = new SysexMessage();
	    try {
            msg.setMessage(message, message.length);
        } catch (InvalidMidiDataException ex) {
            throw new RuntimeException(ex);
        }
        receiver.send(msg, -1);
    }
}
