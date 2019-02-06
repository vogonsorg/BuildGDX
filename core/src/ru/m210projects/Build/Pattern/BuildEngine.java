package ru.m210projects.Build.Pattern;

import static ru.m210projects.Build.Net.Mmulti.*;

import ru.m210projects.Build.Engine;
import ru.m210projects.Build.Pattern.BuildGame.NetMode;
import ru.m210projects.Build.Pattern.ScreenAdapters.GameAdapter;
import ru.m210projects.Build.Pattern.ScreenAdapters.LoadingAdapter;

public abstract class BuildEngine extends Engine {
	
	protected int ticks;
	private BuildGame game;

	public BuildEngine(BuildGame game, int ticks, boolean releasedEngine) throws Exception {
		super(releasedEngine);
		this.game = game;
		this.ticks = ticks;
	}
	
	public void inittimer(int tickspersecond) {
		super.inittimer(tickspersecond);
		
		timerskipticks = (timerfreq / timerticspersec) * ticks;
		updatesmoothticks();
	}
	
	@Override
	public void faketimerhandler() {
		BuildNet net = game.net;
		if(net == null) return; //not initialized yet
		
		if (!game.getScreen().getClass().getSuperclass().equals(GameAdapter.class) 
				&& !game.getScreen().getClass().equals(LoadingAdapter.class)) 
			handleevents();

		if (totalclock >= net.gNetFifoClock && net.ready2send) {
			updatesmoothticks();
			net.gNetFifoClock += ticks;

			handleevents();
			GetInput(net);
		}
	}
	
	@Override
	public void handleevents() {
		super.handleevents();
		game.input.gpmanager.handler();
	}
	
	protected void GetInput(BuildNet net) {
		if (numplayers > 1)
			net.GetPackets();

		for (int i = connecthead; i >= 0; i = connectpoint2[i])
			if (i != myconnectindex && net.gNetFifoHead[myconnectindex] - 200 > net.gNetFifoHead[i])
				return;

		game.input.ctrlGetInput(game.net.gInput);

		if((net.gNetFifoHead[myconnectindex] & (net.MovesPerPacket - 1)) != 0)
		{
			net.gFifoInput[net.gNetFifoHead[myconnectindex] & 0xFF][myconnectindex].
				Copy(net.gFifoInput[(net.gNetFifoHead[myconnectindex] - 1) & 0xFF][myconnectindex]);
			net.gNetFifoHead[myconnectindex]++;
			return;
		}
		
		net.gFifoInput[net.gNetFifoHead[myconnectindex] & 0xFF][myconnectindex].Copy(net.gInput);
		net.gNetFifoHead[myconnectindex]++;
		
		if ( game.nNetMode == NetMode.Single && numplayers < 2) {
			for (int i = connecthead; i >= 0; i = connectpoint2[i]) {
				if (i != myconnectindex) {
					net.gFifoInput[net.gNetFifoHead[i] & 0xFF][i].
					Copy(net.gFifoInput[(net.gNetFifoHead[i] - 1) & 0xFF][i]);
					net.gNetFifoHead[i]++;
				}
			}
			return;
		}
		
		net.GetNetworkInput();
	}
	
	public void setanisotropy(BuildConfig cfg, int anisotropy)
	{
		glanisotropy = anisotropy;
		render.gltexapplyprops();
		cfg.anisotropy = glanisotropy;
	}
	
	public void setwidescreen(BuildConfig cfg, boolean widescreen)
	{
		r_usenewaspect = widescreen ? 1 : 0;
		setaspect_new();
		cfg.widescreen = r_usenewaspect;
	}

}
