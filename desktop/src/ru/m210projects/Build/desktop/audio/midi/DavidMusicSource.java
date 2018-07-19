// This file is part of Mocha Doom.
// Copyright (C) 1993-1996  id Software, Inc.
// Copyright (C) 2010-2013  Victor Epitropou
// Copyright (C) 2016-2017  Alexandre-Xavier Labonté-Lamoureux
//
// This file has been modified by Alexander Makarov-[M210] (m210-2007@mail.ru)

package ru.m210projects.Build.desktop.audio.midi;

import java.io.ByteArrayInputStream;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import ru.m210projects.Build.Audio.BMusic.MusicSource;

public class DavidMusicSource extends MusicSource {

	private boolean songloaded;
	private boolean looping;
	private Sequencer sequencer;
	private Receiver receiver;
	
	public DavidMusicSource(Sequencer sequencer, Receiver receiver, byte[] data) throws Exception
	{
		this.sequencer = sequencer;
		this.receiver = receiver;
		
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
		songloaded = true;
	}
	
	@Override
	public void play(boolean looping) {
		if (songloaded){
	        for (int midiChan = 0; midiChan < 16; ++ midiChan) {
	            setPitchBendSensitivity(receiver, midiChan, 2);
	        }
            if (looping)
            	sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            else
            	sequencer.setLoopCount(0);
            this.looping = looping;
            sequencer.start(); // Start playing
		}
	}

	@Override
	public void stop() {
		if(sequencer != null && sequencer.isOpen())
    		sequencer.stop();
	}

	@Override
	public void pause() {
		if (songloaded)
			sequencer.stop();
	}

	@Override
	public void resume() {
		if (songloaded){
			System.out.println("Resuming song");
			sequencer.start();
		}
	}

	@Override
	public void dispose() {
		stop();
		songloaded = false;
	}

	@Override
	public void update() { /* nothing */ }

	@Override
	public boolean isLooping() {
		return looping;
	}

	@Override
	public boolean isPlaying() {
		return sequencer.isRunning();
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

	private void setPitchBendSensitivity(Receiver receiver, int midiChan, int semitones) {
	    sendRegParamChange(receiver, midiChan, 0, 0, 2);
    }

    private void sendRegParamChange(Receiver receiver, int midiChan, int paramMsb, int paramLsb, int valMsb) {
        sendControlChange(receiver, midiChan, 101, paramMsb);
        sendControlChange(receiver, midiChan, 100, paramLsb);
        sendControlChange(receiver, midiChan, 6, valMsb);
    }
}
