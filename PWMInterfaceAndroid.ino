/* Program by Ashutosh Yadav Student ID: 18249094 */

#include<SoftwareSerial.h>
SoftwareSerial bluetoothSerial(4,5);


String msg,cmd;
boolean motor_dir = 0;
int speed_value = 0;
int i = 0;
int speed;
float rev = 0;
int rpm_val;
int prev = 0;
int time;


#define pwm1     9   //input 2
#define pwm2    10   //input 1



void INTERRUPT()
{
  rev++;
}




void setup() {
  // Initialization
    Serial.begin(9600); // Communication rate of the Bluetooth Module
    msg = "";
    attachInterrupt(1, INTERRUPT, RISING);
    bluetoothSerial.begin(9600);
    pinMode(pwm1,   OUTPUT);
    pinMode(pwm2,   OUTPUT);
}

void loop() {

// RPM value calculation from IR proximity sensor data 
  delay(1000);
  detachInterrupt(0);                   
  time = millis() - prev;          
  rpm_val = (rev/time) * 60000;       
  prev = millis();                  
  rev = 0;
  Serial.println(rpm_val);
  attachInterrupt(1, INTERRUPT, RISING);
  
  // To read message received from other Bluetooth Device
  if (bluetoothSerial.available() > 0){ // Check if there is data coming
    msg = bluetoothSerial.readString(); // Read the message as String
    speed_value = bluetoothSerial.read();   //reading the string sent from master device
    speed_value++;  
    Serial.println("Android Command: " + msg);
  }


  if (msg.substring(0,15) == "<speed changed>"){
    Serial.println(msg.substring(15).toInt());
    analogWrite(pwm1, msg.substring(15).toInt());
    analogWrite(pwm2, 0);
    Serial.println("DC Motor speed is controlled by slider now\n"); 
    msg = ""; 
  }

   if (msg.substring(0,15) == "<speed reverse>"){
    Serial.println(msg.substring(15).toInt());
    analogWrite(pwm1, 0);
    analogWrite(pwm2, msg.substring(15).toInt());
    Serial.println("DC Motor speed is in reverse and controlled by slider now\n"); 
    msg = ""; 
  }





// Change motor direction
//  if (msg == "<reverse>"){
//    motor_dir = !motor_dir;
//    analogWrite(pwm2, speed);
//    Serial.println("DC motor is in reverse\n");
//    msg = ""; // reset command
//  } else {
//if (msg == "<forward>"){
//      analogWrite(pwm1, speed);
//      Serial.println("DC motor is in forward\n"); 
//      msg = ""; 
//    }
//  }

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
    }
  }

  if (msg == "<turn off><turn off>"){
      analogWrite(pwm1, 0);
      analogWrite(pwm2,0);
      Serial.println("DC motor is OFF\n"); 
      msg = ""; 
    }


    if (msg == "<turn off><turn off><turn off>"){
      analogWrite(pwm1, 0);
      analogWrite(pwm2,0);
      Serial.println("DC motor is OFF\n"); 
      msg = ""; 
    }

    if (msg == "<turn off><turn off><turn off><turn off>"){
      analogWrite(pwm1, 0);
      analogWrite(pwm2,0);
      Serial.println("DC motor is OFF\n"); 
      msg = ""; 
    }


  // Speed 1 Controls 
  if (msg == "<speed one forward>"){
      analogWrite(pwm1, 200);
      analogWrite(pwm2, 0);
      Serial.println("DC motor is set to speed 1\n"); 
      msg = ""; 
  } else {
    if (msg == "<speed one reverse>"){
      analogWrite(pwm1,0);
      analogWrite(pwm2, 200);
      Serial.println("DC motor is set to speed 1 reverse\n"); 
      msg = ""; 
    }
  }

 // Speed 2 Controls 
 if (msg == "<speed two forward>"){
      analogWrite(pwm1, 150);
      analogWrite(pwm2, 0);
      Serial.println("DC motor is set to speed 2 forward\n"); 
      msg = ""; 
 } else {
    if (msg == "<speed two reverse>"){
      analogWrite(pwm1, 0);
      analogWrite(pwm2, 150);
      Serial.println("DC motor is set to speed 2 reverse\n"); 
      msg = ""; 
    }
  }

 // Speed 3 Controls 
 if (msg == "<speed three forward>"){
      analogWrite(pwm1, 100);
      analogWrite(pwm2, 0);
      Serial.println("DC motor is set to speed 3 forward\n"); 
      msg = ""; 
 } else {
    if (msg == "<speed three reverse>"){
      analogWrite(pwm1, 0);
      analogWrite(pwm2, 100);
      Serial.println("DC motor is set to speed 3 reverse\n"); 
      msg = ""; 
    }
  }

 // Speed 4 Controls 
 if (msg == "<speed four forward>"){
      analogWrite(pwm1, 69);
      analogWrite(pwm2, 0);
      Serial.println("DC motor is set to speed 4 forward\n"); 
      msg = ""; 
 } else {
    if (msg == "<speed four reverse>"){
      analogWrite(pwm1, 0);
      analogWrite(pwm2, 69);
      Serial.println("DC motor is set to speed 4 reverse\n"); 
      msg = ""; 
    }
  }
}
