// This is a sample blink program for the virtual arduino.

int ledPin = 13;

void setup(){
  pinMode(ledPin, OUTPUT);
}  

void loop(){
  digitalWrite(ledPin, LOW);
  Serial.println("Led is Off");
  delay(1000);
  digitalWrite(ledPin, HIGH);
  Serial.println("Led is On");
  delay(1000);
}
