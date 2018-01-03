/*
  Connect Bluetooth RX,TX to Arduino RX,TX

  Requires NeoPixel Library
  https://github.com/adafruit/Adafruit_NeoPixel

  If you want to view via Serial Monitor and have your Arduino connect to BT module
  simultaneously use Software Serial to open a second port.

  #include <SoftwareSerial.h>
  SoftwareSerial mySerial(3, 4); // RX, TX
*/



#include <Adafruit_NeoPixel.h>

int red, green, blue, mode;
Adafruit_NeoPixel strip = Adafruit_NeoPixel(432, 7, NEO_GRB + NEO_KHZ800);

void setup() {
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'

  // initialize serial:
  Serial.begin(9600);
  // mySerial.begin(9600);
}

void loop() {
  // if there's any serial available, read it:
  while (Serial.available() > 0) {
    readSerial();
  }
  if(mode == 1){
    rainbowCycle(20);
  }
}

void readSerial(){
  mode = Serial.parseInt();
    switch (mode) {
      case 1:
        break;
      case 2:
        // Transition
        break;
      case 3:
        // ColorWipe
        break;
      case 4:
        // Red
        break;
      case 5:
        // Green
        break;
      case 6:
        // Blue
        break;
      case 7:
        // White
        break;
      default:
        // statements
        break;
    }

    // look for the next valid integer in the incoming serial stream:
    int red = Serial.parseInt();
    // do it again:
    int green = Serial.parseInt();
    // do it again:
    int blue = Serial.parseInt();

    // look for the newline. That's the end of your
    // sentence:
    // if (mySerial.read() == '\n') {
    if (Serial.read() == '\n') {
      // sends confirmation
      Serial.println("received");
      // constrain the values to 0 - 255
      red = constrain(red, 0, 255);
      green = constrain(green, 0, 255);
      blue = constrain(blue, 0, 255);
      // fill strip
      if (mode == 0){
        colorSet(strip.Color(red, green, blue), 0);
      } else if(mode==3){
        colorWipe(strip.Color(red, green, blue), 50);
      }
      

      // send some data back
      //mySerial.println("received:"+String(red)+","+String(green)+","+String(blue));

    }
}

// Fill strip with a color
void colorSet(uint32_t c, uint8_t wait) {
  for (uint16_t i = 0; i < strip.numPixels(); i++) {
    strip.setPixelColor(i, c);
  }
  strip.show();
  delay(wait);
}

// Fill the dots one after the other with a color
void colorWipe(uint32_t c, uint8_t wait) {
  for(uint16_t i=0; i<strip.numPixels(); i++) {
      strip.setPixelColor(i, c);
      strip.show();
      if (Serial.available() > 0) {
        readSerial();
        break;
      }
      delay(wait);
  }
}

// Slightly different, this makes the rainbow equally distributed throughout
void rainbowCycle(uint8_t wait) {
  uint16_t i, j;

  for(j=0; j<256*5; j++) { // 5 cycles of all colors on wheel
    for(i=0; i< strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel(((i * 256 / strip.numPixels()) + j) & 255));
    }
    strip.show();
    if (Serial.available() > 0) {
      readSerial();
      break;
    }
    delay(wait);
  }
}

// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  WheelPos = 255 - WheelPos;
  if(WheelPos < 85) {
   return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  } else if(WheelPos < 170) {
    WheelPos -= 85;
   return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  } else {
   WheelPos -= 170;
   return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  }
}

