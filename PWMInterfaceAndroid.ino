/* Program by Ashutosh Yadav Student ID: 18249094 */

#include<SoftwareSerial.h>
SoftwareSerial bluetoothSerial(4,5);


String msg,cmd,sendmsg;
boolean motor_dir = 0;
int speed_value = 0;
int i = 0;
int speed;
float rev = 0;
int rpm_val;
int prev = 0;
int time;


#define pwm1     10   //input 2
#define pwm2    11   //input 1



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
  Serial.println("Rev counter");  
  Serial.println(rev);        
  rpm_val = (rev/time) * 60000;       
  prev = millis();                  
  rev = 0;
  Serial.println(rpm_val);
  attachInterrupt(1, INTERRUPT, RISING);
  bluetoothSerial.print(rpm_val);
  delay(20);
  
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



}
