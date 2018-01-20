/*
  Connect Bluetooth RX,TX to Arduino RX,TX

  Requires NeoPixel Library
  https://github.com/adafruit/Adafruit_NeoPixel

  If you want to view via Serial Monitor and have your Arduino connect to BT module
  simultaneously use Software Serial to open a second port.

  #include <SoftwareSerial.h>
  SoftwareSerial mySerial(3, 4); // RX, TX
*/

#include "EEPROM.h"
#include <Adafruit_NeoPixel.h>

const int KEY_PREF_RESUME_STARTUP = 25;
const int KEY_PREF_BRIGHTNESS = 26;
const int KEY_PREF_COLOR_DELAY = 27;

int EEPROM_MODE = 0;
int EEPROM_RED = 1;
int EEPROM_GREEN = 2;
int EEPROM_BLUE = 3;
int EEPROM_STARTUP = 4;
int EEPROM_DELAY = 5;

int current_red = 0;
int current_green = 0;
int current_blue = 0;
int target_red = EEPROM.read(EEPROM_RED);
int target_green = EEPROM.read(EEPROM_GREEN);
int target_blue = EEPROM.read(EEPROM_BLUE);
int mode = EEPROM.read(EEPROM_MODE);
int resume_startup = EEPROM.read(EEPROM_STARTUP);
int DELAY = EEPROM.read(EEPROM_DELAY);

int MinBrightness = 20;       //value 0-255
int MaxBrightness = 100;      //value 0-255

int numLoops1 = 10;
int numLoops2 = 5;
//int numLoops3 = 5;
//int numLoops4 = 3;          //add new integer and value for more color's loop if needed.

int fadeInWait = 30;          //lighting up speed, steps.
int fadeOutWait = 50;         //dimming speed, steps.

Adafruit_NeoPixel strip = Adafruit_NeoPixel(174, 7, NEO_GRB + NEO_KHZ800);

// Linear interpolation of y value given min/max x, min/max y, and x position.
float lerp(float x, float x0, float x1, float y0, float y1)
{
  // Clamp x within x0 and x1 bounds.
  x = x > x1 ? x1 : x;
  x = x < x0 ? x0 : x;
  // Calculate linear interpolation of y value.
  return y0 + (y1 - y0) * ((x - x0) / (x1 - x0));
}

// Set all pixels to the specified color.
void fill_pixels(Adafruit_NeoPixel& pixels, uint32_t color)
{
  for (int i = 0; i < pixels.numPixels(); ++i) {
    pixels.setPixelColor(i, color);
  }
  strip.show();
}

// Get the color of a pixel within a smooth gradient of two colors.
uint32_t color_gradient(uint8_t start_r, // Starting R,G,B color
                        uint8_t start_g,
                        uint8_t start_b,
                        uint8_t end_r,   // Ending R,G,B color
                        uint8_t end_g,
                        uint8_t end_b,
                        float pos)       // Position along gradient, should be a value 0 to 1.0
{
  // Interpolate R,G,B values and return them as a color.
  uint8_t red   = (uint8_t) lerp(pos, 0.0, 1.0, start_r, end_r);
  uint8_t green = (uint8_t) lerp(pos, 0.0, 1.0, start_g, end_g);
  uint8_t blue  = (uint8_t) lerp(pos, 0.0, 1.0, start_b, end_b);
  return Adafruit_NeoPixel::Color(red, green, blue);
}
void animate_gradient_fill(Adafruit_NeoPixel& pixels, // NeoPixel strip/loop/etc.
                           uint8_t start_r,           // Starting R,G,B color
                           uint8_t start_g,
                           uint8_t start_b,
                           uint8_t end_r,             // Ending R,G,B color
                           uint8_t end_g,
                           uint8_t end_b,
                           int duration_ms)           // Total duration of animation, in milliseconds
{
  unsigned long start = millis();
  // Display start color.
  fill_pixels(pixels, Adafruit_NeoPixel::Color(start_r, start_g, start_b));
  // Main animation loop.
  unsigned long delta = millis() - start;
  while (delta < duration_ms) {
    // Calculate how far along we are in the duration as a position 0...1.0
    float pos = (float)delta / (float)duration_ms;
    // Get the gradient color and fill all the pixels with it.
    uint32_t color = color_gradient(start_r, start_g, start_b, end_r, end_g, end_b, pos);
    fill_pixels(pixels, color);
    // Update delta and repeat.
    delta = millis() - start;
  }
  // Display end color.
  fill_pixels(pixels, Adafruit_NeoPixel::Color(end_r, end_g, end_b));
}

