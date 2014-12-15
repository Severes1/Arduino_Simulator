// Wall Avoidance December 4, 2014

int ledPin = 13;

// Motor Pins:
int motorPin1 = 3;
int motorPin2 = 11;

// Ultrasonic Range Finder Pins:
int us_input_pins[] = {
  1, 2, 3, 4
}; // right, up, left, down
int us_output_pins[] = {
  5, 6, 7, 8
};

// Other Variables:
double distance[] = {
  0.0, 0.0, 0.0, 0.0
}; // right, up, left, down
int pwm_v = 191;
int pwm_h = 191;
int DOM;
String message = "";

void setup() {
  int i = 0;
  for (i = 0; i < length(us_input_pins); i++) {
    pinMode(us_input_pins[i], INPUT);
    pinMode(us_output_pins[i], OUTPUT);
  }
  pinMode(ledPin, OUTPUT);
  pinMode(motorPin1, OUTPUT);
  pinMode(motorPin2, OUTPUT);
}

void loop() {
  // Get the distance in centimeters by sending an ultrasonic pulse.
  int i = 0;
  for (i = 0; i < length(us_output_pins); i++) {
    digitalWrite(us_output_pins[i], LOW);
  }
  //delay(2);
  for (i = 0; i < length(us_output_pins); i++) {
    digitalWrite(us_output_pins[i], HIGH);
  }
  //delay(10);
  for (i = 0; i < length(us_output_pins); i++) {
    digitalWrite(us_output_pins[i], LOW);
  }
  for (i = 0; i < length(us_input_pins); i++) {
    distance[i] = pulseIn(us_input_pins[i], HIGH)/57.355; //cm
  }

  DOM = pickDirection();

  assessSituation();

  switch (DOM) {
  case 0: 
    pwm_h = 240; 
    pwm_v = 191; 
    break;
  case 1: 
    pwm_v = 240; 
    pwm_h = 191; 
    break;
  case 2: 
    pwm_h = 127; 
    pwm_v = 191; 
    break;
  case 3: 
    pwm_v = 127; 
    pwm_h = 191; 
    break;
  }
  analogWrite(motorPin1, pwm_v); // Set the forward-backward motor.
  analogWrite(motorPin2, pwm_h); // Set the left-right motor.
}

int pickDirection() {
  boolean options[] = {
    true, true, true, true
  };
  int i;
  for (i = 0; i < length(distance); i++) {
    if (distance[i] < 20) {
      options[i] = false;
    }
    Serial.println(distance[i]);
  }
  Serial.println("");
  if (options[DOM]) {
    return DOM;
  } 
  else {
    int count = 0;
    for (i = 0; i < length(options); i++) {
      if (options[i]) {
        count++;
      }
    }
    if (count > 1) {
      options[(DOM + 2)%4] = false;
      count -= 1;
    } if (count == 0){
        return (DOM + 2) % 4;        	
    }
    //int c[count];//boolean c[choices]; //ARDUINO
    int c[count];
    count = 0;
    for (i = 0; i < length(options); i++) {
      if (options[i]) {
        c[count] = i;
        count++;
      }
    }
    return c[(int)random(0,length(c))];
  }
}

void assessSituation(){
  int i = 0;
  boolean options[] = {
    true, true, true, true    };
  for (i = 0; i < length(distance); i++){
    if (distance[i] < 70){
      options[i] = false;
    }
  }
  String newMessage = message;
  boolean temp[length(options)];
  boolean configs[][] = {
    {
      true, false, true, false        }
    , {
      false, true, true, true        }
    , {
      true, true, false, false        }
    , {
      true, true, true, true        }
  };
  for (i = 0; i < length(distance); i++){
    for (int j = 0; j < length(options); j++){
      temp[j] = options[(i+j)%length(options)];
    } 
    if (temp[0] && !temp[1] && temp[2] && !temp[3]) {
      newMessage = "corridorF";
    } 
    else if (temp[0] && temp[1] && temp[2] & !temp[3]){
      newMessage = "single wall";
    } 
    else if (options[i%length(options)] && options[(i+1)%length(options)] && !(options[(i+2)%length(options)] || options[(i+3)%length(options)])){
      newMessage = "corner";
    } 
    else if (temp[0] && temp[1] && temp[2] && temp[3]){
      newMessage = "free space";
    }
    if (newMessage != message){
//      Serial.println (newMessage);
      message = newMessage;
    }
  }
}

int length(int a[]){
  return sizeof(a) / sizeof(a[0]);
}

int length(double a[]){
  return sizeof(a) / sizeof(a[0]);
}

int length(boolean a[]){
  return sizeof(a) / sizeof(a[0]);
}




