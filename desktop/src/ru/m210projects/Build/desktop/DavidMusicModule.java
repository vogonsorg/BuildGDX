// This file is part of Mocha Doom.
// Copyright (C) 1993-1996  id Software, Inc.
// Copyright (C) 2010-2013  Victor Epitropou
// Copyright (C) 2016-2017  Alexandre-Xavier Labonté-Lamoureux
//
// Mocha Doom is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Mocha Doom is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along withMocha Doom.  If not, see <http://www.gnu.org/licenses/>.
//
// This file has been modified by Alexander Makarov-[M210] (m210-2007@mail.ru)

package ru.m210projects.Build.desktop;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;

import ru.m210projects.Build.Audio.Music;
import ru.m210projects.Build.OnSceenDisplay.Console;
import static ru.m210projects.Build.OnSceenDisplay.Console.*;

/** Concern separated from David Martel's MIDI & MUS player
 *  for Mocha Doom. Greatly improved upon by finnw, perfecting volume changes
 *  and MIDI device detection.
 *  
 * @author David Martel
 * @author velktron
 * @author finnw
 *
 */

public class DavidMusicModule implements Music {
	
	public static final int CHANGE_VOLUME=7;
	public static final int CHANGE_VOLUME_FINE=9;
	
	Sequencer sequencer;
	VolumeScalingReceiver receiver;
	Transmitter transmitter;
	boolean songloaded;
	MidiDevice.Info nDevice;
	String name;
	boolean inited;
	
	public DavidMusicModule(int nDevice) {
		List<MidiDevice.Info> devices = DavidMusicModule.getDevices();
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
	public boolean init() {
		try {
			inited = false;
			sequencer = (Sequencer) MidiSystem.getSequencer(false);
			sequencer.open();

		    receiver = VolumeScalingReceiver.getInstance(nDevice);
		    if(receiver == null)
		    	return false;
		  
		    // Configure General MIDI level 1
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

    private static void sendControlChange(Receiver receiver, int midiChan, int ctrlId, int value) {
    	ShortMessage msg = new ShortMessage();
        try {
            msg.setMessage(ShortMessage.CONTROL_CHANGE, midiChan, ctrlId, value);
        } catch (InvalidMidiDataException ex) {
            throw new RuntimeException(ex);
        }
        receiver.send(msg, -1);
    }

	private static void sendSysexMessage(Receiver receiver, byte... message) {
	    SysexMessage msg = new SysexMessage();
	    try {
            msg.setMessage(message, message.length);
        } catch (InvalidMidiDataException ex) {
            throw new RuntimeException(ex);
        }
        receiver.send(msg, -1);
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
	public void setVolume(int volume) {
		if(!inited) return;
		volume /= 2;
		System.out.println("Midi volume set to "+volume);
		receiver.setGlobalVolume(volume / 127f);
	}

	@Override
	public void pause() {
		if(!inited) return;
		if (songloaded)
			sequencer.stop();
	}

	@Override
	public void resume() {
		if(!inited) return;
		if (songloaded){
			System.out.println("Resuming song");
		sequencer.start();
		}
	}

	@Override
	public int open(byte[] data) {
		if(!inited) return -1;
		try {
            Sequence sequence;
	        ByteArrayInputStream bis;
	        try {
	            // If data is a midi file, load it directly
	            bis = new ByteArrayInputStream(data);
	            sequence = MidiSystem.getSequence(bis);
	        } catch (InvalidMidiDataException ex) {
	        	// Well, it wasn't. Dude.
                bis = new ByteArrayInputStream(data);
	            sequence = MusReader.getSequence(bis);
	        }
            sequencer.stop(); // stops current music if any
            sequencer.setSequence(sequence); // Create a sequencer for the sequence
            songloaded=true;
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return -1;
	    } 
		// In good old C style, we return 0 upon success?
		return 0;
	}

	@Override
	public void play(boolean looping) {
		if(!inited) return;
		if (songloaded){
	        for (int midiChan = 0; midiChan < 16; ++ midiChan) {
	            setPitchBendSensitivity(receiver, midiChan, 2);
	        }
            if (looping)
            	sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            else
            	sequencer.setLoopCount(0);
            sequencer.start(); // Start playing
		}
	}

	private void setPitchBendSensitivity(Receiver receiver, int midiChan, int semitones) {
	    sendRegParamChange(receiver, midiChan, 0, 0, 2);
    }

    private void sendRegParamChange(Receiver receiver, int midiChan, int paramMsb, int paramLsb, int valMsb) {
        sendControlChange(receiver, midiChan, 101, paramMsb);
        sendControlChange(receiver, midiChan, 100, paramLsb);
        sendControlChange(receiver, midiChan, 6, valMsb);
    }

    @Override
	public void stop() {
    	if(!inited) return;
    	if(sequencer != null && sequencer.isOpen())
    		sequencer.stop();
	}

	@Override
	public void close() {
		// In theory, we should ask the sequencer to "forget" about the song.
		// However since we can register another without unregistering the first,
		// this is practically a dummy.
		
		songloaded=false;
	}

	@Override
	public void open(String name) {
		
		
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
	public boolean isPlaying() {
		return sequencer.isRunning();
	}

}
