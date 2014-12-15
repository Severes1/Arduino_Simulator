import processing.core.*;
public class Arduino_Program {
    Arduino_Simulator parent;
    Arduino_Program(Arduino_Simulator p){
        parent = p;
    }
int MD_SIM_VARIABLE_delayCount=0;
boolean MD_SIM_VARIABLE_stillRunning = true;
    // motor demo
    
    int horizontal_motor_pin = 5;
    int vertical_motor_pin = 6;
    
    void setup(){
      // stop all motors by setting them to 191
    if(MD_SIM_VARIABLE_stillRunning)
  parent.analogWrite(vertical_motor_pin, 191);
    if(MD_SIM_VARIABLE_stillRunning)
  parent.analogWrite(horizontal_motor_pin, 191);
    }
    
    void loop(){
    if(MD_SIM_VARIABLE_stillRunning)
  parent.println("up");
    if(MD_SIM_VARIABLE_stillRunning)
  parent.analogWrite(vertical_motor_pin, 240); // go up
    if(MD_SIM_VARIABLE_stillRunning)
  parent.delay(3000); // wait 3 seconds
    if(MD_SIM_VARIABLE_stillRunning)
  parent.println("left");
    if(MD_SIM_VARIABLE_stillRunning)
  parent.analogWrite(vertical_motor_pin, 191); // stop going up
    if(MD_SIM_VARIABLE_stillRunning)
  parent.analogWrite(horizontal_motor_pin, 142); // go left
    if(MD_SIM_VARIABLE_stillRunning)
  parent.delay(3000);
    if(MD_SIM_VARIABLE_stillRunning)
  parent.println("down");
    if(MD_SIM_VARIABLE_stillRunning)
  parent.analogWrite(horizontal_motor_pin, 191); // stop going left
      parent.analogWrite(vertical_motor_pin, 142); // go down
    if(MD_SIM_VARIABLE_stillRunning)
  parent.delay(3000);
    if(MD_SIM_VARIABLE_stillRunning)
  parent.println("right");
      parent.analogWrite(vertical_motor_pin, 191); // stop going down
    if(MD_SIM_VARIABLE_stillRunning)
  parent.analogWrite(horizontal_motor_pin, 240); // go right
    if(MD_SIM_VARIABLE_stillRunning)
  parent.delay(3000);
    if(MD_SIM_VARIABLE_stillRunning)
  parent.analogWrite(horizontal_motor_pin, 191); // stop going left
    }
}
