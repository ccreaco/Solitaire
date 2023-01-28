package guisolitaire;

import java.awt.event.ActionListener;
import javax.swing.Timer;

import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class ScoreTimer {
	
	int secondsPassed = 0;
	String s;
	Label l = new Label();

	Timer timer = new Timer(1000, new ActionListener() {

		@Override
		public void actionPerformed(java.awt.event.ActionEvent arg0) {
			l.setText(s);
			secondsPassed++;
			s = secondsToString(secondsPassed);
			
		} 
	});

	
	public String secondsToString(int pTime) {
	    return String.format("%02d:%02d", pTime / 60, pTime % 60);
	}
	
	public void start() { 
		timer.start();
	}

	public void restart() { 
		timer.restart();
		secondsPassed = 0;
	}
	
	public void stop() { 
		timer.stop();
		secondsPassed = 0;
	}
	
	

}
