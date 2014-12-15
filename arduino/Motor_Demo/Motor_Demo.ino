// motor demo

int horizontal_motor_pin = 5;
int vertical_motor_pin = 6;

void setup(){
  // stop all motors by setting them to 191
  analogWrite(vertical_motor_pin, 191);
  analogWrite(horizontal_motor_pin, 191);
}

void loop(){
  Serial.println("up");
  analogWrite(vertical_motor_pin, 240); // go up
  delay(3000); // wait 3 seconds
  Serial.println("left");
  analogWrite(vertical_motor_pin, 191); // stop going up
  analogWrite(horizontal_motor_pin, 142); // go left
  delay(3000);
  Serial.println("down");
  analogWrite(horizontal_motor_pin, 191); // stop going left
  analogWrite(vertical_motor_pin, 142); // go down
  delay(3000);
  Serial.println("right");
  analogWrite(vertical_motor_pin, 191); // stop going down
  analogWrite(horizontal_motor_pin, 240); // go right
  delay(3000);
  analogWrite(horizontal_motor_pin, 191); // stop going left
}
