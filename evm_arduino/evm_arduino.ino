/*
Arduino Uno + HC-05 (Bluetooth) - echo bluetooth data

Serial (Tx/Rx) communicate to HC-05
HC-05 Rx - Uno Tx (1)
HC-05 Tx - Uno Rx (0)
HC-05 GND - Uno GND
HC-05 VCC - Uno 5V

*/
#include <EEPROM.h>
#include <SoftwareSerial.h>
#define DEBOUNCE 10
int count[12];
int n;
int addr = 0;
boolean enable_vote=false;  
byte buttons[] = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11}; 
 
#define NUMBUTTONS sizeof(buttons)

byte pressed[NUMBUTTONS], justpressed[NUMBUTTONS], justreleased[NUMBUTTONS];
byte previous_keystate[NUMBUTTONS], current_keystate[NUMBUTTONS];
 
SoftwareSerial bluetooth(12,13);
void setup()
{
  delay(1000);
  byte i;
  n = EEPROM.read(addr);
  for(i=1; i<=10; i++) 
    count[i]= EEPROM.read(addr + i);
    
  Serial.begin(9600);
  bluetooth.begin(9600);
   for (i=0; i< NUMBUTTONS; i++) 
   {
    pinMode(buttons[i], INPUT);
    digitalWrite(buttons[i], HIGH);
   }
}

void loop()
{
  byte thisSwitch=thisSwitch_justPressed();
  if(thisSwitch>=0 && thisSwitch<=n && enable_vote==true)
  {
         count[thisSwitch+1]++;   
         EEPROM.write(addr + thisSwitch+1, count[thisSwitch+1]); 
         enable_vote=false;
  }
  /*while(Serial.available())
  { 
    String s = Serial.readString();
     int data = s.toInt();
       
     if((data >= 1)&&(data <= n)&&(enable_vote==true))
       {
         count[data]++;   
         EEPROM.write(addr + data, count[data]); 
         enable_vote=false;
       }
       //count[data]++;   
       //EEPROM.write(addr + data, count[data]);
  }*/
  while(bluetooth.available())
  {
     int d = bluetooth.read();
     if(d == 114)
     { 
       for(int i=1; i<=n; i++)
       {
         bluetooth.print(EEPROM.read(addr+i));
         if(i!=n)
           bluetooth.print("#");
         else
           bluetooth.print("~");
         
       }
     }
     else if(d == 102)
     {
        for(int i=1; i<=10; i++)
         {
          EEPROM.write(addr+i, 0);
          count[i] = 0;
         }
     }
     else if(d==116)
     {
       enable_vote= true;
     }
     else 
     {
       n = d-47;
       EEPROM.write(addr, n);
       for(int i=1; i<=10; i++)
         {
          EEPROM.write(addr+i, 0);
          count[i] = 0;
         }
     }
  }
}
void check_switches()
{
  static byte previousstate[NUMBUTTONS];
  static byte currentstate[NUMBUTTONS];
  static long lasttime;
  byte index;
  if (millis() < lasttime) {
    lasttime = millis();
  }
  if ((lasttime + DEBOUNCE) > millis()) {
    return; 
  }
  lasttime = millis();
  for (index = 0; index < NUMBUTTONS; index++) {
    justpressed[index] = 0;       
    justreleased[index] = 0;
    currentstate[index] = digitalRead(buttons[index]);   
    if (currentstate[index] == previousstate[index]) {
      if ((pressed[index] == LOW) && (currentstate[index] == LOW)) {
        justpressed[index] = 1;
      }
      else if ((pressed[index] == HIGH) && (currentstate[index] == HIGH)) {
        justreleased[index] = 1; 
      }
      pressed[index] = !currentstate[index];  
    }
    previousstate[index] = currentstate[index]; 
  }
}
 
byte thisSwitch_justPressed() {
  byte thisSwitch = 255;
  check_switches();  
  for (byte i = 0; i < NUMBUTTONS; i++) {
    current_keystate[i]=justpressed[i];
    if (current_keystate[i] != previous_keystate[i]) {
      if (current_keystate[i]) thisSwitch=i;
    }
    previous_keystate[i]=current_keystate[i];
  }  
  return thisSwitch;
}
