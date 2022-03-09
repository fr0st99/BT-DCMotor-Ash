/* Program by Ashutosh Yadav Student ID: 18249094 */

#include<SoftwareSerial.h>
SoftwareSerial bluetoothSerial(4,5);
const byte interruptPin = 2;


String msg,cmd,sendmsg;
boolean motor_dir = 0;  
int i = 0;
int speed;
float rev = 0;
int rpm_val;
int prev = 0;
int time;

int value = 0;
float voltage;


#define pwm1     9   
#define pwm2     10 


void isr()
{
  rev++;
  
}

void setup() {
  // Initialization
    Serial.begin(9600); // Communication rate of the Bluetooth Module
    msg = "";
    attachInterrupt(digitalPinToInterrupt(interruptPin), isr, RISING);
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
  attachInterrupt(digitalPinToInterrupt(interruptPin), isr, RISING);
  bluetoothSerial.println(rpm_val);

  //bluetoothSerial.print('#');


  // SCRAPPED VOLTAGE READINGS 

  //value = analogRead(A0);
  //voltage = value * 5.0/1023;
  //Serial.print("Voltage= ");
  //Serial.println(voltage);
  //bluetoothSerial.println(voltage );
  //bluetoothSerial.print('~');
  //delay(500);
  
  
  
  if (bluetoothSerial.available() > 0){ 
    msg = bluetoothSerial.readString(); // read msg as string
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

  // Speed adjustment 

  if (msg.substring(0,15) == "<request value>"){
    int requestedSpeed = msg.substring(15).toInt();
    Serial.println(requestedSpeed);
    
    msg = ""; 

      
  }

  if (msg == "<request value 3000>"){
    analogWrite(pwm1, 255);
    analogWrite(pwm2,0);
    msg = ""; 
    }

    if (msg == "<request value 2500>"){
    analogWrite(pwm1, 200);
    analogWrite(pwm2,0);
    msg = ""; 
    }

    if (msg == "<request value 2000>"){
    analogWrite(pwm1, 150);
    analogWrite(pwm2,0);
    msg = ""; 
    }

    if (msg == "<request value 1500>"){
    analogWrite(pwm1, 120);
    analogWrite(pwm2,0);
    msg = ""; 
    }

    if (msg == "<request value 1000>"){
    analogWrite(pwm1, 90);
    analogWrite(pwm2,0);
    msg = ""; 
    }

    if (msg == "<request value 500>"){
    analogWrite(pwm1, 73);
    analogWrite(pwm2,0);
    msg = ""; 
    }

    if (msg == "<request value 0>"){
    analogWrite(pwm1, 0);
    analogWrite(pwm2,0);
    }

  

   
  





  // Turn on DC motor in Arduino board
  if (msg == "<turn on>"){
    analogWrite(pwm1, 0);
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