void rgbBreathe(uint32_t c, uint8_t x, uint8_t y) {
  for (int j = 0; j < x; j++) {
    for (uint8_t b = MinBrightness; b < MaxBrightness; b++) {
      strip.setBrightness(b * 255 / 255);
      for (uint16_t i = 0; i < strip.numPixels(); i++) {
        strip.setPixelColor(i, c);
      }
      strip.show();
      delay(fadeInWait);
    }
    strip.setBrightness(MaxBrightness * 255 / 255);
    for (uint16_t i = 0; i < strip.numPixels(); i++) {
      strip.setPixelColor(i, c);
      strip.show();
      delay(y);
    }
    for (uint8_t b = MaxBrightness; b > MinBrightness; b--) {
      strip.setBrightness(b * 255 / 255);
      for (uint16_t i = 0; i < strip.numPixels(); i++) {
        strip.setPixelColor(i, c);
      }
      strip.show();
      delay(fadeOutWait);
    }
  }
}



void setup() {
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'

  // initialize serial:
  Serial.begin(9600);
  // mySerial.begin(9600);
  if (resume_startup == 1) {
    switch (mode) {
      case 0:
        colorSet(strip.Color(target_red, target_green, target_blue), 0);
        break;
      case 1:
        animate_gradient_fill(strip, current_red, current_green, current_blue, target_red, target_green, target_blue, 1000);
        break;
      case 2:
        colorWipe(strip.Color(target_red, target_green, target_blue), DELAY);
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
    current_red = target_red;
    current_green = target_green;
    current_blue = target_blue;
  }
}

void loop() {
  // if there's any serial available, read it:
  while (Serial.available() > 0) {
    readSerial();
  }
  if (mode == 3) {
    rainbowCycle(20);
  } else if (mode == 4) {
    rgbBreathe(strip.Color(current_red, current_green, current_blue), numLoops1, 250);
  }
}

void readSerial() {
  mode = Serial.parseInt();
  switch (mode) {
    case 3:
      break;
    case 4:
      break;
    case KEY_PREF_RESUME_STARTUP:
      resume_startup = Serial.parseInt();
      EEPROM.write(EEPROM_STARTUP, resume_startup);
      break;
    case KEY_PREF_BRIGHTNESS:
      strip.setBrightness(Serial.parseInt());
      strip.show();
      break;
    case KEY_PREF_COLOR_DELAY:
      DELAY = Serial.parseInt();
      EEPROM.write(EEPROM_DELAY, DELAY);
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
    target_red = constrain(red, 0, 255);
    target_green = constrain(green, 0, 255);
    target_blue = constrain(blue, 0, 255);
    // fill strip
    if (mode == 0) {
      colorSet(strip.Color(target_red, target_green, target_blue), 0);
    } else if (mode == 1) {
      animate_gradient_fill(strip, current_red, current_green, current_blue, target_red, target_green, target_blue, 1000);
    } else if (mode == 2) {
      colorWipe(strip.Color(target_red, target_green, target_blue), DELAY);
    }

    current_red = target_red;
    current_green = target_green;
    current_blue = target_blue;

    EEPROM.write(EEPROM_MODE, mode);
    EEPROM.write(EEPROM_RED, current_red);
    EEPROM.write(EEPROM_GREEN, current_green);
    EEPROM.write(EEPROM_BLUE, current_blue);

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
  for (uint16_t i = 0; i < strip.numPixels(); i++) {
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

  for (j = 0; j < 256 * 5; j++) { // 5 cycles of all colors on wheel
    for (i = 0; i < strip.numPixels(); i++) {
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
  if (WheelPos < 85) {
    return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  } else if (WheelPos < 170) {
    WheelPos -= 85;
    return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  } else {
    WheelPos -= 170;
    return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  }
}


