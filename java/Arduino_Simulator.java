import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Arduino_Simulator extends PApplet {

	/**
	 * Welcome to Matt's Arduino Simulator for the Hopkins School Robotics Club!
	 * 
	 * Version 3: Converted to Java, better Ultrasonic sensor graphics, .ino integration.
	 * 
	 * This simulator includes: 1 mouse-draggable square virtual robot, 4
	 * virtual ultrasonic sensors, 2 motors - one for forward-backward, one for
	 * left-right movement
	 * 
	 * INSTRUCTIONS: Write your arduino code, fill out the Wiring.csv excel file, then
     * double click the "run_simulation" batch file.
	 * 
	 * NOTE - The sensorPorts are the input (echo) ports from the
	 * arduino. The output ports do not matter.
	 * 
	 * 
	 * __/__<- up | | left ->/ / <- right | | --/-- <- down
	 */

	/* WIRING */
	int[] sensorPorts;// = { 1, 2, 3, 4 }; // first is right, second is up, third
										// is left, fourth is down
	int[] motorPorts;// = { 11, 3 }; // first is left-right, second is up-down

	/*
	 * If leaveTrace is true, the robot will leave a trace of its path. If
	 * that's not desired, make leaveTrace = false;
	 */
	boolean leaveTrace = true;

	boolean liveEdit = true;

	//Arduino_Program arduino = new Arduino_Program(this);

	ArrayList<VirtualWall> virtual_walls = new ArrayList<VirtualWall>();
	boolean[] keys = new boolean[255];
	String virtual_timestamp = year() + nf(month(), 2) + nf(day(), 2) + "-"
			+ nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2);

    int OUTPUT = 1;
	int INPUT = 0;
    int LOW = 0;
	int HIGH = 1;
    
	public void keyReleased() {
		if (keyCode < keys.length)
			keys[keyCode] = false;
	}

	public void keyPressed() {
		if (key == ' ') {
            TARS.robotOn = !TARS.robotOn;
            TARS.ledOn = TARS.robotOn;
				
			if (TARS.robotOn){
                try {
                   startRobot();
                } catch (Exception e){
                    TARS.robotOn = false;
                    println(e);
                }
            }
            if (!TARS.robotOn) {
                stopRobot();
            }
		} else {
			if (keyCode < keys.length)
				keys[keyCode] = true;
			if (keyCode == 8 || keyCode == 90 && keys[17] || keyCode == 17 && keys[90]) {
				try {
					virtual_walls.remove(virtual_walls.size() - 1);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
			} else if ((keyCode == 83 && keys[17] || (keyCode == 17 && keys[83]))) {
				println();
				print("Saving");
				// For each VirtualWall make one String to be saved
				String[] data = new String[virtual_walls.size()];

				for (int i = 0; i < virtual_walls.size(); i++) {
					print('.');
					data[i] = virtual_walls.get(i).start.x + " , "
							+ virtual_walls.get(i).start.y + " , "
							+ virtual_walls.get(i).end.x + " , "
							+ virtual_walls.get(i).end.y;
				}

				// Save to File
				// The same file is overwritten by adding the data folder path
				// to saveStrings().
				saveStrings("/bin/walls.csv", data);
				println("Done Saving.");
			}
		}
	}

	class VirtualWall {
		PVector start;
		PVector end;

		VirtualWall(int x, int y) {
			start = new PVector(x, y);
			end = new PVector(x, y);
		}

		public float get_distance_from(float x, float y, int orientation) {
			float d = 0;
			switch (orientation) {
			case 0:
				if (y >= min(this.start.y, this.end.y)
						&& y <= max(this.start.y, this.end.y)) {
					d = this.start.x - x;
				}
				break;
			case 1:
				if (x >= min(this.start.x, this.end.x)
						&& x <= max(this.start.x, this.end.x)) {
					d = y - this.start.y;
				}
				break;
			case 2:
				if (y >= min(this.start.y, this.end.y)
						&& y <= max(this.start.y, this.end.y)) {
					d = x - this.start.x;
				}
				break;
			case 3:
				if (x >= min(this.start.x, this.end.x)
						&& x <= max(this.start.x, this.end.x)) {
					d = this.start.y - y;
				}
				break;
			default:
				break;
			}
			return d;
		}
	}

	class Ultrasonic_Sensor {
		int orientation = 0; // 0 = right, 1 = up, 2 = left, 3 = down
		PVector position = new PVector(0, 0);

		Ultrasonic_Sensor(int _orientation) {
			if (orientation == orientation % 4) {
				this.orientation = _orientation;
			} else {
				println("INVALID ORIENTATION. 0 = right, 1 = up, 2 = left, 3 = down");
			}
		}

		public float sense() {
			float min_distance = width + height;
			switch (orientation) {
			case 0:
				min_distance = width - this.position.x;
				break;
			case 1:
				min_distance = this.position.y;
				break;
			case 2:
				min_distance = this.position.x;
				break;
			case 3:
				min_distance = height - this.position.y;
				break;
			}
			float distance;
			for (VirtualWall vw : virtual_walls) {
				distance = vw.get_distance_from(this.position.x,
						this.position.y, orientation);
				if (distance > 0 && distance < min_distance) {
					min_distance = distance;
				}
			}
			return pixels_to_reading(min_distance);
		}

		public float pixels_to_reading(float px) {
			return 57.355f * px;
		}
	}

	class Motor {
		Robot r;
		int orientation; // 0 means left right, 1 means up down
		int pwm;
		float power;
		float efficiency = 1.0f;

		Motor(Robot _r, int _orientation) {
			this.r = _r;
			this.orientation = _orientation;
		}

		public void update() {
			switch (orientation) {
			case 0:
				r.changeX(power);
				break;
			case 1:
				r.changeY(power);
				break;
			}
		}

		public void setPwm(int _pwm) {
			pwm = _pwm;
			this.setPower();
		}

		public void setPower() {
			power = efficiency * (pwm - 191) / 40;
		}
	}

	class Robot {
		float x = width / 2;
		float y = height / 2;
		float w = 31;
		float h = 31;
		float px = x;
		float py = y;
		boolean robotOn = false;
		boolean ledOn = false;
		boolean intersecting = false;
		Motor[] motors;
		Ultrasonic_Sensor[] sensors;

		Robot() {
		}

		public void drawMe() {
			stroke(0);
			// println(get_next_wall(0), get_next_wall(1), get_next_wall(2),
			// get_next_wall(3));
			if (!dragging) {
				// x = min(max(x, get_next_wall(2) + this.w * 0.5),
				// get_next_wall(0) - this.w * 0.5);
				// y = min(max(y, get_next_wall(1) + this.h * 0.5),
				// get_next_wall(3) - this.h * 0.5);
				checkIntersecting();
			}
			pushMatrix();
			translate(x, y);
			rectMode(CENTER);
			fill(200);
			rect(0, 0, w, h);
			fill(255);
			rect(0f, 0f, 0.75f * w, 0.75f * h);
			for (int i = 0; i < 4; i++) {
				if (sensorPorts[i] != 0) {
					pushMatrix();
					rotate(-HALF_PI * sensors[i].orientation);
					fill(0, 88, 255);
					rect(w / 2, 0, 5, 10);
					fill(100);
					ellipse(w / 2 + 2, -3, 2, 1);
					ellipse(w / 2 + 2, 3, 3, 2);
					// line(0 - (w/5) + cos(HALF_PI * sensors[i].orientation) *
					// 0.5 * w, 0 - (h/5) - sin(HALF_PI *
					// sensors[i].orientation) * 0.5 * h, 0 + (w/5) +
					// cos(HALF_PI * sensors[i].orientation) * 0.5 * w, 0 +
					// (h/5) - sin(HALF_PI * sensors[i].orientation) * 0.5 * h);
					popMatrix();
				}
			}
			if (this.ledOn) {
				fill(255, 200, 0);
				stroke(0);
				strokeWeight(1);
				ellipse(w / 4, 0, min(w, h) / 8.0f, min(w, h) / 8.0f);
			}
			popMatrix();
		}

		public void changeX(float dist) {
			px = x;
			x += dist;
            if (checkIntersecting()) {
				x = px;
			}
		}

		public void changeY(float dist) {
            py = y;
			y -= dist;
			if (checkIntersecting()) {
				y = py;
			}
		}
        
        boolean checkIntersecting(){
            intersecting = false;
            for (VirtualWall vw : virtual_walls) {
                boolean intersectingX = max(x - this.w * 0.5f,
                        min(vw.start.x, vw.end.x)) < this.x + this.w * 0.5f
                        && min(x + this.w * 0.5f, max(vw.start.x, vw.end.x)) > this.x
                                - this.w * 0.5f;
                boolean intersectingY = max(y - this.h * 0.5f,
                        min(vw.start.y, vw.end.y)) < this.y + this.h * 0.5f
                        && min(y + this.h * 0.5f, max(vw.start.y, vw.end.y)) > this.y
                                - this.h * 0.5f;
                intersecting = intersecting || (intersectingX && intersectingY);
                if (!intersecting) {
                    intersecting = x > width - w * 0.5f || x < w * 0.5f
                            || y < h * 0.5f || y > height - h * 0.5f;
                }   
            }
            return intersecting;
        }

		public float get_next_wall(int dir) {
			float distance;
			int min_index = -1;
			float min_distance = 0;
			switch (dir) {
			case 0:
				min_distance = width - x;
				break;
			case 1:
				min_distance = y;
				break;
			case 2:
				min_distance = x;
				break;
			case 3:
				min_distance = height - y;
				break;
			}

			VirtualWall vw;
			float _x, _y;
			for (int i = 0; i < virtual_walls.size(); i++) {
				vw = virtual_walls.get(i);

				distance = min(
						vw.get_distance_from(x - 0.5f * this.w, y - 0.5f
								* this.h, dir),
						vw.get_distance_from(x + this.w, y - this.h, dir));
				if (distance > 0 && distance < min_distance) {
					min_index = i;
					min_distance = distance;
				}
			}
			if (min_index == -1) {
				switch (dir) {
				case 0:
					return width;
				case 1:
					return 0;
				case 2:
					return 0;
				case 3:
					return height;
				}
			} else {
				switch (dir) {
				case 0:
					return virtual_walls.get(min_index).start.x;
				case 1:
					return virtual_walls.get(min_index).start.y;
				case 2:
					return virtual_walls.get(min_index).start.x;
				case 3:
					return virtual_walls.get(min_index).start.y;
				}
			}
			return 0;
		}
	}

	Robot TARS; //http://interstellarfilm.wikia.com/wiki/TARS
	Arduino_Serial Serial;
	ArrayList<PVector> trace = new ArrayList<PVector>();
	ArrayList<Integer> traceColors = new ArrayList<Integer>();

	public void setup() {
		size(800, 600);
		//File_Converter conv = new File_Converter(this);
		//conv.setup();
        String[] wiring = loadStrings("Wiring.csv");

        sensorPorts = new int[4];
        motorPorts = new int[2];
        arrayCopy(PApplet.parseInt(split(wiring[1], ',')), 1, sensorPorts, 0, 4);
        arrayCopy(PApplet.parseInt(split(wiring[5], ',')), 1, motorPorts, 0, 2);
        
		Serial = new Arduino_Serial();
		TARS = new Robot(); 
		TARS.motors = new Motor[2];
		TARS.motors[0] = new Motor(TARS, 0); // new horizontal motor
		TARS.motors[1] = new Motor(TARS, 1); // new vertical motor

		TARS.sensors = new Ultrasonic_Sensor[4];
		for (int i = 0; i < 4; i++) {
			if (sensorPorts[i] != 0) {
				TARS.sensors[i] = new Ultrasonic_Sensor(i);
			}
		}
		if (leaveTrace) {
			trace.add(new PVector(TARS.x, TARS.y));
			trace.add(new PVector(TARS.x, TARS.y));
		}

		text("", 0, 0);
		// Load text file as a string
		String[] data = loadStrings("/bin/walls.csv");
		// Convert string into an array of integers using ',' as a delimiter
		for (int i = 0; i < data.length; i++) {
			float[] c = PApplet.parseFloat(split(data[i], ','));
			virtual_walls.add(new VirtualWall(floor(c[0]), floor(c[1])));
			virtual_walls.get(virtual_walls.size() - 1).end.x = c[2];
			virtual_walls.get(virtual_walls.size() - 1).end.y = c[3];
		}
        //startRobot();
        
        
	}

	public void draw() {
		background(255);
		for (int i = 0; i < 4; i++) {
			if (sensorPorts[i] != 0) {
				if (TARS.sensors[i].orientation % 2 == 0) {
					TARS.sensors[i].position.x = TARS.x + 0.5f * TARS.w
							* cos(TARS.sensors[i].orientation * HALF_PI);
					TARS.sensors[i].position.y = TARS.y;
				} else {
					TARS.sensors[i].position.x = TARS.x;
					TARS.sensors[i].position.y = TARS.y - 0.5f * TARS.h
							* sin(TARS.sensors[i].orientation * HALF_PI);
				}
			}
		}
		TARS.drawMe();
        fill(0);
        if (TARS.robotOn){
            text("Robot is ON", 400, 580);
        } else {
            text("Robot is OFF", 400, 580);
        }
		if (!dragging) {
			if (leaveTrace)
				if (TARS.x != trace.get(trace.size() - 2).x
						&& TARS.y != trace.get(trace.size() - 2).y) {
					trace.add(new PVector(TARS.x, TARS.y));
				} else {
					trace.set(trace.size() - 1, new PVector(TARS.x, TARS.y));
				}
			if (TARS.robotOn) {
				for (Motor m : TARS.motors) {
					m.update();
				}
			}
		}
		if (leaveTrace) {
			noFill();
			int col;
			if (traceColors.size() > 0) {
				col = traceColors.get(0);
			} else {
				col = color(random(255), random(255), random(255));
				traceColors.add(col);
			}
			stroke(col);
			beginShape();
			int colorIndex = 1;
			for (PVector p : trace) {
				if (p.x == -1 && p.y == -1) {
					endShape();
					colorIndex++;
					if (traceColors.size() >= colorIndex) {
						col = traceColors.get(colorIndex - 1);
					} else {
						col = color(random(255), random(255), random(255));
						traceColors.add(col);
					}
					stroke(col);
					beginShape();
				} else {
					vertex(p.x, p.y);
				}
			}
			endShape();
		}
		stroke(0);
		strokeWeight(2);
		for (VirtualWall vw : virtual_walls) {
			line(vw.start.x, vw.start.y, vw.end.x, vw.end.y);
		}
		strokeWeight(1);
		fill(0);
		text("Spacebar to start/stop robot", 500, 580);
		text("Ctrl+s to save", 700, 580);
		if (TARS.robotOn){
			try {
              //  arduino.loop();
            } catch (Exception e){
                TARS.robotOn = false;
                println(e);
            }
         }
	}

	class Arduino_Serial {
		Arduino_Serial() {
		}

		public void begin(float b) {
		}

		public void println(String x) {
			println(x);
		}

		public void println(float x) {
			println(x);
		}
	}

	public void pinMode(int pin, int val) {
		int i;
		for (i = 0; i < 2; i++) {
			if (motorPorts[i] == pin) {
				break;
			}
		}
		if (i >= 2) {
			int j;
			for (j = 0; j < 4; j++) {
				if (sensorPorts[j] == pin) {
					break;
				}
			}
			if (j >= 4) {
				if (pin != 13 && val != 1)
					println("WARNING: pinMode("
							+ pin
							+ ", ...) has no motor or sensor attached to the pin");
			}
		}
	}

	public void analogWrite(int pin, int val) {
		int i;
		for (i = 0; i < 2; i++) {
			if (motorPorts[i] == pin) {
				break;
			}
		}
		if (i >= 2) {
			println("Invalid Motor Pin: " + pin);
		} else {
			TARS.motors[i].setPwm(val);
		}
	}

	

	public void digitalWrite(int pin, int val) {
        if (TARS.robotOn){
            if (pin == 13) {
                if (val == LOW) {
                    TARS.ledOn = false;
                } else if (val == HIGH) {
                    TARS.ledOn = true;
                }
            }
        }
	}

	public int pulseIn(int pin, int val) {
		int i;
		for (i = 0; i < 4; i++) {
			if (sensorPorts[i] == pin) {
				break;
			}
		}
		if (i < 4) {
			return Math.round(TARS.sensors[i].sense());
		} else {
			println("WARNING: pulseIn(" + pin + ") is not a valid pin");
			return 0;
		}
	}

	boolean dragging = false;
	float dx, dy;

	public void mousePressed() {
		if (mouseX < TARS.x + 0.5f * TARS.w
				&& mouseX > TARS.x - 0.5f * TARS.w
				&& mouseY < TARS.y + 0.5f * TARS.h
				&& mouseY > TARS.y - 0.5f * TARS.h) {
			dragging = true;
			dx = mouseX - TARS.x;
			dy = mouseY - TARS.y;
		} else if (!TARS.robotOn || liveEdit) {
			if (mouseButton == LEFT) {
				virtual_walls.add(new VirtualWall(mouseX, mouseY));
			} else if (mouseButton == RIGHT) {
				float min = width + height;
				int min_index = 0;
				float distance = min + 1;
				for (int i = 0; i < virtual_walls.size(); i++) {
					int x1 = floor(virtual_walls.get(i).start.x);
					int x2 = floor(virtual_walls.get(i).end.x);
					int y1 = floor(virtual_walls.get(i).start.y);
					int y2 = floor(virtual_walls.get(i).end.y);
					if ((mouseX > min(x1, x2) && mouseX < max(x1, x2))
							|| (mouseY > min(y1, y2) && mouseY < max(y1, y2))) {
						if (x1 - x2 == 0) {
							distance = abs(mouseX - x1);
						}
						if (y1 - y2 == 0) {
							distance = abs(mouseY - y1);
						}
						if (distance < min) {
							min = distance;
							min_index = i;
						}
					}
				}
				if (min < 10)
					virtual_walls.remove(min_index);
			}
		}
	}

	public void mouseDragged() {
		if (dragging) {
			TARS.x = mouseX - dx;
			TARS.y = mouseY - dy;
		} else if (!TARS.robotOn || liveEdit) {
			if (mouseButton == LEFT) {
				if (abs(mouseX
						- virtual_walls.get(virtual_walls.size() - 1).start.x) < abs(mouseY
						- virtual_walls.get(virtual_walls.size() - 1).start.y)) {
					virtual_walls.get(virtual_walls.size() - 1).end.x = virtual_walls
							.get(virtual_walls.size() - 1).start.x;
					virtual_walls.get(virtual_walls.size() - 1).end.y = mouseY;
				} else {
					virtual_walls.get(virtual_walls.size() - 1).end.x = mouseX;
					virtual_walls.get(virtual_walls.size() - 1).end.y = virtual_walls
							.get(virtual_walls.size() - 1).start.y;
				}
			}
		}
	}

	public void mouseReleased() {
		if (dragging == true) {
			if (leaveTrace)
				trace.add(new PVector(-1, -1));
			trace.add(new PVector(TARS.x, TARS.y));
			trace.add(new PVector(TARS.x, TARS.y));
		}
		dragging = false;
	}

	int sizeof(int[] x) {
		return x.length;
	}

	int sizeof(int x) {
		return 1;
	}

	int sizeof(double[] x) {
		return x.length;
	}

	int sizeof(double x) {
		return 1;
	}

	int sizeof(boolean[] x) {
		return x.length;
	}

	int sizeof(boolean x) {
		return 1;
	}

    int resumeTime = 0;

    void setDelay(int milliseconds) {
      resumeTime = millis() + milliseconds;
    }
    
    void setDelay(double milliseconds){
        resumeTime = millis() + (int)milliseconds;
    }

    boolean delaying() {
      return (millis() < resumeTime);
    }
    
  //  ExecutorService executor = Executors.newFixedThreadPool(1);
    ArduinoThread worker; 
    Thread robot_thread;
    private void startRobot(){
        worker = new ArduinoThread(this);    
        TARS.ledOn = true;
        robot_thread = new Thread(worker);
		robot_thread.start();
    }
    
    private void stopRobot(){
        worker.stop();
        worker = null;
        robot_thread.interrupt();
        TARS.ledOn = false;
    }
    
    public static class ArduinoThread implements Runnable { 
        Arduino_Program arduino;
        boolean running = false;
        ArduinoThread(Arduino_Simulator parent){
            arduino = new Arduino_Program(parent);
        }
 
	//	@Override
		public void run() {
            running = true;
            arduino.setup();
            while(running){
                arduino.loop();
            }
            
		}
        public void stop(){
            running = false;
            arduino.MD_SIM_VARIABLE_stillRunning = false;
        }
    }
    
   // Arduino_Program arduino = new Arduino_Program(this);
	static public void main(String[] passedArgs) {
        //Arduino_Program arduino = new Arduino_Program(this);
        
		String[] appletArgs = new String[] { "Arduino_Simulator" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}
}
