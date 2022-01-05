/* Program by Ashutosh Yadav Student ID: 18249094 */

#include<SoftwareSerial.h>
SoftwareSerial bluetoothSerial(4,5);
String msg,cmd;
boolean motor_dir = 0;

#define pwm1     9   //input 2
#define pwm2    10   //input 1

int speed;

void setup() {
  // Initialization
    Serial.begin(9600); // Communication rate of the Bluetooth Module
    msg = "";
    bluetoothSerial.begin(9600);
    pinMode(pwm1,   OUTPUT);
    pinMode(pwm2,   OUTPUT);
}

void loop() {
  
  // To read message received from other Bluetooth Device
  if (bluetoothSerial.available() > 0){ // Check if there is data coming
    msg = bluetoothSerial.readString(); // Read the message as String
    Serial.println("Android Command: " + msg);
  }

// Change motor direction
  if (msg == "<reverse>"){
    motor_dir = !motor_dir;
    analogWrite(pwm2, speed);
    Serial.println("DC motor is in reverse\n");
    msg = ""; // reset command
  } else {
    if (msg == "<forward>"){
      analogWrite(pwm1, speed);
      Serial.println("DC motor is in forward\n"); 
      msg = ""; 
    }
  }

  // Turn on DC motor in Arduino board
  if (msg == "<turn on>"){
    analogWrite(pwm1, 20);
    Serial.println("DC motor is ON\n");
    msg = ""; // reset command
  } else {
    if (msg == "<turn off>"){
      analogWrite(pwm1, 0);
      analogWrite(pwm2,0);
      Serial.println("DC motor is OFF\n"); 
      msg = ""; 
    } else {
    if (msg == "<speed one>"){
      analogWrite(pwm1, 200);
      Serial.println("DC motor is set to speed 1\n"); 
      msg = ""; 
    } else {
    if (msg == "<speed two>"){
      analogWrite(pwm1, 150);
      Serial.println("DC motor is set to speed 2\n"); 
      msg = ""; 
    } else {
    if (msg == "<speed three>"){
      analogWrite(pwm1, 100);
      Serial.println("DC motor is set to speed 3\n"); 
      msg = ""; 
    } else {
    if (msg == "<speed four>"){
      analogWrite(pwm1, 68.7);
      Serial.println("DC motor is set to speed 4\n"); 
      msg = ""; 
    } else {
    if (msg == "<speed five>"){
      analogWrite(pwm1, 50);
      Serial.println("DC motor is set to speed 5\n"); 
      msg = ""; 
          }
       }
       }
      }
    }
  }
}
}
