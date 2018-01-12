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

package ru.m210projects.Build.desktop;

import java.util.Arrays;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/** A {@link Receiver} that scales channel volumes.
 * 
 * Works by recognising channel volume change events and scaling the new volume
 * by the global music volume setting before forwarding the event to the
 * synthesizer.
 *
 * @author finnw
 *
 */
public class VolumeScalingReceiver implements Receiver {

    /** Guess which is the "best" available synthesizer & create a
     *  VolumeScalingReceiver that forwards to it.
     *
     * @return a <code>VolumeScalingReceiver</code> connected to a semi-
     * intelligently-chosen synthesizer.
     *
     */
	private static String name;

	public static VolumeScalingReceiver getInstance(MidiDevice.Info info) {
    	name = null;
    	try {
    		MidiDevice dev = MidiSystem.getMidiDevice(info);
    		dev.open();
            name = dev.getDeviceInfo().getName();
            return new VolumeScalingReceiver(dev.getReceiver());
        } catch (Exception ex) {
        	ex.printStackTrace();
            return null;
        }
    }

    public String getName()
    {
    	return name;
    }

    /** Create a VolumeScalingReceiver connected to a specific receiver. */
    public VolumeScalingReceiver(Receiver delegate) {
        this.channelVolume = new int[16];
        this.synthReceiver = delegate;
        Arrays.fill(this.channelVolume, 127);
    }

    @Override
    public void close() {
        synthReceiver.close();
    }

    /** Set the scaling factor to be applied to all channel volumes */
    public synchronized void setGlobalVolume(float globalVolume) {
        this.globalVolume = globalVolume;
        for (int chan = 0; chan < 16; ++ chan) {
            int volScaled = (int) Math.round(channelVolume[chan] * globalVolume);
            sendVolumeChange(chan, volScaled, -1);
        }
    }

    /** Forward a message to the synthesizer.
     * 
     *  If <code>message</code> is a volume change message, the volume is
     *  first multiplied by the global volume.  Otherwise, the message is
     *  passed unmodified to the synthesizer.
     */
    @Override
    public synchronized void send(MidiMessage message, long timeStamp) {
        int chan = getVolumeChangeChannel(message);
        if (chan < 0) {
            synthReceiver.send(message, timeStamp);
        } else {
            int newVolUnscaled = message.getMessage()[2];
            channelVolume[chan] = newVolUnscaled;
            int newVolScaled = (int) Math.round(newVolUnscaled * globalVolume);
            sendVolumeChange(chan, newVolScaled, timeStamp);
        }
    }

    /** Send a volume update to a specific channel.
     *
     *  This is used for both local & global volume changes.
     */
    private void sendVolumeChange(int chan, int newVolScaled, long timeStamp) {
        newVolScaled = Math.max(0, Math.min(newVolScaled, 127));
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(0xb0 | (chan & 15), 7, newVolScaled);
            synthReceiver.send(message, timeStamp);
        } catch (InvalidMidiDataException ex) {
            System.err.println(ex);
        }
    }

    /** Determine if the given message is a channel volume change.
     *
     * @return Channel number for which volume is being changed, or -1 if not a
     * channel volume change command.
     */
    private int getVolumeChangeChannel(MidiMessage message) {
        if (message.getLength() >= 3) {
            byte[] mBytes = message.getMessage();
            if ((byte) 0xb0 <= mBytes[0] && mBytes[0] < (byte) 0xc0 &&
                mBytes[1] == 7) {
                return mBytes[0] & 15;
            }
        }
        return -1;
    }

    private final int[] channelVolume;

    private float globalVolume;

    private final Receiver synthReceiver;

}
